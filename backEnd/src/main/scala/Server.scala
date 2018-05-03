import cats.Monad
import cats.effect._
import com.sksamuel.elastic4s.http.HttpClient
import config.{BackendConfig,                DatabaseConfig}
import domain.account.{AccountService,       AccountValidationInterpreter}
import domain.assignment.{AssignmentService, AssignmentValidationInterpreter}
import domain.course.{CourseService,         CourseValidationInterpreter}
import domain.professor.{ProfessorService,   ProfessorValidationInterpreter}
import domain.student.{StudentService,       StudentValidationInterpreter}
import domain.submission.{SubmissionService, SubmissionValidationInterpreter}
import doobie.hikari.HikariTransactor
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import infrastructure.endpoint._
import infrastructure.repository.doobie._
import infrastructure.repository.elastic._
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
      es   <- Stream.eval(DatabaseConfig.initializeElasticDb(conf.db))
      _    <- Stream.eval(DatabaseConfig.initializeSQLDb(conf.db, xa))
      accountRepo          = accountRepository[F](Right(es))
      professorRepo        = professorRepository[F](Right(es))
      studentRepo          = studentRepository[F](Right(es))
      courseRepo           = courseRepository[F](Right(es))
      assignmentRepo       = assignmentRepository[F](Right(es))
      submissionRepo       = submisssionRepository[F](Right(es))
      accountValidation    = AccountValidationInterpreter[F](accountRepo)
      professorValidation  = ProfessorValidationInterpreter[F](professorRepo)
      studentValidation    = StudentValidationInterpreter[F](studentRepo)
      courseValidation     = CourseValidationInterpreter[F](courseRepo)
      assignmentValidation = AssignmentValidationInterpreter[F](assignmentRepo)
      submissionValidation = SubmissionValidationInterpreter[F](submissionRepo)
      accountService       = AccountService[F](accountRepo, accountValidation)
      professorService     = ProfessorService[F](professorRepo, professorValidation)
      studentService       = StudentService[F](studentRepo, studentValidation)
      courseService        = CourseService[F](courseRepo, courseValidation)
      assignmentService    = AssignmentService[F](assignmentRepo, courseValidation, assignmentValidation)
      submissionService    = SubmissionService[F](submissionRepo, submissionValidation)
      exitCode <- BlazeBuilder[F]
                   .bindHttp(8080, "localhost")
                   .mountService(AccountEndpoints(accountService), "/")
                   .mountService(ProfessorEndpoints(professorService, accountService), "/")
                   .mountService(StudentEndpoints(studentService, accountService), "/")
                   .mountService(CourseEndpoints(courseService), "/")
                   .mountService(AssignmentEndpoints(assignmentService), "/")
                   .mountService(SubmissionEndpoints(submissionService), "/")
                   .serve
    } yield exitCode

  private def accountRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieAccountRepositoryInterpreter[F](xa)
    case Right(es) => ElasticAccountRepositoryInterpreter[F](es)
  }

  private def professorRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieProfessorRepositoryInterpreter[F](xa)
    case Right(es) => ElasticProfessorRepositoryInterpreter[F](es)
  }

  private def studentRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieStudentRepositoryInterpreter[F](xa)
    case Right(es) => ElasticStudentRepositoryInterpreter[F](es)
  }

  private def courseRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieCourseRepositoryInterpreter[F](xa)
    case Right(es) => ElasticCourseRepositoryInterpreter[F](es)
  }

  private def assignmentRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieAssignmentRepositoryInterpreter[F](xa)
    case Right(es) => ElasticAssignmentRepositoryInterpreter[F](es)
  }

  private def submisssionRepository[F[_]: Monad](conf: Either[HikariTransactor[F], HttpClient]) = conf match {
    case Left(xa)  => DoobieSubmissionRepositoryInterpreter[F](xa)
    case Right(es) => ElasticSubmissionRepositoryInterpreter[F](es)
  }
}
