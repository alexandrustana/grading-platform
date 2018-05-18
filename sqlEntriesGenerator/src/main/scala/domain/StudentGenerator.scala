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
abstract class StudentGenerator[F[_]: Monad] {

  protected type Student = Int

  def generateEntries(from: Int, to: Int): F[Int]
}

class SQLStudentGenerator[F[_]: Monad](val xa: Transactor[F]) extends StudentGenerator[F] {

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

class ESStudentGenerator[F[_]: Monad](val es: HttpClient) extends StudentGenerator[F] {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  private def data(l: List[Student], r: List[Student] = List.empty[Student]): List[Student] =
    if (l.isEmpty) r
    else {
      val s = RandomGenerator.numeric(l)
      data(s._2, s._1 :: r)
    }

  override def generateEntries(from: Int, to: Int): F[Int] = {
    data((from to to).toList).zipWithIndex.map {
      case (e, i) => {
        es.execute {
          index("student" -> "type") fields List(("id", i), ("id_account", e))
        }.await
      }
    }
    1.pure[F]
  }

}

object StudentGenerator {

  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) =
    new SQLStudentGenerator[F](xa).generateEntries(from, to)

  def apply[F[_]: Monad](es: HttpClient)(from: Int, to: Int) =
    new ESStudentGenerator[F](es).generateEntries(from, to)

}
