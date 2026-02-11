package io.taig.mapping

import _root_.ciris.ConfigDecoder
import cats.Show
import cats.kernel.Order
import cats.syntax.all.*

trait ciris:
  given configDecoder[A, B, C: Show](using
      mapping: Mapping[B, C]
  )(using decoder: ConfigDecoder[A, C]): ConfigDecoder[A, B] =
    ConfigDecoder[A, C].mapOption(typeName = mapping.values.map(mapping.inj).map(_.show).mkString_("|"))(mapping.prj)

  def decoderOf[A, B, C: Order: Show](
      f: B => C
  )(using decoder: ConfigDecoder[A, C], sv: SingletonValues[B]): ConfigDecoder[A, B] = configDecoder(using
    Mapping.of(f)
  )

object ciris extends ciris
