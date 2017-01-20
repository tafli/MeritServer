package meritserver.services

import akka.agent.Agent
import meritserver.models.{CreateUser, User}
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

  def getUserById(id: String): Option[User] = UserService.userAgent.get.find(_.id == id)

  def deleteUsers(): Unit = {
    val usersFuture = UserService.userAgent.alter(List())

    usersFuture.onSuccess {
      case users => DataAccessService.saveUsers(users)
    }

    val transactionsFuture = TransactionService.transactionAgent.alter(List())
    transactionsFuture.onSuccess {
      case transactions => DataAccessService.saveTransactions(transactions)
    }
  }

  def createUser(pUser: CreateUser): Try[User] = {
    val newUser: User = mapUser(pUser)
    UserService.userAgent.get.count(_.id == newUser.id) match {
      case 0 =>
        UserService.userAgent.alter(_ :+ newUser).onSuccess {
          case users => DataAccessService.saveUsers(users)
        }
        Success(newUser)
      case _ => println("Got that User already")
        Failure(new IllegalArgumentException("User with this ID already exists!"))
    }
  }

  def createUsers(pUsers: List[CreateUser]): Future[List[User]] = {
    val usersFuture = UserService.userAgent.alter(pUsers.map(user => mapUser(user)))
    usersFuture.onSuccess {
      case users => DataAccessService.saveUsers(users)
    }
    usersFuture
  }

  private def mapUser(pUser: CreateUser): User = pUser.id.map(pId => User(id=pId, familyName = pUser.familyName, firstName = pUser.firstName))
    .getOrElse(User(familyName = pUser.familyName, firstName = pUser.firstName))
}