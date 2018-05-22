package infrastructure.repository.doobie

import cats._
import cats.implicits._
import domain.course.{Course, CourseRepositoryAlgebra}
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object CourseSQL {

  def insert(course: Course): Update0 =
    sql"""
          INSERT INTO COURSE(NAME)
          VALUES(${course.name})
       """.update

  def selectAll: Query0[Course] =
    sql"""
         SELECT *
         FROM COURSE
       """.query[Course]

  def byName(name: String): Query0[Course] =
    sql"""
      SELECT *
      FROM COURSE
      WHERE NAME = $name
    """.query[Course]

  def getAverageGrades: Query0[(Double, String)] =
    sql"""
         |SELECT AVG(ASUB.`grade`) `AVERAGE GRADE`, C.`name` 
         |	FROM `Course` AS C
         |	INNER JOIN `Assignment` AS A ON A.`id_course` = C.`id`
         |	INNER JOIN `AssignmentSubmission` AS ASUB ON ASUB.`id_assignment` = A.`id`
         |	WHERE YEAR(ASUB.`time`) = YEAR(NOW())
         |	GROUP BY A.`id_course`
         |	ORDER BY `AVERAGE GRADE` DESC;
      """.stripMargin.query[(Double, String)]
}

class DoobieCourseRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F]) extends CourseRepositoryAlgebra[F] {
  import CourseSQL._

  override def create(o: Course): F[Course] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Course]] = selectAll.to[List].transact(xa)

  override def getGrades: F[List[(Double, String)]] = getAverageGrades.to[List].transact(xa)

  override def findByName(name: String): F[Option[Course]] = byName(name).option.transact(xa)
}

object DoobieCourseRepositoryInterpreter {

  def apply[F[_]: Monad](xa: Transactor[F]): DoobieCourseRepositoryInterpreter[F] =
    new DoobieCourseRepositoryInterpreter(xa)
}
