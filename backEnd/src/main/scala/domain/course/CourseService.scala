package domain.course

import cats.Monad
import cats.data.EitherT
import domain.{GenericService, ValidationError}

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class CourseService[F[_]](courseRepo: CourseRepositoryAlgebra[F], validation: CourseValidationAlgebra[F])
    extends GenericService[F, Course] {

  override def create(o: Course)(implicit M: Monad[F]): EitherT[F, ValidationError, Course] =
    for {
      _     <- validation.doesNotExist(o)
      _     <- validation.checkModel(o)
      saved <- EitherT.liftF(courseRepo.create(o))
    } yield saved

  override def getAll(implicit M: Monad[F]): EitherT[F, _, List[Course]] =
    EitherT.liftF(courseRepo.getAll)
}

object CourseService {

  def apply[F[_]](
    repositoryAlgebra: CourseRepositoryAlgebra[F],
    validationAlgebra: CourseValidationAlgebra[F]
  ): CourseService[F] =
    new CourseService[F](repositoryAlgebra, validationAlgebra)
}
