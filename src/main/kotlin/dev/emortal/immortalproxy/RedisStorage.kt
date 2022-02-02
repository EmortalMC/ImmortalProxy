package dev.emortal.immortalproxy

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPooled

object RedisStorage {

    val pool = JedisPooled("localhost", 6379)

}