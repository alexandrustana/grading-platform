package infrastructure.repository.elastic

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http._
import domain.submission.{Submission, SubmissionRepositoryAlgebra}
import util.MapT._

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 03/05/2018
  */
class ElasticSubmissionRepositoryInterpreter[F[_]: Monad](edb: HttpClient) extends SubmissionRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def create(o: Submission): F[Submission] =
    edb.execute {
      index("submission" -> "type") fields o
        .copy(id = Option(Random.nextLong))
        .toMap[Map[String, Any]]
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => o.pure[F]
    }

  override def getAll: F[List[Submission]] =
    edb.execute {
      search("submission")
    }.await match {
      case Left(e) => throw new Exception(e.error.reason)
      case Right(v) =>
        v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Submission]].pure[F]
    }
}

object ElasticSubmissionRepositoryInterpreter {

  def apply[F[_]: Monad](edb: HttpClient): ElasticSubmissionRepositoryInterpreter[F] =
    new ElasticSubmissionRepositoryInterpreter(edb)
}
