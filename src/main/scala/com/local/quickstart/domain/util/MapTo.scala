package com.local.quickstart.domain.util

import com.local.quickstart.domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 12/04/2018
  */
trait MapTo[A] {
  def mapTo(a: Map[String, Any]): A
}
object MapTo {
  def apply[A](implicit m: MapTo[A]): MapTo[A] = m

  implicit class MapToOps[A: MapTo](a: Map[String, AnyRef]) {
    def mapTo: A = MapTo[A].mapTo(a)
  }

  implicit val mapToAccount: MapTo[Account] =
    map =>
      Account(
        Option(map("id").asInstanceOf[Long]),
        map("firstName").asInstanceOf[String],
        map("lastName").asInstanceOf[String],
        map("email").asInstanceOf[String],
        map("password").asInstanceOf[String]
    )
}
