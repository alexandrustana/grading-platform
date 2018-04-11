package com.local.quickstart.infrastructure.repository.elastic

import cats.Monad
import cats.effect.Effect
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}
import com.sksamuel.elastic4s.http._
import io.circe._
import io.circe.generic.semiauto._
/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 11/04/2018
  */
class ElasticAccountRepositoryInterpreter[F[_]: Monad](edb: HttpClient)(implicit E: Effect[F])
    extends AccountRepositoryAlgebra[F] {

  import com.sksamuel.elastic4s.http.ElasticDsl._
  import io.circe.syntax._
  implicit val accountDecoder: Decoder[Account] = deriveDecoder[Account]
  implicit val accountEncoder: Encoder[Account] = deriveEncoder[Account]


  def byEmail(email: String): Account =
    edb.execute {
      search("account") query matchQuery("email", email)
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(v) => v.result.hits.hits(0).sourceAsString.asJson.as[Account] match {
        case Left(_) => throw new Exception
        case Right(value) => value
      }
    }

  override def findByEmail(email: String): F[Option[Account]] =
    E.pure(Some(byEmail(email)))

  override def create(o: Account): F[Account] =
    edb.execute {
      index("account" -> "type").fields(getCCParams(o))
    }.await match {
      case Left(e)  => throw new Exception(e.error.reason)
      case Right(_) => E.pure(byEmail(o.email))
    }

  private def getCCParams(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }
}

object ElasticAccountRepositoryInterpreter {
  def apply[F[_]: Monad](edb: HttpClient)(
      implicit E: Effect[F]): ElasticAccountRepositoryInterpreter[F] =
    new ElasticAccountRepositoryInterpreter(edb)
}
