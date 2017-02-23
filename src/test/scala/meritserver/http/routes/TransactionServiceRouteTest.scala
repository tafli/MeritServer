package meritserver.http.routes

import akka.http.scaladsl.model.StatusCodes._
import meritserver.models.{CreateTransaction, Transaction}
import org.scalatest.Assertion
import org.scalatest.exceptions.TestFailedException
import spray.json.JsArray

class TransactionServiceRouteTest extends ServiceTest {

  "The service for the transactions path" when {
    s"calling GET /$apiVersion/transactions" should {
      "return an empty transaction list" in withTransactions(List()) {
        transactions =>
          Get(s"/$apiVersion/transactions") ~> routes ~> check {
            responseAs[JsArray] shouldEqual JsArray()
          }
      }
      "return all transactions" in withUsers(4) { users =>
        withTransactions(users) { transactions =>
          Get(s"/$apiVersion/transactions") ~> routes ~> check {
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
                                              amount = 1,
                                              reason = "Ey, its just a test!")
          Post(
            s"/$apiVersion/transactions?auth=42",
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
      "fail" when {
        "not authorized" in withUsers(2) { users =>
          val transaction = CreateTransaction(from = users.head.id,
                                              to = users.tail.head.id,
                                              amount = 1,
                                              reason = "Ey, its just a test!")
          val exception = intercept[TestFailedException] {
            Post(
              s"/$apiVersion/transactions",
              transaction
            ) ~> routes ~> check {
              status shouldBe Unauthorized
            }
          }

          assert(exception.message.get.contains(
            "Request was rejected with rejection AuthorizationFailedRejection"))
        }
        "sender and receiver are the same" in withUsers(1) { users =>
          val transaction = CreateTransaction(from = users.head.id,
                                              to = users.head.id,
                                              amount = 1,
                                              reason = "Ey, its just a test!")
          Post(
            s"/$apiVersion/transactions?auth=42",
            transaction
          ) ~> routes ~> check {
            status shouldBe BadRequest
            assert(responseAs[String].contains(
              "Sender and Receiver cannot be the same!"))
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
