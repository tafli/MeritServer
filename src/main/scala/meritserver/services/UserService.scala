package meritserver.services

import akka.agent.Agent
import meritserver.models.User
import meritserver.utils.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object UserService extends UserService with Configuration {

  val usersFile: String = meritUsersFile

  val userAgent = Agent(List[User]())

  def load(): Option[String] = FileAccessService.readFromFile(usersFile)

  def save(data: String): Unit = FileAccessService.writeToFile(usersFile, data)
}

trait UserService {

  def getUsers: List[User] = UserService.userAgent.get

  def getUserById(id: String): Option[User] =
    UserService.userAgent.get.find(_.id == id)

  def deleteUsers(): Unit = {
    val usersFuture = UserService.userAgent.alter(List())

    usersFuture.foreach { users =>
      DataAccessService.saveUsers(users)
    }

    val transactionsFuture = TransactionService.transactionAgent.alter(List())
    transactionsFuture.foreach { transactions =>
      DataAccessService.saveTransactions(transactions)
    }
  }

  def createUser(pUser: User): Try[User] = {
    UserService.userAgent.get.count(_.id == pUser.id) match {
      case 0 =>
        UserService.userAgent.alter(_ :+ pUser).foreach { users =>
          DataAccessService.saveUsers(users)
        }
        Success(pUser)
      case _ =>
        Failure(
          new IllegalArgumentException("User with this ID already exists!"))
    }
  }

  def createUsers(pUsers: List[User]): Future[List[User]] = {
    val usersFuture =
      UserService.userAgent.alter(pUsers)
    usersFuture.foreach { users =>
      DataAccessService.saveUsers(users)
    }
    usersFuture
  }
}
