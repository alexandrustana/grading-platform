package infrastructure.repository.elastic

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http._
import domain.assignment.{Assignment, AssignmentRepositoryAlgebra}
import util.MapT._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticAssignmentRepositoryInterpreter[F[_]: Monad](edb: HttpClient) extends AssignmentRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def create(o: Assignment): F[Assignment] =
    edb.execute {
      index("assignment" -> "type") fields o
        .toMap[Map[String, Any]]
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => o.pure[F]
    }

  override def getAll: F[List[Assignment]] =
    edb.execute {
      search("assignment")
    }.await match {
      case Left(e) => throw new Exception(e.error.reason)
      case Right(v) =>
        v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Assignment]].pure[F]
    }
}

object ElasticAssignmentRepositoryInterpreter {

  def apply[F[_]: Monad](edb: HttpClient): ElasticAssignmentRepositoryInterpreter[F] =
    new ElasticAssignmentRepositoryInterpreter(edb)
}
