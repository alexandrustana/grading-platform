package domain

import cats.Monad
import cats.implicits._
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
class AccountGenerator[F[_]: Monad](val xa: Transactor[F]) {

  private type Account = (String, String, String, String)

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

  def generateEntries(n: Int): F[Int] =
    insertMany(data(n))
      .transact(xa)

}

object AccountGenerator {
  def apply[F[_]: Monad](xa: Transactor[F])(n: Int) = new AccountGenerator[F](xa).generateEntries(n)
}
