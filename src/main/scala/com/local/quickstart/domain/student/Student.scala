package com.local.quickstart.domain.student

import com.local.quickstart.domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
final case class Student(id: Option[Long] = None, account: Account)
