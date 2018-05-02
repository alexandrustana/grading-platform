package infrastructure.repository.doobie

import cats._
import cats.implicits._
import domain.student.{Student, StudentRepositoryAlgebra}
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object StudentSQL {

  def insert(student: Student): Update0 =
    sql"""
          INSERT INTO STUDENT(ID_ACCOUNT)
          VALUES(${student.account.get.id})
       """.update

  def selectAll: Query0[Student] =
    sql"""
         SELECT *
         FROM STUDENT AS S
         INNER JOIN ACCOUNT AS A ON S.ID_ACCOUNT = A.ID
       """.query[Student]
}

class DoobieStudentRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F]) extends StudentRepositoryAlgebra[F] {
  import StudentSQL._

  override def create(o: Student): F[Student] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Student]] = selectAll.to[List].transact(xa)
}

object DoobieStudentRepositoryInterpreter {

  def apply[F[_]: Monad](xa: Transactor[F]): DoobieStudentRepositoryInterpreter[F] =
    new DoobieStudentRepositoryInterpreter(xa)
}
