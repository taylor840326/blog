## 段合并的优化和注意事项
-----

### 1. Merge优化

ES和Lucene会自动进行Merge操作

Merge操作相对比较重，需要优化，降低对系统的影响

### 1.1. 优化点1

可以将Refresh Interval调整到分钟级别

indices.memory.index_buffer_size 调大，默认10%

尽量避免文档的更新操作


### 1.2. 优化点2

降低最大分段大小，避免较大的发呢段继续参与Merge，节省系统资源。

index.merge.policy.segments_per_tier: 默认值为10，越小需要越多的合并的操作

index.merge.policy.max_merged_segment: 默认5GB，操作此大小以后，就不再参与后续的合并操作


### 2. Force Merge

当index不再有写入操作的时候，建议对其进行force merge

这样可以提升查询速度，减少内存开销。

```html
POST blogs/_forcemerge?max_num_segments=1

GET _cat/segments/blogs?v
```

最终分成几个segments比较合适？

越少越好，最好可以force merge 成1个，但是formce merge 会占用大量的网络IO和CPU

如果不能在业务高峰期之前昨晚，就需要考虑增大最终的分段数

shard的大小/ index.merge.policy.max_merged_segment的大小

