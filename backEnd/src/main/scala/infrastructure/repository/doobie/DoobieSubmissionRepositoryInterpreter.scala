package infrastructure.repository.doobie

import cats._
import cats.implicits._
import domain.submission.{Submission, SubmissionRepositoryAlgebra}
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 02/05/2018
  */
private object SubmissionSQL {

  def insert(submission: Submission): Update0 =
    sql"""
          INSERT INTO SUBMISSION(ID_STUDENT, ID_ASSIGNMENT, GRADE, TIME)
          VALUES(${submission.student.get.id}, ${submission.assignment.get.id}, ${submission.grade}, ${submission.time})
       """.update

  def selectAll: Query0[Submission] =
    sql"""
         SELECT *
         FROM SUBMISSION AS S
         INNER JOIN STUDENT AS A ON S.ID_STUDENT = A.ID
         INNER JOIN ASSIGNMENT AS T ON S.ASSIGNMENT = T.ID
       """.query[Submission]
}

class DoobieSubmissionRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F]) extends SubmissionRepositoryAlgebra[F] {
  import SubmissionSQL._

  override def create(o: Submission): F[Submission] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Submission]] = selectAll.to[List].transact(xa)
}

object DoobieSubmissionRepositoryInterpreter {

  def apply[F[_]: Monad](xa: Transactor[F]): DoobieSubmissionRepositoryInterpreter[F] =
    new DoobieSubmissionRepositoryInterpreter(xa)
}
