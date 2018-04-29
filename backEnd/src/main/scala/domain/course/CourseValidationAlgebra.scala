package domain.course

import cats.data.EitherT
import domain.{AlreadyExistsError, GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait CourseValidationAlgebra[F[_]] extends GenericValidationAlgebra[F, Course] {
  def checkModel(course:   Course): EitherT[F, InvalidModelError,  Unit]
  def doesNotExist(course: Course): EitherT[F, AlreadyExistsError[Course], Unit]
}
