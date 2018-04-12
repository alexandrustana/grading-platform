package com.local.quickstart.domain.util

import com.local.quickstart.domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 12/04/2018
  */
trait ToMap[A] {
  def toMap(a: A): Map[String, Any]
}
object ToMap {
  def apply[A](implicit m: ToMap[A]): ToMap[A] = m

  implicit class MapTOps[A: ToMap](a: A) {
    def toMap: Map[String, Any] = ToMap[A].toMap(a)
  }

  implicit val accountToMap: ToMap[Account] =
    account =>
      (Map[String, Any]() /: account.getClass.getDeclaredFields) { (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(account))
    }
}
