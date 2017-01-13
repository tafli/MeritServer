package meritserver.services

import akka.util.Timeout
import meritserver.actors.UserActor
import meritserver.models.{CreateUser, User}
import meritserver.utils.{Configuration, TryAwait}

object UsersService extends UsersService with Configuration {
  val usersFile = meritUsersFile

  def load(): Option[String] = FileAccessService.readFromFile(usersFile)

  def save(data: String): Unit = FileAccessService.writeToFile(usersFile, data)
}

trait UsersService extends TryAwait {

  import akka.pattern.ask
  import scala.util.Try
  import scala.concurrent.duration._

  implicit val timeout: Timeout = Timeout(1 seconds)

  def getUsers: Try[List[User]] = tryAwait((UserActor.actor ? UserActor.GetUsers).mapTo[List[User]])

  def getUserById(id: String): Try[Option[User]] = tryAwait((UserActor.actor ? UserActor.GetUser(id)).mapTo[Option[User]])

  def deleteUsers = UserActor.actor ! UserActor.DeleteUsers

  def createUser(user: CreateUser) = tryAwait((UserActor.actor ? UserActor.CreateUser(User(familyName = user.familyName, firstName = user.firstName))).mapTo[User])

  def createUsers(users: List[CreateUser]) =
    tryAwait((UserActor.actor ? UserActor.CreateUsers(users.map(user => User(familyName = user.familyName, firstName = user.firstName)))).mapTo[List[User]])
}