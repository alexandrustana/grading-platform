package com.local.quickstart.domain.assignment

import com.local.quickstart.domain.course.Course

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
final case class Assignment(id: Option[Long] = None, name: String, course: Course)
