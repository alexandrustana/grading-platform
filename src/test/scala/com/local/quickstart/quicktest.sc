val b:Either[Int, String] = Left(1)

val c: Either[Int, String] = b match {
  case Left(value) => Left(value)
  case Right(value) => Right(value)
}