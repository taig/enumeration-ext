package io.taig.enumeration.ext

import cats.Show
import cats.syntax.all.*
import io.circe.{Decoder, Encoder}

trait circe:
  implicit def decodeMapping[A, B](using mapping: Mapping[A, B], decoder: Decoder[B])(using Show[B]): Decoder[A] =
    decoder.emap: b =>
      mapping
        .prj(b)
        .toRight(s"Couldn't decode value '$b.' Allowed values: '${mapping.values.map(mapping.inj).mkString(",")}'")

  implicit def encodeMapping[A, B](using mapping: Mapping[A, B], encoder: Encoder[B]): Encoder[A] =
    encoder.contramap(mapping.inj)

object circe extends circe
