package dev.emortal.divine.utils

import dev.emortal.divine.DivinePlugin
import org.redisson.Redisson
import org.redisson.config.Config

object RedisStorage {

    val redisson = Redisson.create(Config().also { it.useSingleServer().setAddress(DivinePlugin.divineConfig.address).setClientName("Proxy") })

}