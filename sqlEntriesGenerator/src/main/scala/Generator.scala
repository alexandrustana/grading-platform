import cats.effect.{Effect,   IO}
import config.{BackendConfig, DatabaseConfig}
import domain._
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
      n    <- Stream.range(0, 10)
//      _    <- Stream.eval(AccountGenerator[F](xa)(1000))
//      _    <- Stream.eval(StudentGenerator[F](xa)(1 + 900 * n, 900 + 900 * n))
//      _    <- Stream.eval(ProfessorGenerator[F](xa)(901 + 1000 * n, 1000 + 1000 * n))
//      _    <- Stream.eval(CourseGenerator[F](xa)(100))
//      _    <- Stream.eval(AssignmentGenerator[F](xa)(1 + 100 * n, 100 + 100 * n))
//      _    <- Stream.eval(ProfessorCourseGenerator[F](xa)(1 + 100 * n, 100 + 100 * n, 1 + 100 * n, 100 + 100 * n))
      i    <- Stream.range(0, 10)
//      _ <- Stream.eval(
//            StudentCourseGenerator[F](xa)(
//              1 + 10 * i + 100 * n,
//              10 + 10 * i + 100 * n,
//              1 + 90 * i + 900 * n,
//              90 + 90 * i + 900 * n
//            )
//          )
      _ <- Stream.range(0, 10)
      _ <- Stream.eval(
            AssignmentSubmissionGenerator[F](xa)(
              1 + 10 * i + 100 * n,
              10 + 10 * i + 100 * n,
              1 + 90 * i + 900 * n,
              90 + 90 * i + 900 * n
            )
          )
    } yield ()

}
