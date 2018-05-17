package domain

import cats.Monad
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import tsec.passwordhashers.imports.BCrypt

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
class AccountGenerator[F[_]: Monad](val xa: Transactor[F]) {

  def generateAccounts = {
    val firstName = randomAlpha
    val lastName  = randomAlpha
    val email     = s"$randomAlpha@test.com"
    val password  = BCrypt.hashpwUnsafe(randomAlphaNumeric).repr
    sql"""INSERT INTO Account(first_name, last_name, email, password)
                                     VALUES ($firstName,
                                              $lastName,
                                              $email,
                                              $password)""".stripMargin.update
      .withGeneratedKeys[Long]("id")
      .compile
      .drain
      .transact(xa)
  }

  private def randomAlpha = {
    val chars = ('a' to 'z') ++ ('A' to 'Z')
    randomStringFromCharList(5, chars)
  }

  private def randomAlphaNumeric = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomStringFromCharList(5, chars)
  }

  private def randomStringFromCharList(length: Int, chars: Seq[Char]) = {
    val sb = new StringBuilder
    for (_ <- 1 to length) {
      val randomNum = Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
}

object AccountGenerator {
  def apply[F[_]: Monad](xa: Transactor[F]) = new AccountGenerator[F](xa)
}
