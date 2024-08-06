package io.taig.enumeration.ext

import cats.{Eq, Inject}
import cats.syntax.all.*
import cats.data.NonEmptyMap

import scala.annotation.targetName
import cats.Order
import cats.data.NonEmptyList

sealed abstract class Mapping[A, B] extends Inject[A, B]:
  def values: NonEmptyList[A]

object Mapping:
  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def enumeration[A, B: Order](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping[A, B] = new Mapping[A, B]:
    override val values: NonEmptyList[A] = valuesOf[A]

    val lookup: NonEmptyMap[B, A] =
      NonEmptyMap.of((f(values.head), values.head), values.tail.map(a => (f(a), a))*)

    override def inj: A => B = f
    override def prj: B => Option[A] = lookup.apply

  def constant[A <: B: ValueOf, B: Eq]: Mapping[A, B] = new Mapping[A, B]:
    val a: A = valueOf[A]
    override def values: NonEmptyList[A] = NonEmptyList.one(a)
    override def inj: A => B = identity
    override def prj: B => Option[A] = b => Option.when(b === a)(a)

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]
