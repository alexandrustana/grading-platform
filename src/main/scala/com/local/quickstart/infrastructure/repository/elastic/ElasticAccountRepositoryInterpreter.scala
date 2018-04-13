package com.local.quickstart.infrastructure.repository.elastic

import cats.Monad
import cats.effect.Effect
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}
import com.sksamuel.elastic4s.http._

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticAccountRepositoryInterpreter[F[_]: Monad](edb: HttpClient)(implicit E: Effect[F])
    extends AccountRepositoryAlgebra[F] {

  import com.local.quickstart.domain.util.MapT.{mapToOps, toMapOps}
  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def findByEmail(email: String): F[Option[Account]] =
    edb.execute {
      search("account") query matchQuery("email", email)
    }.await match {
      case Left(e) => throw new Exception(e.error.reason)
      case Right(v) =>
        E.pure(Option(v.result.hits.hits(0).sourceAsMap.mapTo[Account]))
    }

  override def create(o: Account): F[Account] =
    edb.execute {
      index("account" -> "type") fields o.toMap[Map[String, Any]]
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => E.pure(o)
    }
}

object ElasticAccountRepositoryInterpreter {
  def apply[F[_]: Monad](edb: HttpClient)(
      implicit E: Effect[F]): ElasticAccountRepositoryInterpreter[F] =
    new ElasticAccountRepositoryInterpreter(edb)
}
