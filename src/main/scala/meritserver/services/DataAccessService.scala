package meritserver.services

import meritserver.models.{Model2Json, Transaction, User}
import meritserver.utils.Configuration
import spray.json._

object DataAccessService extends Configuration with Model2Json {

  def loadUsers(): List[User] = {
    FileAccessService
      .readFromFile(meritUsersFile)
      .map(_.trim)
      .filter(_.length >= 2)
      .map(_.parseJson) match {
      case Some(json: JsValue) => json.convertTo[List[User]]
      case Some(_)             => List[User]()
      case None                => List[User]()
    }
  }

  def saveUsers(users: List[User]): Unit =
    FileAccessService.writeToFile(meritUsersFile, users.toJson.prettyPrint)

  def loadTransactions(): List[Transaction] = {
    FileAccessService
      .readFromFile(meritTransactionsFile)
      .map(_.trim)
      .filter(_.length >= 2)
      .map(_.parseJson) match {
      case Some(json: JsValue) => json.convertTo[List[Transaction]]
      case Some(_)             => List[Transaction]()
      case None                => List[Transaction]()
    }
  }

  def saveTransactions(transactions: List[Transaction]): Unit =
    FileAccessService.writeToFile(meritTransactionsFile,
                                  transactions.toJson.prettyPrint)
}
