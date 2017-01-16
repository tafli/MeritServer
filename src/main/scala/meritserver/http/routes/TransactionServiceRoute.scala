package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateTransaction, Model2Json, Transaction}
import meritserver.services.TransactionService

import scala.util.{Failure, Success}

trait TransactionServiceRoute extends TransactionService with BaseServiceRoute with SprayJsonSupport with Model2Json {

  val transactionsRoute: Route = pathPrefix("transactions") {
    pathEndOrSingleSlash {
      get {
        complete(getTransactions)
      } ~
      post {
        entity(as[CreateTransaction]) { transaction =>
          createTransaction(transaction) match {
            case Success(t:Transaction) => complete(StatusCodes.Created -> t)
            case Failure(f) => complete(StatusCodes.BadRequest -> f.getMessage)
          }
        }
      }
    } ~
      path(Segment) { id: String =>
        get {
          getTransactionById(id) match {
            case Some(transaction) => complete(transaction)
            case _ => complete(StatusCodes.InternalServerError)
          }
        } ~
        delete {
          deleteTransaction(id)
          complete(StatusCodes.NoContent)
        }
      }
  }
}
