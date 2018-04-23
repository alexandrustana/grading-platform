package domain.account

import domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
trait AccountRepositoryAlgebra[F[_]]
    extends GenericRepositoryAlgebra[F, Account] {
  def findByEmail(email: String): F[Option[Account]]
}
