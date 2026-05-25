import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class UnitTest {
    @Test
    fun testAddSingleShard() {
        val random = Random(System.currentTimeMillis())
        val cHash = ConsistentHashImpl<Int>()
        val shard1 = Shard("shard_1")
        val addRes = cHash.addShard(shard1, setOf(100))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)
        repeat(100) {
            val key = random.nextInt()
            assertEquals(shard1, cHash.getShardByKey(key))
        }
    }

    @Test
    fun testAddMultipleVnodesSingleShard() {
        val random = Random(System.currentTimeMillis())
        val cHash = ConsistentHashImpl<Int>()
        val shard1 = Shard("shard_1")
        val addRes = cHash.addShard(
            shard1,
            setOf(-100_000, 100_000, 300_000, 500_000, 900_000)
        )
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)
        repeat(100) {
            val key = random.nextInt()
            assertEquals(shard1, cHash.getShardByKey(key))
        }
    }

    @Test
    fun testAddMultipleShardsSingleVnode() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(200))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(101, 200))
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(150))
        assertEquals(
            mapOf(
                shard2 to setOf(HashRange(101, 150))
            ),
            addRes
        )

        val shard4 = Shard("shard_4")
        addRes = cHash.addShard(shard4, setOf(50))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(201, 50))
            ),
            addRes
        )

        val shard5 = Shard("shard_5")
        addRes = cHash.addShard(shard5, setOf(500))
        assertEquals(
            mapOf(
                shard4 to setOf(HashRange(201, 500))
            ),
            addRes
        )

        assertEquals(shard4, cHash.getShardByKey(-100))
        assertEquals(shard4, cHash.getShardByKey(10))
        assertEquals(shard1, cHash.getShardByKey(75))
        assertEquals(shard1, cHash.getShardByKey(100))
        assertEquals(shard5, cHash.getShardByKey(300))
        assertEquals(shard4, cHash.getShardByKey(1_000_000))
    }

    @Test
    fun testOneAfterAnother() {
        val cHash = ConsistentHashImpl<Int>()
        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(200, 500, 400))
        assertEquals(
            mapOf(shard1 to setOf(HashRange(101, 500))),
            addRes
        )

        assertEquals(shard1, cHash.getShardByKey(100))
        assertEquals(shard1, cHash.getShardByKey(-100))
        assertEquals(shard2, cHash.getShardByKey(150))
        assertEquals(shard2, cHash.getShardByKey(500))
        assertEquals(shard1, cHash.getShardByKey(501))
        assertEquals(shard1, cHash.getShardByKey(3000))
    }

    @Test
    fun testOneAfterAnotherCircleEnd() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(3000, -100, -500, 2500))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(2001, -100))
            ),
            addRes
        )
    }

    @Test
    fun testMultipleRangesReplaceSameShard() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(300, 1200, 2200))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(1700, 4200, 3200))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(1201, 1700),
                    HashRange(2201, 4200)
                )
            ),
            addRes
        )

        assertEquals(shard1, cHash.getShardByKey(4201))
    }

    @Test
    fun testMultipleRangesReplaceMultipleShard() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(1500, 4000, 3000))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(2001, 4000),
                    HashRange(1001, 1500)
                )
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(5000, -100, -200, 1300, 1250))
        assertEquals(
            mapOf(
                shard2 to setOf(HashRange(1001, 1300)),
                shard1 to setOf(HashRange(4001, -100))
            ),
            addRes
        )
    }

    @Test
    fun testAddMultipleShardsMultipleVnodesStress() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(200, 3000, -100))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(2001, -100),
                    HashRange(101, 200)
                )
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(300, -200, 400))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(201, 400)),
                shard2 to setOf(HashRange(3001, -200))
            ),
            addRes
        )

        val shard4 = Shard("shard_4")
        addRes = cHash.addShard(shard4, setOf(1500, 1800, 1700))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(1001, 1800))
            ),
            addRes
        )

        val shard5 = Shard("shard_5")
        addRes = cHash.addShard(shard5, setOf(1600, 1750, 150))
        assertEquals(
            mapOf(
                shard2 to setOf(HashRange(101, 150)),
                shard4 to setOf(
                    HashRange(1701, 1750),
                    HashRange(1501, 1600)
                )
            ),
            addRes
        )

        val shard6 = Shard("shard_6")
        addRes = cHash.addShard(shard6, setOf(4000, -300))
        assertEquals(
            mapOf(
                shard3 to setOf(HashRange(3001, -300))
            ),
            addRes
        )

        assertEquals(shard2, cHash.getShardByKey(-100))
        assertEquals(shard4, cHash.getShardByKey(1602))
        assertEquals(shard2, cHash.getShardByKey(-150))
        assertEquals(shard3, cHash.getShardByKey(350))
    }

    @Test
    fun testAddAndRemoveSingleShard() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(1500))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(1001, 1500))
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard2)
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(1001, 1500))
            ),
            removeRes
        )

        val random = Random(System.currentTimeMillis())
        repeat(100) {
            val key = random.nextInt()
            assertEquals(shard1, cHash.getShardByKey(key))
        }
    }

    @Test
    fun testAddNewAndRemoveOldShard() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(200, 1100, 2100))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(1600))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(1101, 1600))
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard1)
        assertEquals(
            mapOf(
                shard2 to setOf(HashRange(1601, 1100))
            ),
            removeRes
        )

        val random = Random(System.currentTimeMillis())
        repeat(100) {
            val key = random.nextInt()
            assertEquals(shard2, cHash.getShardByKey(key))
        }
    }

    @Test
    fun testRemoveMultipleRangesSameShard() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(100, 500, 1000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(2000, 300))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(1001, 2000),
                    HashRange(101, 300)
                )
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(250, 200, 1500, 1700))
        assertEquals(
            mapOf(
                shard2 to setOf(
                    HashRange(1001, 1700),
                    HashRange(101, 250)
                )
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard3)
        assertEquals(
            mapOf(
                shard2 to setOf(
                    HashRange(1001, 1700),
                    HashRange(101, 250)
                )
            ),
            removeRes
        )
    }

    @Test
    fun testRemoveMultipleRangesSameShardRangeBeginEnd() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(200, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(3000, 1500, 500))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(201, 500),
                    HashRange(1001, 1500),
                    HashRange(2001, 3000)
                )
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(4000, -100, 700))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(501, 700),
                    HashRange(3001, -100)
                )
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard3)
        assertEquals(
            removeRes,
            mapOf(
                shard1 to setOf(
                    HashRange(501, 700),
                    HashRange(3001, -100)
                )
            )
        )
    }

    @Test
    fun testRemoveMultipleRangesMultipleShards() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(200, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(3000, 4000))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(2001, 4000)
                )
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(5000, -100, 700, 500, 2500))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(4001, -100),
                    HashRange(201, 700)
                ),
                shard2 to setOf(HashRange(2001, 2500))
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard3)
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(4001, -100),
                    HashRange(201, 700)
                ),
                shard2 to setOf(HashRange(2001, 2500))
            ),
            removeRes
        )
    }

    @Test
    fun testRemoveOldShardMultipleRangesMultipleShards() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(200, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(3000, 4000))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(2001, 4000))
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(5000, -100, 700, 500, 2500))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(4001, -100),
                    HashRange(201, 700)
                ),
                shard2 to setOf(HashRange(2001, 2500))
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard2)
        assertEquals(
            mapOf(
                shard3 to setOf(HashRange(2501, 4000))
            ),
            removeRes
        )
    }

    @Test
    fun testRemoveOldestShardMultipleRangesMultipleShards() {
        val cHash = ConsistentHashImpl<Int>()

        val shard1 = Shard("shard_1")
        var addRes = cHash.addShard(shard1, setOf(200, 1000, 2000))
        assertEquals(emptyMap<Shard, Set<HashRange>>(), addRes)

        val shard2 = Shard("shard_2")
        addRes = cHash.addShard(shard2, setOf(3000, 4000))
        assertEquals(
            mapOf(
                shard1 to setOf(HashRange(2001, 4000))
            ),
            addRes
        )

        val shard3 = Shard("shard_3")
        addRes = cHash.addShard(shard3, setOf(5000, -100, 700, 500, 2500))
        assertEquals(
            mapOf(
                shard1 to setOf(
                    HashRange(4001, -100),
                    HashRange(201, 700)
                ),
                shard2 to setOf(HashRange(2001, 2500))
            ),
            addRes
        )

        val removeRes = cHash.removeShard(shard1)
        assertEquals(
            mapOf(
                shard3 to setOf(
                    HashRange(701, 2000),
                    HashRange(-99, 200)
                )
            ),
            removeRes
        )
    }
}
