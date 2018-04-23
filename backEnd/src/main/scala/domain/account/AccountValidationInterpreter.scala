package domain.account

import cats._
import cats.data.EitherT
import cats.implicits._
import domain.{AlreadyExistsError, InvalidModelError}
import util.Check._
import util.Check.CheckOps._
import util.Predicate

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AccountValidationInterpreter[F[_]: Monad](
    accountRepo: AccountRepositoryAlgebra[F])
    extends AccountValidationAlgebra[F] {
  override def doesNotExist(account: Account) = EitherT {
    accountRepo.findByEmail(account.email).map {
      case None    => Right(())
      case Some(_) => Left(AlreadyExistsError(account))
    }
  }

  override def checkModel(account: Account) =
    EitherT.fromEither {
      AccountValidationInterpreter.checkModel(account) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object AccountValidationInterpreter {
  def apply[F[_]: Monad](
      repo: AccountRepositoryAlgebra[F]): AccountValidationInterpreter[F] =
    new AccountValidationInterpreter[F](repo)

  private val checkName = checkPred(longerThan(3)("Name") and alpha)

  private val checkPassword = checkPred(
    longerThan(6)("Password") and
      Predicate.lift(
        error("The password must contain at least an uppercase letter"),
        str => str.exists(_.isUpper)) and
      Predicate.lift(
        error("The password must  contain at least a lowercase letter"),
        str => str.exists(_.isLower)) and
      Predicate.lift(
        error("The password must  contain at least a symbol or a number"),
        str => str.exists(!_.isLetter)))

  private val checkEmail: Check[String, String] = {
    val splitEmail: Check[String, (String, String)] =
      check(_.split('@') match {
        case Array(name, domain) =>
          Right((name, domain))

        case _ =>
          Left(error("Email must contain a single @ character"))
      })
    val checkLeft = checkPred(longerThan(0)("Email"))
    val checkRight = checkPred(longerThan(3)("Email") and contains('.'))
    val joinEmail: Check[(String, String), String] =
      check {
        case (l, r) => (checkLeft(l), checkRight(r)).mapN(_ + "@" + _)
      }

    splitEmail andThen joinEmail
  }

  def checkModel(account: Account): Either[Errors, Account] =
    (Either.right(account.id),
     checkName(account.firstName),
     checkName(account.lastName),
     checkEmail(account.email),
     checkPassword(account.password)).mapN(Account)
}
