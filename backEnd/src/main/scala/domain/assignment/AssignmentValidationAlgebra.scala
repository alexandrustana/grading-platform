package domain.assignment

import cats.data.EitherT
import domain.{GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait AssignmentValidationAlgebra[F[_]]
    extends GenericValidationAlgebra[F, Assignment] {
  def checkModel(assignment: Assignment): EitherT[F, InvalidModelError, Unit]
}
