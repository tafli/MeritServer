package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.http.directives.AuthDirectives
import meritserver.models.{CreateTransaction, Model2Json, Transaction}
import meritserver.services.TransactionService

import scala.util.{Failure, Success}

trait TransactionServiceRoute
    extends TransactionService
    with BaseServiceRoute
    with SprayJsonSupport
    with Model2Json
    with AuthDirectives {

  val transactionsRoute: Route = pathPrefix("transactions") {
    parameter('auth.?) { authToken =>
      pathEndOrSingleSlash {
        get {
          parameters('booked.?, 'from.?, 'to.?, 'fromTS.?, 'toTS.?)
            .as(TransactionService.Filter) { filter =>
              complete(getTransactions(filter))
            }
        } ~
          post {
            entity(as[CreateTransaction]) { transaction =>
              authorizeUser(transaction.from, authToken) {
                createTransaction(transaction) match {
                  case Success(pTransaction: Transaction) =>
                    complete(StatusCodes.Created, pTransaction)
                  case Failure(f) =>
                    complete(StatusCodes.BadRequest, f.getMessage)
                }
              }
            }
          }
      } ~
        path(Segment) { id: String =>
          authorizeAdmin(authToken) {
            delete {
                deleteTransaction(id)
                complete(StatusCodes.NoContent)
              }
          }
        }
    }
  }
}
