package meritserver.services

import meritserver.models.{Transaction, Transaction2Json, User}
import meritserver.utils.Configuration
import spray.json._
import DefaultJsonProtocol._

object DataAccessService extends Configuration with Transaction2Json {
  implicit val userFormat = jsonFormat4(User.apply)
  implicit val transactionFormat = jsonFormat7(Transaction)

  def loadUsers(): List[User] = {
    FileAccessService.readFromFile(meritUsersFile).map(_.parseJson) match {
      case Some(json:JsValue) => json.convertTo[List[User]]
      case None => List[User]()
    }
  }

  def saveUsers(users: List[User]): Unit = FileAccessService.writeToFile(meritUsersFile, users.toJson.prettyPrint)

  def loadTransactions(): List[Transaction] = {
    FileAccessService.readFromFile(meritTransactionsFile).map(_.parseJson) match {
      case Some(json:JsValue) => json.convertTo[List[Transaction]]
      case None => List[Transaction]()
    }
  }

  def saveTransactions(transactions: List[Transaction]): Unit = FileAccessService.writeToFile(meritTransactionsFile, transactions.toJson.prettyPrint)
}
