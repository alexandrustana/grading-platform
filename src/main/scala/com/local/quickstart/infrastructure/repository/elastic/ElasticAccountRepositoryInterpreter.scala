package com.local.quickstart.infrastructure.repository.elastic

import cats.Monad
import cats.effect.Effect
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}
import com.sksamuel.elastic4s.http._
import tsec.passwordhashers.imports.BCrypt
import com.local.quickstart.infrastructure.util.MapT._

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticAccountRepositoryInterpreter[F[_]: Monad](edb: HttpClient)(implicit E: Effect[F])
    extends AccountRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def findByEmail(email: String): F[Option[Account]] =
    edb.execute {
      search("account") query matchQuery("email", email)
    }.await match {
      case Left(_) => E.pure(None)
      case Right(v) => {
        val result = v.result.hits.hits
        if(result.length == 0) E.pure(None)
        else E.pure(Option(v.result.hits.hits(0).sourceAsMap.mapTo[Account]))
      }
    }

  override def create(o: Account): F[Account] =
    edb.execute {
      index("account" -> "type") fields o
        .copy(password = BCrypt.hashpwUnsafe(o.password).repr, id = Option(Random.nextLong))
        .toMap[Map[String, Any]]
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => E.pure(o)
    }

  override def getAll: F[List[Account]] =
    edb.execute {
      search("account")
    }.await match {
      case Left(e) => throw new Exception(e.error.reason)
      case Right(v) =>
        E.pure(v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Account]])
    }
}

object ElasticAccountRepositoryInterpreter {
  def apply[F[_]: Monad](edb: HttpClient)(
      implicit E: Effect[F]): ElasticAccountRepositoryInterpreter[F] =
    new ElasticAccountRepositoryInterpreter(edb)
}
