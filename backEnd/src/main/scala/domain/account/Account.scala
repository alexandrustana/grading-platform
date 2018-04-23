package domain.account

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
final case class Account(
    id: Option[Long] = None,
    firstName: String,
    lastName: String,
    email: String,
    password: String
)
