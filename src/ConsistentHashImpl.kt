import java.util.*;

public class ConsistentHashImpl<K> implements ConsistentHash<K> {
    @Override
    public Shard getShardByKey(K key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<Shard, Set<HashRange>> addShard(Shard newShard, Set<Integer> vnodeHashes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<Shard, Set<HashRange>> removeShard(Shard shard) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
