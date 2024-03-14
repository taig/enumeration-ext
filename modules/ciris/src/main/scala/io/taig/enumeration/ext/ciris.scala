package io.taig.enumeration.ext

import cats.syntax.all.*
import cats.{Hash, Show}
import _root_.ciris.ConfigDecoder

trait ciris:
  given conficDecoder[A, B, C: Show](using mapping: Mapping[B, C], decoder: ConfigDecoder[A, C]): ConfigDecoder[A, B] =
    ConfigDecoder[A, C].mapOption(typeName = mapping.values.map(mapping.inj).map(_.show).mkString("|"))(mapping.prj)

  def decoderEnumeration[A, B, C: Show: Hash](
      f: B => C
  )(using EnumerationValues.Aux[B, B])(using decoder: ConfigDecoder[A, C]): ConfigDecoder[A, B] =
    conficDecoder(using Show[C], Mapping.enumeration(f), decoder)

object ciris extends ciris
