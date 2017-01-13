package meritserver.http

import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import meritserver.http.routes.{TransactionsServiceRoute, UsersServiceRoute}

trait HttpService extends UsersServiceRoute with TransactionsServiceRoute {

  import scala.concurrent.duration._
  override implicit val timeout: Timeout = Timeout(1 seconds)

  val routes =
    pathPrefix("v1") {
      usersRoute ~ transactionsRoute
    }
}
