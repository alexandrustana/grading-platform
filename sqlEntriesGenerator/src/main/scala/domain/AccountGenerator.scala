package domain

import cats.Monad
import cats.implicits._
import com.sksamuel.elastic4s.http._
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import tsec.passwordhashers.imports.BCrypt
import util.RandomGenerator

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
abstract class AccountGenerator[F[_]: Monad] {

  protected type Account = (String, String, String, String)

  def generateEntries(n: Int): F[Int]
}

class SQLAccountGenerator[F[_]: Monad](val xa: Transactor[F]) extends AccountGenerator[F] {
  private def data(n: Int): List[Account] =
    (1 to n)
      .map(
        _ =>
          (
            RandomGenerator.alpha,
            RandomGenerator.alpha,
            s"${RandomGenerator.alpha}@test.com",
            BCrypt.hashpwUnsafe(RandomGenerator.alphanumeric).repr
        )
      )
      .toList

  private def insertMany(ps: List[Account]) =
    Update[Account](
      "INSERT INTO Account(first_name, last_name, email, password) VALUES (?, ?, ?, ?)",
      logHandler0 = LogHandler.jdkLogHandler
    ).updateMany(ps)

  override def generateEntries(n: Int): F[Int] =
    insertMany(data(n))
      .transact(xa)
}

class ESAccountGenerator[F[_]: Monad](val es: HttpClient) extends AccountGenerator[F] {
  import com.sksamuel.elastic4s.http.ElasticDsl._

  private def data(n: Int) =
    (1 to n)
      .map(
        i =>
          List(
            ("id",         i),
            ("first_name", RandomGenerator.alpha),
            ("last_name",  RandomGenerator.alpha),
            ("email",      s"${RandomGenerator.alpha}@test.com"),
            ("password",   BCrypt.hashpwUnsafe(RandomGenerator.alphanumeric).repr)
        )
      )
      .toList

  override def generateEntries(n: Int): F[Int] = {
    data(n).map(e => {
      es.execute {
        index("account" -> "type") fields e
      }.await
    })
    1.pure[F]
  }
}

object AccountGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(n: Int) = new SQLAccountGenerator[F](xa).generateEntries(n)
  def apply[F[_]: Monad](es: HttpClient)(n:    Int) = new ESAccountGenerator[F](es).generateEntries(n)
}
