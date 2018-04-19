package com.local.quickstart

import cats.effect._
import com.local.quickstart.config.{DatabaseConfig, QuickstartConfig}
import com.local.quickstart.domain.account.{AccountService, AccountValidationInterpreter}
import com.local.quickstart.domain.assignment.{AssignmentService, AssignmentValidationInterpreter}
import com.local.quickstart.domain.course.{CourseService, CourseValidationInterpreter}
import com.local.quickstart.infrastructure.endpoint.{AccountEndpoints, AssignmentEndpoints, CourseEndpoints}
import com.local.quickstart.infrastructure.repository.doobie.{DoobieAccountRepositoryInterpreter, DoobieAssignmentRepositoryInterpreter, DoobieCourseRepositoryInterpreter}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
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
      conf <- Stream.eval(QuickstartConfig.load[F])
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
