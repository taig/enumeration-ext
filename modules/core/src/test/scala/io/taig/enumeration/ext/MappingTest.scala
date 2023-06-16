package io.taig.enumeration.ext

import munit.FunSuite

final class MappingTest extends FunSuite:
  test("enumeration"):
    enum Animal:
      case Bird
      case Cat
      case Dog

    val injection: Mapping[Animal, String] = Mapping.enumeration:
      case Animal.Bird => "Bird"
      case Animal.Cat  => "Cat"
      case Animal.Dog  => "Dog"

    assertEquals(obtained = injection.inj(Animal.Bird), expected = "Bird")
    assertEquals(obtained = injection.inj(Animal.Cat), expected = "Cat")
    assertEquals(obtained = injection.inj(Animal.Dog), expected = "Dog")
    assertEquals(obtained = injection.prj("Bird"), expected = Some(Animal.Bird))
    assertEquals(obtained = injection.prj("Cat"), expected = Some(Animal.Cat))
    assertEquals(obtained = injection.prj("Dog"), expected = Some(Animal.Dog))
    assertEquals(obtained = injection.prj("Whale"), expected = None)
