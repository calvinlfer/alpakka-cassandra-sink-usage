package com.experiments.calvin

import akka.actor.ActorSystem
import com.typesafe.config.Config

class Settings(config: Config) {
  object cassandra {
    val host: String = config.getString("cassandra.host")
    val port: Int = config.getInt("cassandra.port")
    val keyspace: String = config.getString("cassandra.keyspace")
    val trustStorePath: String = config.getString("cassandra.truststore-path")
    val trustStorePass: String = config.getString("cassandra.truststore-password")
    val username: Option[String] = {
      val user = config.getString("cassandra.username")
      if (user.nonEmpty) Some(user) else None
    }
    val password: Option[String] = {
      val pass = config.getString("cassandra.password")
      if (pass.nonEmpty) Some(pass) else None
    }
  }
}

object Settings {
  def apply(system: ActorSystem): Settings = new Settings(system.settings.config)
  def apply(config: Config): Settings = new Settings(config)
}
