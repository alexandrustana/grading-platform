package com.local.quickstart.infrastructure.util.validation

import cats.data.{Kleisli, NonEmptyList}
/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 16/04/2018
  */
object Check {
  type Errors = NonEmptyList[String]

  def error(s: String): Errors =
    NonEmptyList(s, Nil)

  type Result[A] = Either[Errors, A]

  type Check[A, B] = Kleisli[Result, A, B]

  def check[A, B](func: A => Result[B]): Check[A, B] =
    Kleisli(func)

  def checkPred[A](pred: Predicate[Errors, A]): Check[A, A] =
    Kleisli[Result, A, A](pred.run)

  object CheckOps {

    def longerThan(n: Int)(field: String = ""): Predicate[Errors, String] =
      Predicate.lift(error(s"${if(field.isEmpty) "Must" else s"$field must"} be longer than $n characters"), str => str.length > n)

    val alphanumeric: Predicate[Errors, String] =
      Predicate.lift(error(s"Must be all alphanumeric characters"),
                     str => str.forall(_.isLetterOrDigit))

    val alpha: Predicate[Errors, String] =
      Predicate.lift(error("Must be all alphabetic characters"), str => str.forall(_.isLetter))

    def contains(char: Char): Predicate[Errors, String] =
      Predicate.lift(error(s"Must contain the character $char"), str => str.contains(char))

    def containsOnce(char: Char): Predicate[Errors, String] =
      Predicate.lift(error(s"Must contain the character $char only once"),
                     str => str.count(c => c == char) == 1)
  }

}
