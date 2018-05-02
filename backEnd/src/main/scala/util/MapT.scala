package util

import domain.account.Account
import domain.assignment.Assignment
import domain.course.Course
import domain.professor.Professor
import domain.student.Student

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 12/04/2018
  */
trait MapT[A, B] {
  def toMap(a: A): B
}

object MapT {

  implicit class toMapOps[A](a: A) {
    def toMap[B](implicit m: MapT[A, B]): B = m.toMap(a)
  }

  implicit class mapToOps[A](a: A) {
    def mapTo[B](implicit m: MapT[A, B]): B = m.toMap(a)
  }
  implicit val accountToMap: MapT[Account, Map[String, Any]] = (a: Account) =>
    Map(
      "id"        -> a.id.get,
      "firstName" -> a.firstName,
      "lastName"  -> a.lastName,
      "email"     -> a.email,
      "password"  -> a.password
  )

  implicit val mapToAccount: MapT[Map[String, AnyRef], Account] =
    (a: Map[String, AnyRef]) =>
      Account(
        Option(a("id").toString.toLong),
        a("firstName").toString,
        a("lastName").toString,
        a("email").toString,
        a("password").toString
    )

  implicit val mapToAccountList: MapT[List[Map[String, AnyRef]], List[Account]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Account])

  implicit val professorToMap: MapT[Professor, Map[String, Any]] = (a: Professor) =>
    Map("id" -> a.id.get, "title" -> a.title, "account" -> a.account.get.toMap[Map[String, Any]])

  implicit val mapToProfessor: MapT[Map[String, AnyRef], Professor] =
    (a: Map[String, AnyRef]) =>
      Professor(
        Option(a("id").toString.toLong),
        Option(a("account").asInstanceOf[Map[String, AnyRef]].mapTo[Account]),
        a("title").toString
    )

  implicit val mapToProfessorList: MapT[List[Map[String, AnyRef]], List[Professor]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Professor])

  implicit val studentToMap: MapT[Student, Map[String, Any]] = (a: Student) =>
    Map("id" -> a.id.get, "account" -> a.account.get.toMap[Map[String, Any]])

  implicit val mapToStudent: MapT[Map[String, AnyRef], Student] =
    (a: Map[String, AnyRef]) =>
      Student(
        Option(a("id").toString.toLong),
        Option(a("account").asInstanceOf[Map[String, AnyRef]].mapTo[Account])
    )

  implicit val mapToStudentList: MapT[List[Map[String, AnyRef]], List[Student]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Student])

  implicit val courseToMap: MapT[Course, Map[String, Any]] = (a: Course) =>
    Map(
      "id"   -> a.id.get,
      "name" -> a.name
  )

  implicit val mapToCourse: MapT[Map[String, AnyRef], Course] =
    (a: Map[String, AnyRef]) =>
      Course(
        Option(a("id").toString.toLong),
        a("name").toString
    )

  implicit val mapToCourseList: MapT[List[Map[String, AnyRef]], List[Course]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Course])

  implicit val assignmentToMap: MapT[Assignment, Map[String, Any]] = (a: Assignment) =>
    Map("id" -> a.id.get, "name" -> a.name, "course" -> a.course.get.toMap[Map[String, Any]])

  implicit val mapToAssignment: MapT[Map[String, AnyRef], Assignment] =
    (a: Map[String, AnyRef]) =>
      Assignment(
        Option(a("id").toString.toLong),
        Option(a("course").asInstanceOf[Map[String, AnyRef]].mapTo[Course]),
        name = a("name").toString
    )

  implicit val mapToAssignmentList: MapT[List[Map[String, AnyRef]], List[Assignment]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Assignment])

}
