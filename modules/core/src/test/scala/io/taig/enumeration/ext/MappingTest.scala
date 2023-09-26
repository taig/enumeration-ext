package io.taig.enumeration.ext

import munit.FunSuite

final class MappingTest extends FunSuite:
  test("enumeration"):
    enum Animal:
      case Bird
      case Cat
      case Dog

    val mapping: Mapping[Animal, String] = Mapping.enumeration:
      case Animal.Bird => "Bird"
      case Animal.Cat  => "Cat"
      case Animal.Dog  => "Dog"

    assertEquals(obtained = mapping.inj(Animal.Bird), expected = "Bird")
    assertEquals(obtained = mapping.inj(Animal.Cat), expected = "Cat")
    assertEquals(obtained = mapping.inj(Animal.Dog), expected = "Dog")
    assertEquals(obtained = mapping.prj("Bird"), expected = Some(Animal.Bird))
    assertEquals(obtained = mapping.prj("Cat"), expected = Some(Animal.Cat))
    assertEquals(obtained = mapping.prj("Dog"), expected = Some(Animal.Dog))
    assertEquals(obtained = mapping.prj("Whale"), expected = None)

  test("enumeration.constant"):
    val mapping = Mapping.constant[String]("foobar")

    assertEquals(obtained = mapping.inj("foobar"), expected = "foobar")
    assertEquals(obtained = mapping.prj("foobar"): Option[String], expected = Some("foobar"))
    assertEquals(obtained = mapping.prj("foo"): Option[String], expected = None)
    assertEquals(obtained = mapping.prj("bar"): Option[String], expected = None)
