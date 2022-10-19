package dev.emortal.divine.utils

import dev.emortal.divine.DivinePlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import redis.clients.jedis.JedisPooled

object JedisStorage {

    val jedisScope = CoroutineScope(Dispatchers.IO)
    val jedis = JedisPooled(DivinePlugin.divineConfig.address)

}