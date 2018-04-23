package domain.professor

import domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
trait ProfessorRepositoryAlgebra[F[_]]
    extends GenericRepositoryAlgebra[F, Professor] {}
