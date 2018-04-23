package domain.account

import cats.data.EitherT
import domain.{AlreadyExistsError, GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait AccountValidationAlgebra[F[_]]
    extends GenericValidationAlgebra[F, Account] {
  def doesNotExist(account: Account): EitherT[F, AlreadyExistsError, Unit]
  def checkModel(account: Account): EitherT[F, InvalidModelError, Unit]
}
