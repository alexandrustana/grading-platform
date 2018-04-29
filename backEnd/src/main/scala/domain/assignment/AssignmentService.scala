package domain.assignment

import cats.Monad
import cats.data.EitherT
import domain.course.CourseValidationAlgebra
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AssignmentService[F[_]](
  assignmentRepo:       AssignmentRepositoryAlgebra[F],
  courseValidation:     CourseValidationAlgebra[F],
  assignmentValidation: AssignmentValidationAlgebra[F]
) extends GenericService[F, Assignment] {

  override def create(o: Assignment)(implicit M: Monad[F]): EitherT[F, ValidationError, Assignment] =
    for {
      _     <- assignmentValidation.checkModel(o)
      _     <- courseValidation.doesExist(o.course.get)
      saved <- EitherT.liftF(assignmentRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Assignment]] =
    EitherT.liftF(assignmentRepo.getAll)
}

object AssignmentService {

  def apply[F[_]](
    assignmentRepositoryAlgebra: AssignmentRepositoryAlgebra[F],
    courseValidationAlgebra:     CourseValidationAlgebra[F],
    assignmentValidationAlgebra: AssignmentValidationAlgebra[F]
  ): AssignmentService[F] =
    new AssignmentService[F](assignmentRepositoryAlgebra, courseValidationAlgebra, assignmentValidationAlgebra)
}
