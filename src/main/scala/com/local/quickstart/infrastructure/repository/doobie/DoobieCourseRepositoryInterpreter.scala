package com.local.quickstart.infrastructure.repository.doobie

import cats._
import cats.implicits._
import com.local.quickstart.domain.course.{Course, CourseRepositoryAlgebra}
import doobie._
import doobie.implicits._

import scala.language.higherKinds

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
         SELECT ID, NAME
         FROM COURSE
       """.query[Course]
}

class DoobieCourseRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends CourseRepositoryAlgebra[F] {
  import CourseSQL._

  override def create(o: Course): F[Course] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def getAll: F[List[Course]] = selectAll.to[List].transact(xa)
}

object DoobieCourseRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieCourseRepositoryInterpreter[F] =
    new DoobieCourseRepositoryInterpreter(xa)
}
