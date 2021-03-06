1. 引言
Pandas是一个开源的Python数据分析库。Pandas把结构化数据分为了三类：

Series，1维序列，可视作为没有column名的、只有一个column的DataFrame；
DataFrame，同Spark SQL中的DataFrame一样，其概念来自于R语言，为多column并schema化的2维结构化数据，可视作为Series的容器（container）；
Panel，为3维的结构化数据，可视作为DataFrame的容器；
DataFrame较为常见，因此本文主要讨论内容将为DataFrame。DataFrame的生成可通过读取纯文本、Json等数据来生成，亦可以通过Python对象来生成：

import pandas as pd
import numpy as np


df = pd.DataFrame({'total_bill': [16.99, 10.34, 23.68, 23.68, 24.59],
                   'tip': [1.01, 1.66, 3.50, 3.31, 3.61],
                   'sex': ['Female', 'Male', 'Male', 'Male', 'Female']})
对于DataFrame，我们可以看到其固有属性：

# data type of columns
df.dtypes
# indexes
df.index
# return pandas.Index
df.columns
# each row, return array[array]
df.values
# a tuple representing the dimensionality of df
df.shape
.index，为行索引
.columns，为列名称（label）
.dtype，为列数据类型
2. SQL操作
官方Doc给出了部分SQL的Pandas实现，在此基础上本文给出了一些扩充说明。以下内容基于Python 2.7 + Pandas 0.18.1的版本。

select
SQL中的select是根据列的名称来选取；Pandas则更为灵活，不但可根据列名称选取，还可以根据列所在的position选取。相关函数如下：

loc，基于列label，可选取特定行（根据行index）；
iloc，基于行/列的position；
df.loc[1:3, ['total_bill', 'tip']]
df.loc[1:3, 'tip': 'total_bill']
df.iloc[1:3, [1, 2]]
df.iloc[1:3, 1: 3]
at，根据指定行index及列label，快速定位DataFrame的元素；
iat，与at类似，不同的是根据position来定位的；
df.at[3, 'tip']
df.iat[3, 1]
ix，为loc与iloc的混合体，既支持label也支持position；
df.ix[1:3, [1, 2]]
df.ix[1:3, ['total_bill', 'tip']]
此外，有更为简洁的行/列选取方式：

df[1: 3]
df[['total_bill', 'tip']]
# df[1:2, ['total_bill', 'tip']]  # TypeError: unhashable type
where
Pandas实现where filter，较为常用的办法为df[df[colunm] boolean expr]，比如：

df[df['sex'] == 'Female']
df[df['total_bill'] > 20]

# or
df.query('total_bill > 20')
在where子句中常常会搭配and, or, in, not关键词，Pandas中也有对应的实现：

# and
df[(df['sex'] == 'Female') & (df['total_bill'] > 20)]
# or
df[(df['sex'] == 'Female') | (df['total_bill'] > 20)]
# in
df[df['total_bill'].isin([21.01, 23.68, 24.59])]
# not
df[-(df['sex'] == 'Male')]
df[-df['total_bill'].isin([21.01, 23.68, 24.59])]
# string function
df = df[(-df['app'].isin(sys_app)) & (-df.app.str.contains('^微信\d+$'))]
对where条件筛选后只有一行的dataframe取其中某一列的值，其两种实现方式如下：

total = df.loc[df['tip'] == 1.66, 'total_bill'].values[0]
total = df.get_value(df.loc[df['tip'] == 1.66].index.values[0], 'total_bill')
distinct
drop_duplicates根据某列对dataframe进行去重：

df.drop_duplicates(subset=['sex'], keep='first', inplace=True)
包含参数：

subset，为选定的列做distinct，默认为所有列；
keep，值选项{'first', 'last', False}，保留重复元素中的第一个、最后一个，或全部删除；
inplace ，默认为False，返回一个新的dataframe；若为True，则返回去重后的原dataframe
group
group一般会配合合计函数（Aggregate functions）使用，比如：count、avg等。Pandas对合计函数的支持有限，有count和size函数实现SQL的count：

df.groupby('sex').size()
df.groupby('sex').count()
df.groupby('sex')['tip'].count()
对于多合计函数，

