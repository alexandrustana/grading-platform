package domain

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 07/04/2018
  */
sealed trait ValidationError extends Product with Serializable
case class AlreadyExistsError[A](o: A) extends ValidationError
case class InvalidModelError(errors: List[String]) extends ValidationError