package com.local.quickstart.domain

import com.local.quickstart.domain.account.Account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
sealed trait ValidationError extends Product with Serializable
case class AlreadyExistsError(account: Account) extends ValidationError
case class InvalidModelError(errors: List[String]) extends ValidationError