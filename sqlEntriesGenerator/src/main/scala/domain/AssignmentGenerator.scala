package domain

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http.HttpClient
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import util.RandomGenerator

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
abstract class AssignmentGenerator[F[_]: Monad] {

  protected type Assignment = (Int, String)

  def generateEntries(from: Int, to: Int): F[Int]

}

class SQLAssignmentGenerator[F[_]: Monad](val xa: Transactor[F]) extends AssignmentGenerator[F] {

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

  override def generateEntries(from: Int, to: Int): F[Int] = {
    insertMany(data((from to to).toList))
      .transact(xa)
  }

}

class ESAssignmentGenerator[F[_]: Monad](val es: HttpClient) extends AssignmentGenerator[F] {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  private def data(l: List[Int], r: List[Assignment] = List.empty[Assignment]): List[Assignment] =
    if (l.isEmpty) r
    else {
      val s = RandomGenerator.numeric(l)
      data(s._2, (s._1, RandomGenerator.alphanumeric) :: r)
    }

  override def generateEntries(from: Int, to: Int): F[Int] = {
    data((from to to).toList).zipWithIndex.map {
      case (e, i) => {
        es.execute {
          index("assignment" -> "type") fields List(("id", i), ("id_course", e), ("name", RandomGenerator.alphanumeric))
        }.await
      }
    }
    1.pure[F]
  }

}

object AssignmentGenerator {

  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) =
    new SQLAssignmentGenerator[F](xa).generateEntries(from, to)

  def apply[F[_]: Monad](es: HttpClient)(from: Int, to: Int) =
    new ESAssignmentGenerator[F](es).generateEntries(from, to)
}
