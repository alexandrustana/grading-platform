package domain.assignment

import domain.course.Course

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
final case class Assignment(id: Option[Long] = None,  course: Option[Course] = None, name: String)
