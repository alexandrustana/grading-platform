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
class ProfessorGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Professor = (Int, String)

  private def data(l: List[Int], r: List[Professor] = List.empty[Professor]): List[Professor] =
    if (l.isEmpty) r
    else {
      val s = RandomGenerator.numeric(l)
      data(s._2, (s._1, "Prof.") :: r)
    }

  private def insertMany(ps: List[Professor]) =
    Update[Professor](
      "INSERT INTO Professor(id_account, title) VALUES (?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps)

  def generateEntries(from: Int, to: Int): F[Int] = {
    insertMany(data((from to to).toList))
      .transact(xa)
  }

}

object ProfessorGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) = new ProfessorGenerator[F](xa).generateEntries(from, to)
}
