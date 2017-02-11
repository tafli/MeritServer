package meritserver.utils

import com.typesafe.config.ConfigFactory
import meritserver.services.FileAccessService

trait Configuration {
  private val config     = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")

  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")

  val meritStartAmount: Int = config.getInt("merit.startAmount")

  val meritDataDir: String = config.getString("merit.dataDir")
  val meritUsersFile: String = s"${config.getString("merit.dataDir")}/users.json"
  val meritTeamsFile: String = s"${config.getString("merit.dataDir")}/teams.json"
  val meritTransactionsFile: String = s"${config.getString("merit.dataDir")}/transactions.json"

  val payoutThreshold: Double = config.getDouble("merit.payoutThreshold")

  val masterKey: String = config.getString("merit.auth.masterKey")

  FileAccessService.createDirectoryIfNotExist(meritDataDir)
}
