## 通过快照备份和恢复数据
-----

### 1. 如何备份

有了 repostiroy 后，我们就可以做备份了，也叫快照，也就是记录当下数据的状态。如下所示我们创建一个名为 snapshot_1 的快照。

PUT /_snapshot/my_backup/snapshot_1?wait_for_completion=true
wait_for_completion 为 true 是指该 api 在备份执行完毕后再返回结果，否则默认是异步执行的，我们这里为了立刻看到效果，所以设置了该参数，线上执行时不用设置该参数，让其在后台异步执行即可。

执行成功后会返回如下结果，用于说明备份的情况：

{
  "snapshots": [
    {
      "snapshot": "snapshot_1",
      "uuid": "52Lr4aFuQYGjMEv5ZFeFEg",
      "version_id": 6030099,
      "version": "6.3.0",
      "indices": [
        ".monitoring-kibana-6-2018.05.30",
        ".monitoring-es-6-2018.05.28",
        ".watcher-history-7-2018.05.30",
        ".monitoring-beats-6-2018.05.29",
        "metricbeat-6.2.4-2018.05.28",
        ".monitoring-alerts-6",
        "metricbeat-6.2.4-2018.05.30"
      ],
      "include_global_state": true,
      "state": "SUCCESS",
      "start_time": "2018-05-31T12:45:57.492Z",
      "start_time_in_millis": 1527770757492,
      "end_time": "2018-05-31T12:46:15.214Z",
      "end_time_in_millis": 1527770775214,
      "duration_in_millis": 17722,
      "failures": [],
      "shards": {
        "total": 28,
        "failed": 0,
        "successful": 28
      }
    }
  ]
}
返回结果的参数意义都是比较直观的，比如 indices 指明此次备份涉及到的索引名称，由于我们没有指定需要备份的索引，这里备份了所有索引；state 指明状态；duration_in_millis 指明备份任务执行时长等。

我们可以通过 GET _snapshot/my_backup/snapshot_1获取 snapshot_1 的执行状态。

此时如果去 /mount/backups/my_backup 查看，会发现里面多了很多文件，这些文件其实都是基于 elasticsearch data 目录中的文件生成的压缩存储的备份文件。大家可以通过 du -sh . 命令看一下该目录的大小，方便后续做对比。

### 2. 何时备份

通过上面的步骤我们成功创建了一个备份，但随着数据的新增，我们需要对新增的数据也做备份，那么我们如何做呢？方法很简单，只要再创建一个快照 snapshot_2 就可以了。

PUT /_snapshot/my_backup/snapshot_2?wait_for_completion=true
当执行完毕后，你会发现 /mount/backups/my_backup 体积变大了。这说明新数据备份进来了。要说明的一点是，当你在同一个 repository 中做多次 snapshot 时，elasticsearch 会检查要备份的数据 segment 文件是否有变化，如果没有变化则不处理，否则只会把发生变化的 segment file 备份下来。这其实就实现了增量备份。

elasticsearch 的资深用户应该了解 force merge 功能，即可以强行将一个索引的 segment file 合并成指定数目，这里要注意的是如果你主动调用 force merge api，那么 snapshot 功能的增量备份功能就失效了，因为 api 调用完毕后，数据目录中的所有 segment file 都发生变化了。

另一个就是备份时机的问题，虽然 snapshot 不会占用太多的 cpu、磁盘和网络资源，但还是建议大家尽量在闲时做备份。

### 3. 如何恢复

所谓“养兵千日，用兵一时”，我们该演练下备份的成果，将其恢复出来。通过调用如下 api 即可快速实现恢复功能。

POST /_snapshot/my_backup/snapshot_1/_restore?wait_for_completion=true
{
  "indices": "index_1",
  "rename_replacement": "restored_index_1"
}
通过上面的 api，我们可以将 index_1 索引恢复到 restored_index_1 中。这个恢复过程完全是基于文件的，因此效率会比较高。

虽然我们这里演示的是在同一个集群做备份与恢复，你也可以在另一个集群上连接该 repository 做恢复。我们这里就不做说明了。

### 4. 备份的兼容性 

由于 Elasticsearch 版本更新比较快，因此大家在做备份与恢复的时候，要注意版本问题，同一个大版本之间的备份与恢复是没有问题的，比如都是 5.1 和 5.6 之间可以互相备份恢复。但你不能把一个高版本的备份在低版本恢复，比如将 6.x 的备份在 5.x 中恢复。而低版本备份在高版本恢复有一定要求：

1) 5.x 可以在 6.x 恢复

2) 2.x 可以在 5.x 恢复

3) 1.x 可以在 2.x 恢复

其他跨大版本的升级都是不可用的，比如1.x 的无法在 5.x 恢复。这里主要原因还是 Lucene 版本问题导致的，每一次 ES 的大版本升级都会伴随 Lucene 的大版本，而 Lucene 的版本是尽量保证向前兼容，即新版可以读旧版的文件，但版本跨越太多，无法实现兼容的情况也在所难免了。

```html
https://elasticsearch.cn/article/648
```