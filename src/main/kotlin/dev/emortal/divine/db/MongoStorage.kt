package dev.emortal.divine.db

import com.mongodb.client.model.ReplaceOptions
import dev.emortal.divine.DivinePlugin.Companion.divineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.util.*

class MongoStorage {

    companion object {
        //172.17.0.1 <- docker
        var client: CoroutineClient? = null
        var database: CoroutineDatabase? = null

        var playerUptime: CoroutineCollection<PlayerUptime>? = null

        val mongoScope = CoroutineScope(Dispatchers.IO)
    }

    fun init() {
        client = KMongo.createClient(divineConfig.mongoAddress).coroutine
        database = client!!.getDatabase("Divine")

        playerUptime = database!!.getCollection("playerUptime")
    }

    suspend fun getUptime(uuid: UUID): PlayerUptime? =
        playerUptime?.findOne(PlayerUptime::uuid eq uuid.toString())

    fun saveUptime(uptime: PlayerUptime) = runBlocking {
        playerUptime?.replaceOne(PlayerUptime::uuid eq uptime.uuid, uptime, ReplaceOptions().upsert(true))
    }

}