package domain

import cats.Monad
import cats.implicits._
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import util.RandomGenerator

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
class StudentCourseGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Relation = (Int, Int, Int)

  private def data(c: List[Int], s: List[Int], r: Set[Relation] = Set.empty[Relation]): Set[Relation] = {
    val a = for {
      _ <- 1 to c.length
      _ <- 1 to s.length
      cr = RandomGenerator.numeric(c)
      sr = RandomGenerator.numeric(s)

    } yield (cr, sr)
    a.map { case (course, student) => (student._1, course._1, Random.nextInt(100 + 1)) }.toSet
  }
  private def insertMany(ps: Set[Relation]) =
    Update[Relation](
      "INSERT INTO Student_Course(id_student, id_course, grade) VALUES (?, ?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps.toList)

  def generateEntries(cFrom: Int, cTo: Int, sFrom: Int, sTo: Int): F[Int] = {
    insertMany(data((cFrom to cTo).toList, (sFrom to sTo).toList))
      .transact(xa)
  }

}

object StudentCourseGenerator {
  def apply[F[_]: Monad](xa: Transactor[F]) = new StudentCourseGenerator[F](xa)
}
