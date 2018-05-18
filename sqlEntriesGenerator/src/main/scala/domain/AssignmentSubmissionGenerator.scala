package domain

import cats.Monad
import cats.implicits._
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import org.joda.time.DateTime
import util.RandomGenerator

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
class AssignmentSubmissionGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Relation = (Int, Int, Int, String)

  private def data(a: List[Int], s: List[Int], r: Set[Relation] = Set.empty[Relation]): Set[Relation] = {
    (for {
      _ <- 1 to a.length
      _ <- 1 to s.length
      ar = RandomGenerator.numeric(a)
      sr = RandomGenerator.numeric(s)

    } yield (ar, sr)).map {
      case (assignment, student) =>
        (
          student._1,
          assignment._1,
          Random.nextInt(100 + 1),
          DateTime.now().minusMonths(Random.nextInt(12)).toString("yyyy-MM-dd")
        )
    }.toSet
  }
  private def insertMany(ps: Set[Relation]) =
    Update[Relation](
      "INSERT INTO AssignmentSubmission(id_student, id_assignment, grade, time) VALUES (?, ?, ?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps.toList)

  def generateEntries(aFrom: Int, aTo: Int, sFrom: Int, sTo: Int): F[Int] = {
    insertMany(data((aFrom to aTo).toList, (sFrom to sTo).toList))
      .transact(xa)
  }

}

object AssignmentSubmissionGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(aFrom: Int, aTo: Int, sFrom: Int, sTo: Int) = new AssignmentSubmissionGenerator[F](xa).generateEntries(aFrom, aTo, sFrom, sTo)
}
