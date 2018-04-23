package domain

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait GenericRepositoryAlgebra[F[_], A] {
  def create(o: A): F[A]
  def getAll: F[List[A]]
}
