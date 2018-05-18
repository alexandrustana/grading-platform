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
      _    <- Stream.eval(AccountGenerator[F](xa)(10))
      _    <- Stream.eval(StudentGenerator[F](xa)(1, 5))
      _    <- Stream.eval(ProfessorGenerator[F](xa)(6, 10))
      _    <- Stream.eval(CourseGenerator[F](xa)(10))
      _    <- Stream.eval(AssignmentGenerator[F](xa)(1, 10))
      _    <- Stream.eval(ProfessorCourseGenerator[F](xa)(1, 10, 1, 5))
      _    <- Stream.eval(StudentCourseGenerator[F](xa)(1, 10, 1, 5))
      _    <- Stream.eval(AssignmentSubmissionGenerator[F](xa)(1, 10, 1, 5))
    } yield ()

}
