package meritserver.http.routes

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import meritserver.http.HttpService
import meritserver.models.{Team, Transaction, User}
import meritserver.services.{TeamService, TransactionService, UserService}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.language.postfixOps

/**
  * Created by boss on 21.01.17.
  */
class ServiceTest
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with HttpService
    with TeamService
    with TransactionService {
  override protected def log: LoggingAdapter = NoLogging

  val apiVersion = "v1"

  import scala.concurrent.duration._
  def withUsers(count: Int, team: String = "fcd", clearUsersList: Boolean = true)(test: List[User] => Any): Any = {
    if(clearUsersList) Await.result(UserService.userAgent.alter(List()), 1 second)

    val list = (for {
      i <- 1 to count
      user = User(familyName = s"FamilyName$i",
                  firstName = s"FirstName$i",
                  teamId = team)
    } yield user).toList

    Await.result(UserService.userAgent.alter(_ ++ list), 1 second)

    test(list)
  }

  def withTransactions(users: List[User])(test: List[Transaction] => Any): Any = {
    val list = (for {
      i <- 0 until users.length - 1
      transaction = Transaction(from = users(i).id,
                                to = users(i + 1).id,
                                amount = i+1,
                                reason = "Hey, just testing!")
    } yield transaction).toList

    test(Await.result(TransactionService.transactionAgent.alter(list), 1 second))
  }

  def withTeam(teamName: String*)(test: List[Team] => Any): Any = {
    val list =(for {
      name <- teamName
      team = Team(id = name, name = name, startAmount = 42)
    } yield team).toList

    test(Await.result(TeamService.teamAgent.alter(list), 1 second))
  }
}
