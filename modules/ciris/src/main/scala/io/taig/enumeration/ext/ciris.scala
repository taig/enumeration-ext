package io.taig.enumeration.ext

import _root_.ciris.ConfigDecoder
import cats.Show
import cats.kernel.Order
import cats.syntax.all.*

trait ciris:
  given conficDecoder[A, B, C: Show](using mapping: Mapping[B, C])(using decoder: ConfigDecoder[A, C]): ConfigDecoder[A, B] =
    ConfigDecoder[A, C].mapOption(typeName = mapping.values.map(mapping.inj).map(_.show).mkString_("|"))(mapping.prj)

  def decoderEnumeration[A, B, C: Order: Show](
      f: B => C
  )(using EnumerationValues.Aux[B, B])(using decoder: ConfigDecoder[A, C]): ConfigDecoder[A, B] =
    conficDecoder(using Mapping.enumeration(f))

object ciris extends ciris
