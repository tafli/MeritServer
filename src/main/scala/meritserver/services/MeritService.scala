package meritserver.services

import meritserver.models.Merit
import meritserver.utils.Configuration

object MeritService {
  def getListOfMerits: List[Merit] =
    UserService.userAgent.get.map(user =>
      Merit(
        userId = user.id,
        teamId = user.teamId,
        name = s"${user.firstName} ${user.familyName}",
        received = user.balance + TransactionService.transactionAgent.get
          .filter(_.to == user.id)
          .filter(!_.booked)
          .foldLeft(0) { (acc, t) =>
            acc + t.amount
          },
        sent = TransactionService.transactionAgent.get
          .filter(_.from == user.id)
          .filter(!_.booked)
          .foldLeft(0) { (acc, t) =>
            acc + t.amount
          },
        available = TransactionService.meritStartAmount - TransactionService.transactionAgent.get
          .filter(_.from == user.id)
          .filter(!_.booked)
          .foldLeft(0) { (acc, t) =>
            acc + t.amount
          }
      ))
}

trait MeritService extends Configuration {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getMerits: List[Merit] = MeritService.getListOfMerits

  def getMeritsForTeam(teamId: String): List[Merit] = MeritService.getListOfMerits.filter(_.teamId == teamId)

  def payout(teamId: String, payoutThresholdParam: Option[Double]): List[Merit] = {

    math.random match {
      case i if i <= payoutThresholdParam.getOrElse(payoutThreshold) =>
        val listOfMerits = MeritService.getListOfMerits.filter(_.teamId == teamId)
        bookTransactions(teamId)
        UserService.userAgent.alter(_.filter(_.teamId == teamId).map(_.copy(balance = 0))).foreach {
          users =>
            DataAccessService.saveUsers(users)
        }
        listOfMerits
      case _ =>
        println("!!! Payout-Time !!!")
        UserService.userAgent
          .alter(
            _.filter(_.teamId == teamId)
              .map(
                user =>
                  user.copy(
                    balance = TransactionService.transactionAgent.get
                      .filter(_.to == user.id)
                      .filter(!_.booked)
                      .foldLeft(0) { (acc, t) =>
                        acc + t.amount
                      }
                  )))
          .foreach { users =>
            DataAccessService.saveUsers(users)
            bookTransactions(teamId)
          }
        List()
    }
  }

  def bookTransactions(teamId: String): Unit = {
    TransactionService.transactionAgent
      .alter(_.filter(t => UserService.getUserById(t.to).get.teamId == teamId).map(_.copy(booked = true)))
      .foreach { transactions =>
        DataAccessService.saveTransactions(transactions)
      }
  }
}
