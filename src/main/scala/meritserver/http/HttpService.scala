package meritserver.http

import akka.http.scaladsl.server.Directives._
import meritserver.http.routes.{MeritServiceRoute, TransactionServiceRoute, UserServiceRoute}

trait HttpService extends UserServiceRoute with TransactionServiceRoute with MeritServiceRoute {

  val routes =
    pathPrefix("v1") {
      usersRoute ~ transactionsRoute ~ meritsRoute
    }
}
