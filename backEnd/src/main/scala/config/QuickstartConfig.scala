package config

import cats.effect.Effect
import cats.implicits._
import pureconfig.error.ConfigReaderException

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 06/04/2018
  */
case class QuickstartConfig(db: DatabaseConfig)

object QuickstartConfig {

  import pureconfig._

  def load[F[_]](implicit E: Effect[F]): F[QuickstartConfig] =
    E.delay(loadConfig[QuickstartConfig]("quickstart")).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[QuickstartConfig](e))
    }
}

