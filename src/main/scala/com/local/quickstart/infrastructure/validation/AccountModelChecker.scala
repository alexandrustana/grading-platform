package com.local.quickstart.infrastructure.validation

import cats.implicits._
import com.local.quickstart.domain.account.Account
import com.local.quickstart.infrastructure.util.validation.Check.CheckOps._
import com.local.quickstart.infrastructure.util.validation.Check._
import com.local.quickstart.infrastructure.util.validation.Predicate

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 17/04/2018
  */
object AccountModelChecker {

  private val checkId = checkPred(
    Predicate.lift(error("Must be all numeric characters"), _ => true))

  private val checkName = checkPred(longerThan(3) and alpha)

  private val checkPassword = checkPred(
    longerThan(6) and
      Predicate.lift(error("Must contain at least an uppercase letter"),
                     str => str.exists(_.isUpper)) and
      Predicate.lift(error("Must contain at least a lowercase letter"),
                     str => str.exists(_.isLower)) and
      Predicate.lift(error("Must contain at least a symbol or number"),
                     str => str.exists(!_.isLetter)))

  private val checkEmail: Check[String, String] = {
    val splitEmail: Check[String, (String, String)] =
      check(_.split('@') match {
        case Array(name, domain) =>
          Right((name, domain))

        case _ =>
          Left(error("Must contain a single @ character"))
      })
    val checkLeft  = checkPred(longerThan(0))
    val checkRight = checkPred(longerThan(3) and contains('.'))
    val joinEmail: Check[(String, String), String] =
      check {
        case (l, r) => (checkLeft(l), checkRight(r)).mapN(_ + "@" + _)
      }

    splitEmail andThen joinEmail
  }

  def apply(account: Account): Either[Errors, Account] =
    (checkId.run(account.id),
     checkName.run(account.firstName),
     checkName.run(account.lastName),
     checkEmail.run(account.email),
     checkPassword.run(account.password)).mapN(Account)
}
