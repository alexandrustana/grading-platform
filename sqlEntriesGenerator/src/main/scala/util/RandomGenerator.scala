package util

import scala.util.Random

/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 18/05/2018
  */
object RandomGenerator {

  def alpha: String =
    randomStringFromCharList(5, ('a' to 'z') ++ ('A' to 'Z'))

  def alphanumeric: String =
    randomStringFromCharList(5, ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))

  def numeric(l: List[Int]): (Int, List[Int]) = {
    val index = Random.nextInt(l.size)
    (l(index), l.zipWithIndex.filter { case (_, i) => i != index }.map(_._1))
  }

  private def randomStringFromCharList(length: Int, chars: Seq[Char]) =
    (1 to length).map(_ => chars(Random.nextInt(chars.length))).mkString
}
