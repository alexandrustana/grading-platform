package domain

import cats.Monad
import cats.implicits._
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import util.RandomGenerator

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
class CourseGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Course = String

  private def data(n: Int): List[Course] =
    (1 to n)
      .map(
        _ => RandomGenerator.alpha + " " + RandomGenerator.alphanumeric
      )
      .toList

  private def insertMany(ps: List[Course]) =
    Update[Course](
      "INSERT INTO Course(name) VALUES (?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps)

  def generateEntries(n: Int): F[Int] =
    insertMany(data(n))
      .transact(xa)

}

object CourseGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(n: Int) = new CourseGenerator[F](xa).generateEntries(n)
}
