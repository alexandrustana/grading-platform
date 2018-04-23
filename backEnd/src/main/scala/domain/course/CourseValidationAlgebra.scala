package domain.course

import cats.data.EitherT
import domain.{GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait CourseValidationAlgebra[F[_]]
    extends GenericValidationAlgebra[F, Course] {
  def checkModel(assignment: Course): EitherT[F, InvalidModelError, Unit]
}
