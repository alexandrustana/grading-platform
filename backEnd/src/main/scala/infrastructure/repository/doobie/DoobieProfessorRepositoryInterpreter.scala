package infrastructure.repository.doobie

import cats._
import cats.implicits._
import domain.professor.{Professor, ProfessorRepositoryAlgebra}
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object ProfessorSQL {

  def insert(professor: Professor): Update0 =
    sql"""
          INSERT INTO PROFESSOR(ID_ACCOUNT, TITLE)
          VALUES(${professor.account.id},
                  ${professor.title})
       """.update

  def selectAll: Query0[Professor] =
    sql"""
         SELECT *
         FROM PROFESSOR AS P
         INNER JOIN ACCOUNT AS A ON P.ID_ACCOUNT = A.ID
       """.query[(Professor)]
}

class DoobieProfessorRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F]) extends ProfessorRepositoryAlgebra[F] {
  import ProfessorSQL._

  override def create(o: Professor): F[Professor] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Professor]] = selectAll.to[List].transact(xa)
}

object DoobieProfessorRepositoryInterpreter {

  def apply[F[_]: Monad](xa: Transactor[F]): DoobieProfessorRepositoryInterpreter[F] =
    new DoobieProfessorRepositoryInterpreter(xa)
}
