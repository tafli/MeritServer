package meritserver.services

import akka.agent.Agent
import meritserver.models.{CreateUser, User}
import meritserver.utils.{Configuration, TryAwait}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserService extends UserService with Configuration {

  val usersFile: String = meritUsersFile

  val userAgent = Agent(List[User]())

  def load(): Option[String] = FileAccessService.readFromFile(usersFile)

  def save(data: String): Unit = FileAccessService.writeToFile(usersFile, data)
}

trait UserService extends TryAwait {

  def getUsers: List[User] = UserService.userAgent.get

  def getUserById(id: String): Option[User] = UserService.userAgent.get.find(_.id == id)

  def deleteUsers(): Unit = {
    val usersFuture = UserService.userAgent.alter(List())

    usersFuture.onSuccess {
      case users => DataAccessService.saveUsers(users)
    }
  }

  def createUser(pUser: CreateUser): User = {
    val newUser = User(familyName = pUser.familyName, firstName = pUser.firstName)
    UserService.userAgent.alter(_ :+ newUser).onSuccess {
      case users => DataAccessService.saveUsers(users)
    }
    newUser
  }

  def createUsers(pUsers: List[CreateUser]): Future[List[User]] = {
    val usersFuture = UserService.userAgent.alter(pUsers.map(user => User(familyName = user.familyName, firstName = user.firstName)))
    usersFuture.onSuccess {
      case users => DataAccessService.saveUsers(users)
    }
    usersFuture
  }
}