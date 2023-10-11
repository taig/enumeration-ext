package io.taig.enumeration.ext

import io.taig.enumeration.ext.circe.given
import io.taig.enumeration.ext.circe.*
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import munit.FunSuite

final class CirceTest extends FunSuite:
  enum Animal:
    case Bird
    case Cat
    case Dog

  object Animal:
    val mapping: Animal => String =
      case Animal.Bird => "bird"
      case Animal.Cat  => "cat"
      case Animal.Dog  => "dog"

    given Mapping[Animal, String] = Mapping.enumeration(mapping)

  test("decodeMapping"):
    assertEquals(obtained = Decoder[Animal].decodeJson(Json.fromString("dog")), expected = Right(Animal.Dog))
    assertEquals(
      obtained = Decoder[Animal].decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )

  test("decoderEnumeration"):
    assertEquals(
      obtained = decoderEnumeration(Animal.mapping).decodeJson(Json.fromString("dog")),
      expected = Right(Animal.Dog)
    )
    assertEquals(
      obtained = decoderEnumeration(Animal.mapping).decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )

  test("encodeMapping"):
    assertEquals(obtained = Encoder[Animal].apply(Animal.Dog), expected = Json.fromString("dog"))

  test("encoderEnumeration"):
    assertEquals(
      obtained = encoderEnumeration(Animal.mapping).apply(Animal.Dog),
      expected = Json.fromString("dog")
    )

  test("codecEnumeration"):
    assertEquals(
      obtained = codecEnumeration(Animal.mapping).decodeJson(Json.fromString("dog")),
      expected = Right(Animal.Dog)
    )
    assertEquals(
      obtained = codecEnumeration(Animal.mapping).decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )
    assertEquals(
      obtained = codecEnumeration(Animal.mapping).apply(Animal.Dog),
      expected = Json.fromString("dog")
    )
