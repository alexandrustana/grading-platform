package com.local.quickstart.domain.account

import cats.data.EitherT
import com.local.quickstart.domain.{AccountAlreadyExistsError, AccountInvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait AccountValidationAlgebra[F[_]] {
  def doesNotExist(account: Account): EitherT[F, AccountAlreadyExistsError, Unit]
  def checkModel(account: Account): EitherT[F, AccountInvalidModelError, Unit]
}
