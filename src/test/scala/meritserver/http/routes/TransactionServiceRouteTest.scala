package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import meritserver.models.{CreateTransaction, Transaction}
import org.scalatest.Assertion
import spray.json.JsArray

class TransactionServiceRouteTest extends ServiceTest {

  "The service for the users path" when {
    "calling GET /v1/transactions" should {
      "return an empty transaction list" in {
        Get("/transactions") ~> transactionsRoute ~> check {
          responseAs[JsArray] shouldEqual JsArray()
        }
      }
      "return all transactions" in withUsers(4) { users =>
        withTransactions(users) { transactions =>
          Get("/transactions") ~> transactionsRoute ~> check {
            val response = responseAs[JsArray]
            response.elements.size shouldEqual transactions.length
          }
        }
      }
    }
    "calling POST /v1/transactions" should {
      "return newly created transaction" when {
        "created" in withUsers(2) { users =>
          val transaction = CreateTransaction(from = users.head.id,
                                              to = users.tail.head.id,
                                              1,
                                              "Ex, its just a test!")
          Post(
            s"/$apiVersion/transactions",
            transaction
          ) ~> routes ~> check {
            status shouldBe Created
            assertTransaction(responseAs[Transaction],
                              Transaction(from = transaction.from,
                                          to = transaction.to,
                                          amount = transaction.amount,
                                          reason = transaction.reason))
          }
        }
      }
    }
  }

  private def assertTransaction(response: Transaction,
                                against: Transaction): Assertion = {
    assert(
      response.id.length > 0 && response.from == against.from && response.to == against.to && response.amount == against.amount && response.reason == against.reason && !response.booked
    )
  }
}
