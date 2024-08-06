package io.taig.enumeration.ext

import cats.Order
import cats.Show
import cats.syntax.all.*
import io.circe.{Codec, Decoder, Encoder}

trait circe:
  given decodeMapping[A, B](using mapping: Mapping[A, B], decoder: Decoder[B])(using Show[B]): Decoder[A] =
    decoder.emap: b =>
      mapping
        .prj(b)
        .toRight(
          s"Couldn't decode value '$b.' Allowed values: '${mapping.values.map(mapping.inj).map(_.show).mkString_(",")}'"
        )

  def decoderEnumeration[A, B: Order: Show](
      f: A => B
  )(using EnumerationValues.Aux[A, A])(using decoder: Decoder[B]): Decoder[A] =
    decodeMapping(using Mapping.enumeration(f), decoder)

  given encodeMapping[A, B](using mapping: Mapping[A, B], encoder: Encoder[B]): Encoder[A] =
    encoder.contramap(mapping.inj)

  def encoderEnumeration[A, B: Order](
      f: A => B
  )(using EnumerationValues.Aux[A, A])(using encoder: Encoder[B]): Encoder[A] =
    encodeMapping(using Mapping.enumeration(f), encoder)

  def codecEnumeration[A, B: Order: Show](
      f: A => B
  )(using EnumerationValues.Aux[A, A], Decoder[B], Encoder[B]): Codec[A] =
    Codec.from(decoderEnumeration(f), encoderEnumeration(f))

object circe extends circe
