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
abstract class CourseGenerator[F[_]: Monad] {

  protected type Course = String

  def generateEntries(n: Int): F[Int]
}

class SQLCourseGenerator[F[_]: Monad](val xa: Transactor[F]) extends CourseGenerator[F] {

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

class ESCourseGenerator[F[_]: Monad](val es: HttpClient) extends CourseGenerator[F] {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  private def data(n: Int) =
    (1 to n)
      .map(
        i => List(("id", i), ("name", RandomGenerator.alpha + " " + RandomGenerator.alphanumeric))
      )
      .toList

  override def generateEntries(n: Int): F[Int] = {
    data(n).map(e => {
      es.execute {
        index("course" -> "type") fields e
      }.await
    })
    1.pure[F]
  }

}

object CourseGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(n: Int) = new SQLCourseGenerator[F](xa).generateEntries(n)
  def apply[F[_]: Monad](es: HttpClient)(n:    Int) = new ESCourseGenerator[F](es).generateEntries(n)
}
