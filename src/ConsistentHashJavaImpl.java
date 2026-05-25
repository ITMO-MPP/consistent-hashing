import java.util.Map;
import java.util.Set;

/**
 * Consistent hashing implementation Java template.
 *
 * @author <First-Name> <Last-Name> // TODO: replace with your name
 */
public class ConsistentHashJavaImpl<K> implements ConsistentHash<K> {
    @Override
    public Shard getShardByKey(K key) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Map<Shard, Set<HashRange>> addShard(Shard newShard, Set<Integer> vnodeHashes) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Map<Shard, Set<HashRange>> removeShard(Shard shard) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
