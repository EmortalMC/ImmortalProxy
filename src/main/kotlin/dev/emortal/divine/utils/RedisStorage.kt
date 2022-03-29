package dev.emortal.divine.utils

import dev.emortal.divine.DivinePlugin
import dev.emortal.divine.config.DivineConfig
import org.redisson.Redisson
import org.redisson.config.Config

object RedisStorage {

    val redisson = Redisson.create(Config().also { it.useSingleServer().setAddress(DivinePlugin.divineConfig.address).setClientName("Proxy") })

}