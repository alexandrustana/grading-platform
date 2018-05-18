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
abstract class ProfessorGenerator[F[_]: Monad] {
  protected type Professor = (Int, String)

  def generateEntries(from: Int, to: Int): F[Int]
}

class SQLProfessorGenerator[F[_]: Monad](val xa: Transactor[F]) extends ProfessorGenerator[F] {

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

class ESProfessorGenerator[F[_]: Monad](val es: HttpClient) extends StudentGenerator[F] {
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
          index("professor" -> "type") fields List(("id", i), ("id_account", e), ("title", "Prof."))
        }.await
      }
    }
    1.pure[F]
  }

}

object ProfessorGenerator {

  def apply[F[_]: Monad](xa: Transactor[F])(from: Int, to: Int) =
    new SQLProfessorGenerator[F](xa).generateEntries(from, to)

  def apply[F[_]: Monad](es: HttpClient)(from: Int, to: Int) =
    new ESProfessorGenerator[F](es).generateEntries(from, to)

}
