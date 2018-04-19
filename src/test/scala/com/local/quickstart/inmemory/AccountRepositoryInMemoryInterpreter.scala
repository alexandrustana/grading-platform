package com.local.quickstart.inmemory

import cats.Applicative
import cats.implicits._
import com.local.quickstart.domain.account.{Account, AccountRepositoryAlgebra}

import scala.collection.concurrent.TrieMap
import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
class AccountRepositoryInMemoryInterpreter[F[_]: Applicative] extends AccountRepositoryAlgebra[F]{

  private val cache = new TrieMap[Long, Account]

  private val random = new Random


  def create(o: Account): F[Account] = {
    val id = random.nextLong
    val toSave = o.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  override def findByEmail(email: String): F[Option[Account]] = cache.values.find(o => o.email == email).pure[F]

  override def getAll: F[List[Account]] = cache.values.toList.pure[F]
}

object AccountRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() = new AccountRepositoryInMemoryInterpreter[F]
}