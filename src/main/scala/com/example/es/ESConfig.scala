package com.example.es

import com.typesafe.config.{Config, ConfigFactory}

trait ESConfig {
  private val config: Config = ConfigFactory.load().withFallback(ConfigFactory.load().getConfig("app"))
  val nodes: Array[String] = config.getString("elasticsearch.nodes").split(",")
  val clusterName: String = config.getString("elasticsearch.cluster")
  val port: Int = config.getInt("elasticsearch.port")
  val user: String = config.getString("elasticsearch.esuser")
  val password: String = config.getString("elasticsearch.password")
  val enableSsl: Boolean = config.getBoolean("elasticsearch.enablessl")
}

