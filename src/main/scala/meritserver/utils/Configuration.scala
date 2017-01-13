package meritserver.utils

import com.typesafe.config.ConfigFactory

trait Configuration {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val meritStartAmount = config.getInt("merit.startAmount")
  val meritUsersFile = config.getString("merit.usersFile")
  val meritTransactionsFile = config.getString("merit.transactionsFile")
}
