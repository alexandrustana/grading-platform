package domain.submission

import cats.data.EitherT
import domain.{GenericValidationAlgebra, InvalidModelError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 02/05/2018
  */
trait SubmissionValidationAlgebra[F[_]] extends GenericValidationAlgebra[F, Submission] {
  def checkModel(submission: Submission): EitherT[F, InvalidModelError, Unit]
}
