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
class AssignmentGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Assignment = (Int, String)

  private def data(l: List[Int], r: List[Assignment] = List.empty[Assignment]): List[Assignment] =
    if (l.isEmpty) r
    else {
      val s = RandomGenerator.numeric(l)
      data(s._2, (s._1, RandomGenerator.alphanumeric) :: r)
    }

  private def insertMany(ps: List[Assignment]) =
    Update[Assignment](
      "INSERT INTO Assignment(id_course, name) VALUES (?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps)

  def generateEntries(from: Int, to: Int): F[Int] = {
    insertMany(data((from to to).toList))
      .transact(xa)
  }

}

object AssignmentGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) = new AssignmentGenerator[F](xa).generateEntries(from, to)
}