select sex, max(tip), sum(total_bill) as total
from tips_tb
group by sex;
实现在agg()中指定dict：

df.groupby('sex').agg({'tip': np.max, 'total_bill': np.sum})

# count(distinct **)
df.groupby('tip').agg({'sex': pd.Series.nunique})
as
SQL中使用as修改列的别名，Pandas也支持这种修改：

# first implementation
df.columns = ['total', 'pit', 'xes']
# second implementation
df.rename(columns={'total_bill': 'total', 'tip': 'pit', 'sex': 'xes'}, inplace=True)
其中，第一种方法的修改是有问题的，因为其是按照列position逐一替换的。因此，我推荐第二种方法。

join
Pandas中join的实现也有两种：

# 1.
df.join(df2, how='left'...)

# 2. 
pd.merge(df1, df2, how='left', left_on='app', right_on='app')
第一种方法是按DataFrame的index进行join的，而第二种方法才是按on指定的列做join。Pandas满足left、right、inner、full outer四种join方式。

order
Pandas中支持多列order，并可以调整不同列的升序/降序，有更高的排序自由度：

df.sort_values(['total_bill', 'tip'], ascending=[False, True])
top
对于全局的top：

df.nlargest(3, columns=['total_bill'])
对于分组top，MySQL的实现（采用自join的方式）：

select a.sex, a.tip
from tips_tb a
where (
	select count(*)
	from tips_tb b
	where b.sex = a.sex and b.tip > a.tip
) < 2
order by a.sex, a.tip desc;
Pandas的等价实现，思路与上类似：

# 1.
df.assign(rn=df.sort_values(['total_bill'], ascending=False)
          .groupby('sex')
          .cumcount()+1)\
    .query('rn < 3')\
    .sort_values(['sex', 'rn'])
    
# 2.
df.assign(rn=df.groupby('sex')['total_bill']
          .rank(method='first', ascending=False)) \
    .query('rn < 3') \
    .sort_values(['sex', 'rn'])
replace
replace函数提供对dataframe全局修改，亦可通过where条件进行过滤修改（搭配loc）：

# overall replace
df.replace(to_replace='Female', value='Sansa', inplace=True)

# dict replace
df.replace({'sex': {'Female': 'Sansa', 'Male': 'Leone'}}, inplace=True)

# replace on where condition 
df.loc[df.sex == 'Male', 'sex'] = 'Leone'
自定义
除了上述SQL操作外，Pandas提供对每列/每一元素做自定义操作，为此而设计以下三个函数：

map(func)，为Series的函数，DataFrame不能直接调用，需取列后再调用；
apply(func)，对DataFrame中的某一行/列进行func操作；
applymap(func)，为element-wise函数，对每一个元素做func操作
df['tip'].map(lambda x: x - 1)
df[['total_bill', 'tip']].apply(sum)
df.applymap(lambda x: x.upper() if type(x) is str else x)
3. 实战
环比增长
现有两个月APP的UV数据，要得到月UV环比增长；该操作等价于两个Dataframe left join后按指定列做减操作：

def chain(current, last):
    df1 = pd.read_csv(current, names=['app', 'tag', 'uv'], sep='\t')
    df2 = pd.read_csv(last, names=['app', 'tag', 'uv'], sep='\t')
    df3 = pd.merge(df1, df2, how='left', on='app')
    df3['uv_y'] = df3['uv_y'].map(lambda x: 0.0 if pd.isnull(x) else x)
    df3['growth'] = df3['uv_x'] - df3['uv_y']
    return df3[['app', 'growth', 'uv_x', 'uv_y']].sort_values(by='growth', ascending=False)
差集
对于给定的列，一个Dataframe过滤另一个Dataframe该列的值；相当于集合的差集操作：

def difference(left, right, on):
    """
    difference of two dataframes
    :param left: left dataframe
    :param right: right dataframe
    :param on: join key
    :return: difference dataframe
    """
    df = pd.merge(left, right, how='left', on=on)
    left_columns = left.columns
    col_y = df.columns[left_columns.size]
    df = df[df[col_y].isnull()]
    df = df.ix[:, 0:left_columns.size]
    df.columns = left_columns
    return df
如需转载，请注明作者及出处.
作者：Treant
出处：http://www.cnblogs.com/en-heng/
