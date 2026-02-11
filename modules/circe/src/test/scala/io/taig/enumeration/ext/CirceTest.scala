package io.taig.enumeration.ext

import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.Encoder
import io.circe.Json
import io.taig.enumeration.ext.circe.*
import io.taig.enumeration.ext.circe.given
import munit.FunSuite

enum Animal:
  case Bird
  case Cat
  case Dog

object Animal:
  val mapping: Animal => String =
    case Animal.Bird => "bird"
    case Animal.Cat  => "cat"
    case Animal.Dog  => "dog"

  given Mapping[Animal, String] = Mapping.of(mapping)

final class CirceTest extends FunSuite:
  test("decodeMapping"):
    assertEquals(obtained = Decoder[Animal].decodeJson(Json.fromString("dog")), expected = Right(Animal.Dog))
    assertEquals(
      obtained = Decoder[Animal].decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )

  test("decoderOf"):
    assertEquals(
      obtained = decoderOf(Animal.mapping).decodeJson(Json.fromString("dog")),
      expected = Right(Animal.Dog)
    )
    assertEquals(
      obtained = decoderOf(Animal.mapping).decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )

  test("encodeMapping"):
    assertEquals(obtained = Encoder[Animal].apply(Animal.Dog), expected = Json.fromString("dog"))

  test("encoderOf"):
    assertEquals(
      obtained = encoderOf(Animal.mapping).apply(Animal.Dog),
      expected = Json.fromString("dog")
    )

  test("codecOf"):
    assertEquals(
      obtained = codecOf(Animal.mapping).decodeJson(Json.fromString("dog")),
      expected = Right(Animal.Dog)
    )
    assertEquals(
      obtained = codecOf(Animal.mapping).decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )
    assertEquals(
      obtained = codecOf(Animal.mapping).apply(Animal.Dog),
      expected = Json.fromString("dog")
    )
