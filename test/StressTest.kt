import kotlinx.serialization.json.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.*

class StressTest {
    private fun parseResult(jsonResult: JsonArray): Map<Shard, Set<HashRange>> {
        val result = HashMap<Shard, MutableSet<HashRange>>()
        for (curShard in jsonResult) {
            val shardObj = curShard.jsonObject
            val shardName = shardObj["first"]!!.jsonObject["shardName"]!!.jsonPrimitive.content
            val shard = Shard(shardName)
            assert(!result.containsKey(shard))

            val rangesSet = HashSet<HashRange>()
            val ranges = shardObj["second"]!!.jsonArray
            for (curRange in ranges) {
                val rangeObj = curRange.jsonObject
                val leftBorder = rangeObj["leftBorder"]!!.jsonPrimitive.int
                val rightBorder = rangeObj["rightBorder"]!!.jsonPrimitive.int
                val addRes = rangesSet.add(HashRange(leftBorder, rightBorder))
                assert(addRes)
            }

            val prevSet = result.put(shard, rangesSet)
            assert(prevSet == null)
        }
        return result
    }

    private fun processGetShard(cHash: ConsistentHash<String>, op: JsonObject) {
        val key = op["key"]!!.jsonPrimitive.content
        val result = cHash.getShardByKey(key).name
        val expectedResult = op["result"]!!.jsonObject["shardName"]!!.jsonPrimitive.content
        assertEquals(expectedResult, result)
    }

    private fun processAddShard(cHash: ConsistentHash<String>, op: JsonObject) {
        val shardName = op["newShard"]!!.jsonObject["shardName"]!!.jsonPrimitive.content
        val shard = Shard(shardName)
        val hashes = op["vnodeHashes"]!!.jsonArray
        val vnodes = HashSet<Int>()
        for (x in hashes) {
            vnodes.add(x.jsonPrimitive.int)
        }
        val result = cHash.addShard(shard, vnodes)
        val expectedResult = parseResult(op["result"]!!.jsonArray)
        assertEquals(expectedResult, result)
    }

    private fun processRemoveShard(cHash: ConsistentHash<String>, op: JsonObject) {
        val shardName = op["shard"]!!.jsonObject["shardName"]!!.jsonPrimitive.content
        val shard = Shard(shardName)
        val result = cHash.removeShard(shard)
        val expectedResult = parseResult(op["result"]!!.jsonArray)
        assertEquals(expectedResult, result)
    }

    private fun doSingleTest(operations: JsonArray) {
        val cHash = ConsistentHashImpl<String>()
        for (curOp in operations) {
            val op = curOp.jsonObject
            when (op["type"]!!.jsonPrimitive.content) {
                "AddShardRequest" -> processAddShard(cHash, op)
                "GetShardByKeyRequest" -> processGetShard(cHash, op)
                "RemoveShardRequest" -> processRemoveShard(cHash, op)
            }
        }
    }

    @Test
    fun testStress() {
        val filesList: List<Path>
        val files = Files.list(Paths.get("resources"))
        try {
            filesList = files.toList()
        } finally {
            files.close()
        }
        for (curPath in filesList) {
            if (!curPath.toString().endsWith(".json")) {
                continue
            }
            val curJson = Files.newBufferedReader(curPath).use { reader ->
                reader.readLines().joinToString("\n")
            }
            doSingleTest(Json.parseToJsonElement(curJson).jsonArray)
        }
    }
}
