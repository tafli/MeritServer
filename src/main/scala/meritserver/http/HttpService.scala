package meritserver.http

import akka.http.scaladsl.server.Directives._
import meritserver.http.routes.{TransactionServiceRoute, UserServiceRoute}

trait HttpService extends UserServiceRoute with TransactionServiceRoute {

  val routes =
    pathPrefix("v1") {
      usersRoute ~ transactionsRoute
    }
}
