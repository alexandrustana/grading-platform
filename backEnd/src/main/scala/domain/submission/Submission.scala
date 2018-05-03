package domain.submission

import java.time.LocalDate

import domain.assignment.Assignment
import domain.student.Student

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 02/05/2018
  */
final case class Submission(
  id:         Option[Long] = None,
  student:    Option[Student] = None,
  assignment: Option[Assignment] = None,
  grade:      Int,
  time:       Long
)
