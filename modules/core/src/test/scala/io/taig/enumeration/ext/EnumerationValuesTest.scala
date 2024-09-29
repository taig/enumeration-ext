package io.taig.enumeration.ext

import cats.data.NonEmptyList
import munit.FunSuite

final class EnumerationValuesTest extends FunSuite:
  test("enum"):
    enum Animal:
      case Bird
      case Cat
      case Dog

    assertEquals(obtained = valuesOf[Animal], expected = NonEmptyList.of(Animal.Bird, Animal.Cat, Animal.Dog))

  test("sealed trait"):
    sealed trait Animal extends Product with Serializable
    object Animal:
      case object Bird extends Animal
      case object Cat extends Animal
      case object Dog extends Animal

    assertEquals(obtained = valuesOf[Animal], expected = NonEmptyList.of(Animal.Bird, Animal.Cat, Animal.Dog))

  test("sealed abstract class"):
    sealed abstract class Animal extends Product with Serializable
    object Animal:
      case object Bird extends Animal
      case object Cat extends Animal
      case object Dog extends Animal

    assertEquals(obtained = valuesOf[Animal], expected = NonEmptyList.of(Animal.Bird, Animal.Cat, Animal.Dog))

  test("nested"):
    sealed abstract class Foobar
    object Foobar:
      enum Foo extends Foobar:
        case A
        case B
        case C

      sealed abstract class Bar extends Foobar
      object Bar:
        case object X extends Bar
        case object Y extends Bar
        case object Z extends Bar

    assertEquals(
      obtained = valuesOf[Foobar],
      expected = NonEmptyList.of(Foobar.Foo.A, Foobar.Foo.B, Foobar.Foo.C, Foobar.Bar.X, Foobar.Bar.Y, Foobar.Bar.Z)
    )
