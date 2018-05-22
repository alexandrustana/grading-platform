package domain.course

import domain.GenericRepositoryAlgebra

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
trait CourseRepositoryAlgebra[F[_]] extends GenericRepositoryAlgebra[F, Course] {
  def findByName(name: String): F[Option[Course]]
  def getGrades: F[List[(Double, String)]]
}
