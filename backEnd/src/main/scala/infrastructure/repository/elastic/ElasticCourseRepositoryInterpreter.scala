package infrastructure.repository.elastic

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http._
import domain.course.{Course, CourseRepositoryAlgebra}
import util.MapT._

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 30/04/2018
  */
class ElasticCourseRepositoryInterpreter[F[_]: Monad](edb: HttpClient) extends CourseRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def findByName(name: String): F[Option[Course]] =
    edb.execute {
      search("course") query matchQuery("name", name)
    }.await match {
      case Left(_) => Option.empty[Course].pure[F]
      case Right(v) =>
        val result = v.result.hits.hits
        if (result.length == 0) Option.empty[Course].pure[F]
        else Option(v.result.hits.hits(0).sourceAsMap.mapTo[Course]).pure[F]
    }

  override def create(o: Course): F[Course] =
    edb.execute {
      index("course" -> "type") fields o
        .copy(id = Option(Random.nextLong))
        .toMap[Map[String, Any]]
    }.await match {
      case Right(_) => o.pure[F]
    }

  override def getAll: F[List[Course]] =
    edb.execute {
      search("course")
    }.await match {
      case Right(v) =>
        v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Course]].pure[F]
    }
}

object ElasticCourseRepositoryInterpreter {

  def apply[F[_]: Monad](edb: HttpClient): ElasticCourseRepositoryInterpreter[F] =
    new ElasticCourseRepositoryInterpreter(edb)
}
