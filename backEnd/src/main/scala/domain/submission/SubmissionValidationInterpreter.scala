package domain.submission

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import cats._
import cats.data.EitherT
import cats.implicits._
import domain.InvalidModelError
import util.Check._
import util.Predicate

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 03/05/2018
  */
class SubmissionValidationInterpreter[F[_]: Monad](submissionRepo: SubmissionRepositoryAlgebra[F])
    extends SubmissionValidationAlgebra[F] {

  override def checkModel(submission: Submission) =
    EitherT.fromEither {
      SubmissionValidationInterpreter.checkModel(submission) match {
        case Left(value) => Left(InvalidModelError(value.toList))
        case Right(_)    => Right(())
      }
    }
}

object SubmissionValidationInterpreter {

  def apply[F[_]: Monad](repo: SubmissionRepositoryAlgebra[F]): SubmissionValidationInterpreter[F] =
    new SubmissionValidationInterpreter[F](repo)

  def checkGrade: Check[Int, Int] =
    checkPred(
      Predicate.lift(
        error("The grade must must greater then or equal to 0 and less then or equal to 100"),
        grade => grade >= 0 && grade <= 100
      )
    )

  def checkDate: Check[LocalDate, LocalDate] =
    checkPred(
      Predicate.lift(
        error("The submission cannot be made in the past"),
        time => time.isAfter(LocalDate.now() minus (1, MINUTES))
      )
    )

  def checkModel(submission: Submission): Either[Errors, Submission] =
    /*_*/
    (
      Either.right(submission.id),
      Either.right(submission.student),
      Either.right(submission.assignment),
      checkGrade(submission.grade),
      checkDate(submission.time)
    ).mapN(Submission)
}
