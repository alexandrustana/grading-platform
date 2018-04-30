import cats.effect._
import config.{BackendConfig,                DatabaseConfig}
import domain.account.{AccountService,       AccountValidationInterpreter}
import domain.assignment.{AssignmentService, AssignmentValidationInterpreter}
import domain.course.{CourseService,         CourseValidationInterpreter}
import domain.professor.{ProfessorService,   ProfessorValidationInterpreter}
import domain.student.{StudentService,       StudentValidationInterpreter}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import infrastructure.endpoint._
import infrastructure.repository.doobie._
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
      sqlProfessorRepo     = DoobieProfessorRepositoryInterpreter[F](xa)
      sqlStudentRepo       = DoobieStudentRepositoryInterpreter[F](xa)
      sqlCourseRepo        = DoobieCourseRepositoryInterpreter[F](xa)
      sqlAssignmentRepo    = DoobieAssignmentRepositoryInterpreter[F](xa)
      accountValidation    = AccountValidationInterpreter[F](sqlAccountRepo)
      professorValidation  = ProfessorValidationInterpreter[F](sqlProfessorRepo)
      studentValidation    = StudentValidationInterpreter[F](sqlStudentRepo)
      courseValidation     = CourseValidationInterpreter[F](sqlCourseRepo)
      assignmentValidation = AssignmentValidationInterpreter[F](sqlAssignmentRepo)
      accountService       = AccountService[F](sqlAccountRepo, accountValidation)
      professorService     = ProfessorService[F](sqlProfessorRepo, professorValidation)
      studentService       = StudentService[F](sqlStudentRepo, studentValidation)
      courseService        = CourseService[F](sqlCourseRepo, courseValidation)
      assignmentService    = AssignmentService[F](sqlAssignmentRepo, courseValidation, assignmentValidation)
      exitCode <- BlazeBuilder[F]
                   .bindHttp(8080, "localhost")
                   .mountService(AccountEndpoints(accountService), "/")
                   .mountService(ProfessorEndpoints(professorService), "/")
                   .mountService(StudentEndpoints(studentService), "/")
                   .mountService(CourseEndpoints(courseService), "/")
                   .mountService(AssignmentEndpoints(assignmentService), "/")
                   .serve
    } yield exitCode
}
