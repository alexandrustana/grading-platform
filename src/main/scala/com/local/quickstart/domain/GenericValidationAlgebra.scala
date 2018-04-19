package com.local.quickstart.domain

import cats.data.EitherT
/**
  * @author Alexandru Stana, alexandru.stana@busymachines.com
  * @since 19/04/2018
  */
trait GenericValidationAlgebra[F[_], A] {
  def checkModel(o: A): EitherT[F, InvalidModelError, Unit]
}