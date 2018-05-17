import cats.effect.{Effect,   IO}
import config.{BackendConfig, DatabaseConfig}
import domain.AccountGenerator
import fs2.Stream

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 10/05/2018
  */
object Generator extends App {

  generateEntries[IO].compile.toVector.unsafeRunSync()

  private def generateEntries[F[_]](implicit E: Effect[F]) =
    for {
      conf <- Stream.eval(BackendConfig.load[F])
      xa   <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
      _    <- Stream.eval(DatabaseConfig.initializeSQLDb(conf.db, xa))
      _    <- Stream.range(0, 100)
      acc  <- Stream.eval(AccountGenerator[F](xa).generateAccounts)
    } yield acc

}
