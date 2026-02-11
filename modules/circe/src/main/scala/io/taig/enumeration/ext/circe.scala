package io.taig.enumeration.ext

import cats.Order
import cats.Show
import cats.syntax.all.*
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.KeyDecoder
import io.circe.KeyEncoder

trait circe:
  given decodeMapping[A, B](using mapping: Mapping[A, B], decoder: Decoder[B], show: Show[B]): Decoder[A] =
    decoder.emap: b =>
      mapping
        .prj(b)
        .toRight(
          s"Couldn't decode value '$b.' Allowed values: '${mapping.values.map(mapping.inj).map(_.show).mkString_(",")}'"
        )

  def decoderOf[A, B: Decoder: Order: Show](f: A => B)(using SingletonValues[A]): Decoder[A] =
    decodeMapping(using Mapping.of[A, B](f))

  given encodeMapping[A, B](using mapping: Mapping[A, B], encoder: Encoder[B]): Encoder[A] =
    encoder.contramap(mapping.inj)

  def encoderOf[A, B: Encoder: Order](f: A => B)(using SingletonValues[A]): Encoder[A] = encodeMapping(using
    Mapping.of(f)
  )

  def codecOf[A, B: Decoder: Encoder: Order: Show](f: A => B)(using SingletonValues[A]): Codec[A] =
    Codec.from(decoderOf(f), encoderOf(f))

  given keyDecodeMapping[A](using mapping: Mapping[A, String]): KeyDecoder[A] = mapping.prj(_)

  def keyDecoderOf[A](f: A => String)(using SingletonValues[A]): KeyDecoder[A] =
    keyDecodeMapping(using Mapping.of(f))

  given keyEncodeMapping[A](using mapping: Mapping[A, String]): KeyEncoder[A] =
    KeyEncoder.instance(mapping.inj)

  def keyEncoderOf[A](f: A => String)(using SingletonValues[A]): KeyEncoder[A] =
    keyEncodeMapping(using Mapping.of(f))

object circe extends circe
