package infrastructure.repository.elastic

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http._
import domain.professor.{Professor, ProfessorRepositoryAlgebra}
import util.MapT._

import scala.util.Random
/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticProfessorRepositoryInterpreter[F[_]: Monad](edb: HttpClient) extends ProfessorRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def create(o: Professor): F[Professor] =
    edb.execute {
      index("professor" -> "type") fields o
        .copy(id = Option(Random.nextLong))
        .toMap[Map[String, Any]]
    }.await match {
      case Right(_) => o.pure[F]
    }

  override def getAll: F[List[Professor]] =
    edb.execute {
      search("professor")
    }.await match {
      case Right(v) =>
        v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Professor]].pure[F]
    }
}

object ElasticProfessorRepositoryInterpreter {

  def apply[F[_]: Monad](edb: HttpClient): ElasticProfessorRepositoryInterpreter[F] =
    new ElasticProfessorRepositoryInterpreter(edb)
}
