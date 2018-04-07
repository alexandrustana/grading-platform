package com.local.quickstart.infrastructure.repository.doobie

import cats._
import cats.implicits._
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}
import doobie._
import doobie.implicits._

import scala.language.higherKinds

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
private object AccountSQL {
  def insert(account: Account): Update0 =
    sql"""
          INSERT INTO ACCOUNT(FIRST_NAME, LAST_NAME, EMAIL, PASSWORD)
          VALUES(${account.firstName},
                  ${account.lastName},
                  ${account.email},
                  ${sha256Hash(account.password)})
       """.update

  def byEmail(email: String): Query0[Account] =
    sql"""
         SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD
         FROM ACCOUNT
         WHERE EMAIL = $email
       """.query[Account]

  private def sha256Hash(text: String): String =
    String.format("%064x",
                  new java.math.BigInteger(1,
                                           java.security.MessageDigest
                                             .getInstance("SHA-256")
                                             .digest(text.getBytes("UTF-8"))))
}

class DoobieAccountRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends AccountRepositoryAlgebra[F] {
  import AccountSQL._

  override def create(o: Account): F[Account] =
    insert(o)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => o.copy(id = id.some))
      .transact(xa)

  override def findByEmail(email: String): F[Option[Account]] =
    byEmail(email).option.transact(xa)
}

object DoobieAccountRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieAccountRepositoryInterpreter[F] =
    new DoobieAccountRepositoryInterpreter(xa)
}
