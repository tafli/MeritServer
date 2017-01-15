package meritserver.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import meritserver.models.{CreateTransaction, Transaction, Transaction2Json}
import meritserver.services.TransactionService
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.{Failure, Success}

trait TransactionServiceRoute extends TransactionService with BaseServiceRoute with SprayJsonSupport with DefaultJsonProtocol with Transaction2Json {
  implicit val transactionFormat: RootJsonFormat[Transaction] = jsonFormat7(Transaction)
  implicit val createTransactionFormat: RootJsonFormat[CreateTransaction] = jsonFormat4(CreateTransaction)

  val transactionsRoute: Route = pathPrefix("transactions") {
    pathEndOrSingleSlash {
      get {
        complete(getTransactions)
      } ~
      post {
        entity(as[CreateTransaction]) { transaction =>
          createTransaction(transaction) match {
            case Success(t:Transaction) => complete(t)
            case Failure(f) => complete(ClientError(400)("Bad Request", f.getMessage))
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