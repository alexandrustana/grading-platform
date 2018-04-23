package domain

import cats._
import cats.data._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait GenericService[F[_], A] {

  def create(o: A)(implicit M: Monad[F]): EitherT[F,_, A]

  def getAll(implicit M: Monad[F]): EitherT[F, _, List[A]]
}
