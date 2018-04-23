package config

import cats.effect.{Async, Sync}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import doobie.hikari._
import org.flywaydb.core.Flyway

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 06/04/2018
  */
case class DatabaseConfig(host: String,
                          port: Int,
                          url: String,
                          driver: String,
                          user: String,
                          password: String,
                          clean: Boolean = false)

object DatabaseConfig {

  def dbTransactor[F[_]: Async](dbConfig: DatabaseConfig): F[HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](dbConfig.driver, dbConfig.url, dbConfig.user, dbConfig.password)

  def initializeSQLDb[F[_]](dbConfig: DatabaseConfig, xa: HikariTransactor[F])(
    implicit S: Sync[F]): F[Unit] =
    if (dbConfig.url.contains(":mysql:")) {
      xa.configure { ds =>
        S.delay {
          val fw = new Flyway()
          fw.setDataSource(ds)
          if (dbConfig.clean) fw.clean()
          fw.migrate()
          ()
        }
      }
    } else {
      S.pure(())
    }

  def initializeElasticDb[F[_]](dbConfig: DatabaseConfig)(implicit S: Sync[F]): F[HttpClient] = S.delay {
    HttpClient(ElasticsearchClientUri(dbConfig.host, dbConfig.port))
  }
}
