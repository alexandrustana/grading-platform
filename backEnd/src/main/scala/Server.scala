import cats.effect._
import config.{BackendConfig, DatabaseConfig}
import domain.account.{AccountService, AccountValidationInterpreter}
import domain.assignment.{AssignmentService, AssignmentValidationInterpreter}
import domain.course.{CourseService, CourseValidationInterpreter}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import infrastructure.endpoint.{AccountEndpoints, AssignmentEndpoints, CourseEndpoints}
import infrastructure.repository.doobie.{DoobieAccountRepositoryInterpreter, DoobieAssignmentRepositoryInterpreter, DoobieCourseRepositoryInterpreter}
import org.http4s.server.blaze.BlazeBuilder

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 04/04/2018
  */
object Server extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO]

  def createStream[F[_]](implicit E: Effect[F]): Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(BackendConfig.load[F])
      xa   <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
      _    <- Stream.eval(DatabaseConfig.initializeSQLDb(conf.db, xa))
      sqlAccountRepo       = DoobieAccountRepositoryInterpreter[F](xa)
      sqlCourseRepo        = DoobieCourseRepositoryInterpreter[F](xa)
      sqlAssignmentRepo    = DoobieAssignmentRepositoryInterpreter[F](xa)
      accountValidation    = AccountValidationInterpreter[F](sqlAccountRepo)
      courseValidation     = CourseValidationInterpreter[F](sqlCourseRepo)
      assignmentValidation = AssignmentValidationInterpreter[F](sqlAssignmentRepo)
      accountService       = AccountService[F](sqlAccountRepo, accountValidation)
      courseService        = CourseService[F](sqlCourseRepo, courseValidation)
      assignmentService    = AssignmentService[F](sqlAssignmentRepo, assignmentValidation)
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(AccountEndpoints(accountService), "/")
        .mountService(CourseEndpoints(courseService), "/")
        .mountService(AssignmentEndpoints(assignmentService), "/")
        .serve
    } yield exitCode
}
