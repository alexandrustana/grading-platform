package com.local.quickstart.infrastructure.repository.elastic

import cats.Monad
import cats.implicits._
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}
import com.sksamuel.elastic4s.http._
import tsec.passwordhashers.imports.BCrypt
import com.local.quickstart.infrastructure.util.MapT._

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticAccountRepositoryInterpreter[F[_]: Monad](edb: HttpClient)
    extends AccountRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  override def findByEmail(email: String): F[Option[Account]] =
    edb.execute {
      search("account") query matchQuery("email", email)
    }.await match {
      case Left(_) => Option.empty[Account].pure[F]
      case Right(v) =>
        val result = v.result.hits.hits
        if (result.length == 0) Option.empty[Account].pure[F]
        else Option(v.result.hits.hits(0).sourceAsMap.mapTo[Account]).pure[F]
    }

  override def create(o: Account): F[Account] =
    edb.execute {
      index("account" -> "type") fields o
        .copy(password = BCrypt.hashpwUnsafe(o.password).repr, id = Option(Random.nextLong))
        .toMap[Map[String, Any]]
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => o.pure[F]
    }

  override def getAll: F[List[Account]] =
    edb.execute {
      search("account")
    }.await match {
      case Left(e) => throw new Exception(e.error.reason)
      case Right(v) =>
        v.result.hits.hits.map(_.sourceAsMap).toList.mapTo[List[Account]].pure[F]
    }
}

object ElasticAccountRepositoryInterpreter {
  def apply[F[_]: Monad](edb: HttpClient): ElasticAccountRepositoryInterpreter[F] =
    new ElasticAccountRepositoryInterpreter(edb)
}
