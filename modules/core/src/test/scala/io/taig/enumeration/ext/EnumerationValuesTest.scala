package io.taig.enumeration.ext

import cats.data.NonEmptyList
import munit.FunSuite

import scala.deriving.Mirror

final class EnumerationValuesTest extends FunSuite:
  test("singleton"):
    assertEquals(
      obtained = valuesOf["foo"],
      expected = NonEmptyList.of("foo")
    )

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
    sealed abstract class Nested
    object Nested:
      enum Foo extends Nested:
        case A
        case B
        case C

      sealed abstract class Bar extends Nested
      object Bar:
        case object X extends Bar
        case object Y extends Bar
        case object Z extends Bar

    assertEquals(
      obtained = valuesOf[Nested],
      expected = NonEmptyList.of(Nested.Foo.A, Nested.Foo.B, Nested.Foo.C, Nested.Bar.X, Nested.Bar.Y, Nested.Bar.Z)
    )

  test("tuple"):
    enum Foo:
      case A
      case B

    enum Bar:
      case A
      case B

    assertEquals(
      obtained = valuesOf[(Foo, Bar)],
      expected = NonEmptyList.of(
        (Foo.A, Bar.A),
        (Foo.A, Bar.B),
        (Foo.B, Bar.A),
        (Foo.B, Bar.B)
      )
    )

  test("case class"):
    final case class Foobar(foo: Foobar.Foo, bar: Foobar.Bar)

    object Foobar:
      enum Foo:
        case A
        case B

      enum Bar:
        case A
        case B

    assertEquals(
      obtained = valuesOf[Foobar],
      expected = NonEmptyList.of(
        Foobar(Foobar.Foo.A, Foobar.Bar.A),
        Foobar(Foobar.Foo.A, Foobar.Bar.B),
        Foobar(Foobar.Foo.B, Foobar.Bar.A),
        Foobar(Foobar.Foo.B, Foobar.Bar.B)
      )
    )
