package domain.student

import domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
final case class Student(id: Option[Long] = None, account: Option[Account] = None)
