package meritserver.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.http.routes._

trait HttpService
    extends UserServiceRoute
    with TeamServiceRoute
    with TransactionServiceRoute
    with MeritServiceRoute
    with FileServiceRoute {

  import ch.megard.akka.http.cors.CorsDirectives._

  val routes: Route = cors() {
    pathPrefix("v1") {
      usersRoute ~ teamRoute ~ transactionsRoute ~ meritsRoute
    } ~ staticFileRoute
  }
}
