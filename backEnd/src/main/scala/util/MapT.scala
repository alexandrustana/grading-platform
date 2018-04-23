package util

import domain.account.Account

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
    Map("id" -> a.id.get,
        "firstName" -> a.firstName,
        "lastName" -> a.lastName,
        "email" -> a.email,
        "password" -> a.password)

  implicit val mapToAccount: MapT[Map[String, AnyRef], Account] =
    (a: Map[String, AnyRef]) =>
      Account(
        Option(a("id").toString.toLong),
        a("firstName").toString,
        a("lastName").toString,
        a("email").toString,
        a("password").toString
      )

  implicit val mapToAccountList
    : MapT[List[Map[String, AnyRef]], List[Account]] =
    (a: List[Map[String, AnyRef]]) => a map (m => m.mapTo[Account])

}
