package domain.student

import cats.data.EitherT
import domain.{GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 30/04/2018
  */
trait StudentValidationAlgebra[F[_]] extends GenericValidationAlgebra[F, Student] {
  def checkModel(course: Student): EitherT[F, InvalidModelError, Unit]
}
