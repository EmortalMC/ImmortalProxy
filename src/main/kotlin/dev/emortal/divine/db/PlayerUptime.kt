package dev.emortal.divine.db

@kotlinx.serialization.Serializable
data class PlayerUptime(val uuid: String, val playtimeMap: MutableMap<String, Long>)
