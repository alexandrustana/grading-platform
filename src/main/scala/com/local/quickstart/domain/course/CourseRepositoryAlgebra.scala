package com.local.quickstart.domain.course

import com.local.quickstart.domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
trait CourseRepositoryAlgebra[F[_]] extends GenericRepositoryAlgebra[F, Course]{
}
