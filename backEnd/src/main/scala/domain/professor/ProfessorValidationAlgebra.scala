package domain.professor

import cats.data.EitherT
import domain.{GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 29/04/2018
  */
trait ProfessorValidationAlgebra[F[_]] extends GenericValidationAlgebra[F, Professor] {
  def checkModel(course: Professor): EitherT[F, InvalidModelError, Unit]
}
