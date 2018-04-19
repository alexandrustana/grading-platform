package com.local.quickstart.domain.student

import com.local.quickstart.domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
trait StudentRepositoryAlgebra[F[_]] extends GenericRepositoryAlgebra[F, Student]{
}
