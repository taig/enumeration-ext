package io.taig.enumeration.ext

import io.taig.enumeration.ext.circe.*
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import munit.FunSuite

final class CirceTest extends FunSuite:
  enum Animal:
    case Bird
    case Cat
    case Dog

  object Animal:
    given Mapping[Animal, String] = Mapping.enumeration:
      case Animal.Bird => "bird"
      case Animal.Cat  => "cat"
      case Animal.Dog  => "dog"

  test("decode"):
    assertEquals(obtained = Decoder[Animal].decodeJson(Json.fromString("dog")), expected = Right(Animal.Dog))
    assertEquals(
      obtained = Decoder[Animal].decodeJson(Json.fromString("whale")),
      expected = Left(DecodingFailure("Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'", List.empty))
    )

  test("encode"):
    assertEquals(obtained = Encoder[Animal].apply(Animal.Dog), expected = Json.fromString("dog"))
