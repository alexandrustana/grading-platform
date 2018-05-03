package domain.submission

import domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 03/05/2018
  */
trait SubmissionRepositoryAlgebra[F[_]] extends GenericRepositoryAlgebra[F, Submission] {}
