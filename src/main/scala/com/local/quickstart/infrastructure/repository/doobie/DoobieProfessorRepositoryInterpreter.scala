package com.local.quickstart.infrastructure.repository.doobie

import cats._
import cats.implicits._
import com.local.quickstart.domain.professor.{Professor, ProfessorRepositoryAlgebra}
import doobie._
import doobie.implicits._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object ProfessorSQL {
  def insert(professor: Professor): Update0 = ???

  def selectAll: Query0[Professor] = ???
}

class DoobieProfessorRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends ProfessorRepositoryAlgebra[F] {
  import ProfessorSQL._

  override def create(o: Professor): F[Professor] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Professor]] = ???
}

object DoobieProfessorRepositoryInterpreter{
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieProfessorRepositoryInterpreter[F] =
    new DoobieProfessorRepositoryInterpreter(xa)
}
