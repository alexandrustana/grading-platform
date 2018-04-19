package com.local.quickstart.domain.professor

import com.local.quickstart.domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
final case class Professor(id: Option[Long] = None, title: String, account: Account)
