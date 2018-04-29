package domain.assignment

import cats.Monad
import cats.data.EitherT
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AssignmentService[F[_]](assignmentRepo: AssignmentRepositoryAlgebra[F],
                              validation: AssignmentValidationAlgebra[F])
    extends GenericService[F, Assignment] {

  override def create(o: Assignment)(
      implicit M: Monad[F]): EitherT[F, ValidationError, Assignment] =
    for {
      _ <- validation.checkModel(o)
      saved <- EitherT.liftF(assignmentRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Assignment]] =
    EitherT.liftF(assignmentRepo.getAll)
}

object AssignmentService {
  def apply[F[_]](
      repositoryAlgebra: AssignmentRepositoryAlgebra[F],
      validationAlgebra: AssignmentValidationAlgebra[F]): AssignmentService[F] =
    new AssignmentService[F](repositoryAlgebra, validationAlgebra)
}
