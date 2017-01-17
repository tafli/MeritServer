package meritserver.services

import meritserver.models.Merit
import meritserver.utils.Configuration

object MeritService {
  def getListOfMerits: List[Merit] = UserService.userAgent.get.map(user =>
    Merit(userId = user.id, name = s"${user.firstName} ${user.familyName}",
      amount = user.balance + TransactionService.transactionAgent.get.filter(_.to == user.id).filter(_.booked == false).foldLeft(0) { (acc, t) => acc + t.amount }
    )
  )
}

trait MeritService extends Configuration {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getMerits: List[Merit] = MeritService.getListOfMerits

  def payout(): List[Merit] = {
    math.random match {
      case i if i <= payoutThreshold =>
        val listOfMerits = MeritService.getListOfMerits
        bookTransaction()
        UserService.userAgent.alter(_.map(_.copy(balance = 0))).onSuccess {
          case users => DataAccessService.saveUsers(users)
        }
        listOfMerits

      case _ =>
        UserService.userAgent.alter(_.map(user => user.copy(
          balance = TransactionService.transactionAgent.get.filter(_.to == user.id).filter(_.booked == false).foldLeft(0) { (acc, t) => acc + t.amount }
        ))).onSuccess {
          case users => DataAccessService.saveUsers(users)
        }
        bookTransaction()
        List()
    }
  }

  def bookTransaction(): Unit = {
    TransactionService.transactionAgent.alter(_.map(_.copy(booked = true))).onSuccess {
      case transactions => DataAccessService.saveTransactions(transactions)
    }
  }
}
