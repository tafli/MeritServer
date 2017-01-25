package meritserver.utils

import com.typesafe.config.ConfigFactory

trait Configuration {
  private val config     = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")

  val meritStartAmount: Int = config.getInt("merit.startAmount")
  val meritUsersFile: String = config.getString("merit.usersFile")
  val meritTeamsFile: String = config.getString("merit.teamsFile")
  val meritTransactionsFile: String = config.getString("merit.transactionsFile")

  val payoutThreshold: Double = config.getDouble("merit.payoutThreshold")
}
