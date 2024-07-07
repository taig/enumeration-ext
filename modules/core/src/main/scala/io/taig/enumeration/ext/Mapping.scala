package io.taig.enumeration.ext

import cats.{Eq, Inject}
import cats.syntax.all.*
import cats.data.NonEmptyMap

import scala.annotation.targetName
import cats.Order

sealed abstract class Mapping[A, B] extends Inject[A, B]:
  def values: List[A]

object Mapping:
  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def enumeration[A, B: Order](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping[A, B] = new Mapping[A, B]:
    val valuesNel = valuesOf[A]
    val lookup: NonEmptyMap[B, A] =
      NonEmptyMap.of((f(valuesNel.head), valuesNel.head), valuesNel.tail.map(a => (f(a), a))*)

    override def values: List[A] = valuesNel.toList
    override def inj: A => B = f
    override def prj: B => Option[A] = lookup.apply

  def constant[A <: B: ValueOf, B: Eq]: Mapping[A, B] = new Mapping[A, B]:
    val a: A = valueOf[A]
    override def values: List[A] = List(a)
    override def inj: A => B = identity
    override def prj: B => Option[A] = b => Option.when(b === a)(a)

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]
