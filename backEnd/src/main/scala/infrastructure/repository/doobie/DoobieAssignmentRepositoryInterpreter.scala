package infrastructure.repository.doobie

import cats._
import cats.implicits._
import domain.assignment.{Assignment, AssignmentRepositoryAlgebra}
import domain.course.Course
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object AssignmentSQL {
  def insert(assignment: Assignment): Update0 =
    sql"""
          INSERT INTO ASSIGNMENT(ID_COURSE, NAME)
          VALUES(${assignment.course.id},
                  ${assignment.name})
       """.update

  def selectAll: Query0[(Assignment, Option[Course])] =
    sql"""
         SELECT A.ID, A.NAME
         FROM ACCOUNT AS A
         INNER JOIN COURSE AS C ON A.ID_COURSE = C.ID
       """.query[(Assignment, Option[Course])]
}

class DoobieAssignmentRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends AssignmentRepositoryAlgebra[F] {
  import AssignmentSQL._

  override def create(o: Assignment): F[Assignment] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Assignment]] =
    selectAll
      .to[List]
      .transact(xa)
      .map(_.map {
        case (a, o) =>
          o match {
            case Some(c) => a.copy(course = c)
            case None    => a
          }
      })
}

object DoobieAssignmentRepositoryInterpreter {
  def apply[F[_]: Monad](
      xa: Transactor[F]): DoobieAssignmentRepositoryInterpreter[F] =
    new DoobieAssignmentRepositoryInterpreter(xa)
}
