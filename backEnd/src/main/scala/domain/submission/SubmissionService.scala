package domain.submission

import cats.Monad
import cats.data.EitherT
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 03/05/2018
  */
class SubmissionService[F[_]](studentRepo: SubmissionRepositoryAlgebra[F], validation: SubmissionValidationAlgebra[F])
    extends GenericService[F, Submission] {

  override def create(o: Submission)(implicit M: Monad[F]): EitherT[F, ValidationError, Submission] =
    for {
      _     <- validation.checkModel(o)
      saved <- EitherT.liftF(studentRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Submission]] =
    EitherT.liftF(studentRepo.getAll)
}

object SubmissionService {

  def apply[F[_]](
    repositoryAlgebra: SubmissionRepositoryAlgebra[F],
    validationAlgebra: SubmissionValidationAlgebra[F]
  ): SubmissionService[F] =
    new SubmissionService[F](repositoryAlgebra, validationAlgebra)
}
