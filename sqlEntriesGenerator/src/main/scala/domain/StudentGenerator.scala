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
class StudentGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Student = Int

  private def data(l: List[Student], r: List[Student] = List.empty[Student]): List[Student] =
    if (l.isEmpty) r
    else {
      val s = RandomGenerator.numeric(l)
      data(s._2, s._1 :: r)
    }

  private def insertMany(ps: List[Student]) =
    Update[Student](
      "INSERT INTO Student(id_account) VALUES (?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps)

  def generateEntries(from: Int, to: Int): F[Int] = {
    insertMany(data((from to to).toList))
      .transact(xa)
  }

}

object StudentGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) = new StudentGenerator[F](xa).generateEntries(from, to)
}
