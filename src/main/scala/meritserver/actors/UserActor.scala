package meritserver.actors

import akka.actor.{Actor, ActorRef, Props}
import meritserver.models.User
import meritserver.services.DataAccessService

object UserActor {
  val actor: ActorRef = RootActor.system.actorOf(Props[UserActor], "UserActor")

  object GetUsers

  object DeleteUsers

  case class GetUser(id: String)

  case class CreateUser(user: User)

  case class CreateUsers(users: List[User])

  object LoadData

}

class UserActor extends Actor {
  import meritserver.actors.UserActor._

  var users: List[User] = List[User]()

  override def receive: Receive = {
    case GetUsers => sender ! users
    case DeleteUsers =>
      users = List()
      saveUsers()
    case GetUser(id) => sender ! users.find(_.id == id)
    case CreateUser(pUser) =>
      users = users :+ pUser
      sender ! pUser
      saveUsers()
    case CreateUsers(pUsers) =>
      users = pUsers
      sender ! pUsers
      saveUsers()
    case LoadData => users = DataAccessService.loadUsers()
  }

  private def saveUsers() = DataAccessService.saveUsers(users)
}
