package io.taig.mapping

import io.taig.mapping.Mapping
import munit.FunSuite

enum Fruit:
  case Apple
  case Banana
  case Cherry

object Fruit:
  val mapping: Mapping[Fruit, String] = Mapping.of:
    case Fruit.Apple  => "Apple"
    case Fruit.Banana => "Banana"
    case Fruit.Cherry => "Cherry"

final class MappingTest extends FunSuite:
  test("enumeration"):
    assertEquals(obtained = Fruit.mapping.inj(Fruit.Apple), expected = "Apple")
    assertEquals(obtained = Fruit.mapping.inj(Fruit.Banana), expected = "Banana")
    assertEquals(obtained = Fruit.mapping.inj(Fruit.Cherry), expected = "Cherry")
    assertEquals(obtained = Fruit.mapping.prj("Apple"), expected = Some(Fruit.Apple))
    assertEquals(obtained = Fruit.mapping.prj("Banana"), expected = Some(Fruit.Banana))
    assertEquals(obtained = Fruit.mapping.prj("Cherry"), expected = Some(Fruit.Cherry))
    assertEquals(obtained = Fruit.mapping.prj("Pear"), expected = None)

  test("enumeration.constant"):
    val mapping = Mapping.constant[String]("foobar")

    assertEquals(obtained = mapping.inj("foobar"), expected = "foobar")
    assertEquals(obtained = mapping.prj("foobar"): Option[String], expected = Some("foobar"))
    assertEquals(obtained = mapping.prj("foo"): Option[String], expected = None)
    assertEquals(obtained = mapping.prj("bar"): Option[String], expected = None)
