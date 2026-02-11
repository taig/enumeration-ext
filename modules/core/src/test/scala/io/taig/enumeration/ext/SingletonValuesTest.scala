package io.taig.enumeration.ext

import cats.data.NonEmptyList
import munit.FunSuite

// Classes must be defined at the root level to avoid macro expansion bug
enum AnimalEnum:
  case Bird
  case Cat
  case Dog

sealed trait AnimalTrait extends Product with Serializable
object AnimalTrait:
  case object Bird extends AnimalTrait
  case object Cat extends AnimalTrait
  case object Dog extends AnimalTrait

sealed abstract class AnimalClass extends Product with Serializable
object AnimalClass:
  case object Bird extends AnimalClass
  case object Cat extends AnimalClass
  case object Dog extends AnimalClass

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

enum Foo:
  case A
  case B

enum Bar:
  case A
  case B

final case class Foobar(foo: Foobar.Foo, bar: Foobar.Bar)
object Foobar:
  enum Foo:
    case A
    case B

  enum Bar:
    case A
    case B

type AnimalUnion = "bird" | "cat" | "dog"

type PetUnion = AnimalEnum.Cat.type | AnimalEnum.Dog.type

final class SingletonValuesTest extends FunSuite:
  test("singleton"):
    assertEquals(
      obtained = singletonValues["foo"],
      expected = NonEmptyList.of("foo")
    )

  test("enum"):
    assertEquals(
      obtained = singletonValues[AnimalEnum],
      expected = NonEmptyList.of(AnimalEnum.Bird, AnimalEnum.Cat, AnimalEnum.Dog)
    )

  test("sealed trait"):
    assertEquals(
      obtained = singletonValues[AnimalTrait],
      expected = NonEmptyList.of(AnimalTrait.Bird, AnimalTrait.Cat, AnimalTrait.Dog)
    )

  test("sealed abstract class"):
    assertEquals(
      obtained = singletonValues[AnimalClass],
      expected = NonEmptyList.of(AnimalClass.Bird, AnimalClass.Cat, AnimalClass.Dog)
    )

  test("nested"):
    assertEquals(
      obtained = singletonValues[Nested],
      expected = NonEmptyList.of(Nested.Foo.A, Nested.Foo.B, Nested.Foo.C, Nested.Bar.X, Nested.Bar.Y, Nested.Bar.Z)
    )

  test("tuple"):
    assertEquals(
      obtained = singletonValues[(Foo, Bar)],
      expected = NonEmptyList.of(
        (Foo.A, Bar.A),
        (Foo.A, Bar.B),
        (Foo.B, Bar.A),
        (Foo.B, Bar.B)
      )
    )

  test("case class"):
    assertEquals(
      obtained = singletonValues[Foobar],
      expected = NonEmptyList.of(
        Foobar(Foobar.Foo.A, Foobar.Bar.A),
        Foobar(Foobar.Foo.A, Foobar.Bar.B),
        Foobar(Foobar.Foo.B, Foobar.Bar.A),
        Foobar(Foobar.Foo.B, Foobar.Bar.B)
      )
    )

  test("union: primitive"):
    assertEquals(obtained = singletonValues[AnimalUnion], expected = NonEmptyList.of("bird", "cat", "dog"))

  test("union: singleton object"):
    assertEquals(obtained = singletonValues[PetUnion], expected = NonEmptyList.of(AnimalEnum.Cat, AnimalEnum.Dog))
