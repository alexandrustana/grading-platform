package com.local.quickstart.domain.account

import cats._
import cats.data.EitherT
import cats.implicits._
import com.local.quickstart.domain.AccountAlreadyExistsError
import com.local.quickstart.infrastructure.util.validation.Check._
import com.local.quickstart.infrastructure.validation.AccountModelChecker

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AccountValidationInterpreter[F[_]: Monad](accountRepo: AccountRepositoryAlgebra[F])
    extends AccountValidationAlgebra[F] {
  def doesNotExist(account: Account) = EitherT {
    accountRepo.findByEmail(account.email).map {
      case None    => Right(())
      case Some(_) => Left(AccountAlreadyExistsError(account))
    }
  }

  override def checkModel(account: Account): EitherT[F, Errors, Account] =
    EitherT.fromEither(AccountModelChecker(account))
}

object AccountValidationInterpreter {
  def apply[F[_]: Monad](repo: AccountRepositoryAlgebra[F]): AccountValidationAlgebra[F] =
    new AccountValidationInterpreter[F](repo)
}
