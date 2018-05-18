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
class ProfessorCourseGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Relation = (Int, Int)

  private def data(c: List[Int], p: List[Int], r: Set[Relation] = Set.empty[Relation]): Set[Relation] =
    if (p.isEmpty) r
    else {
      val pr = RandomGenerator.numeric(p)
      data(c, pr._2, r + ((RandomGenerator.numeric(c)._1, pr._1)))
    }

  private def insertMany(ps: Set[Relation]) =
    Update[Relation](
      "INSERT INTO Professor_Course(id_course, id_professor) VALUES (?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps.toList)

  def generateEntries(cFrom: Int, cTo: Int, pFrom: Int, pTo: Int): F[Int] = {
    insertMany(data((cFrom to cTo).toList, (pFrom to pTo).toList))
      .transact(xa)
  }

}

object ProfessorCourseGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(cFrom: Int, cTo: Int, pFrom: Int, pTo: Int) = new ProfessorCourseGenerator[F](xa).generateEntries(cFrom, cTo, pFrom, pTo)
}
