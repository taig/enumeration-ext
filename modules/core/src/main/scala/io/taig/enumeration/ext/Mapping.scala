package io.taig.enumeration.ext

import cats.Eq
import cats.Inject
import cats.Order
import cats.data.NonEmptyList
import cats.data.NonEmptyMap
import cats.syntax.all.*

import scala.annotation.targetName

sealed abstract class Mapping[A, B] extends Inject[A, B]:
  self =>

  def values: NonEmptyList[A]

  final def imap[T](f: A => T)(g: T => A): Mapping[T, B] = new Mapping[T, B]:
    override def values: NonEmptyList[T] = self.values.map(f)
    override def inj: T => B = g.andThen(self.inj)
    override def prj: B => Option[T] = self.prj(_).map(f)

  final def product[C](mapping: Mapping[C, B])(
      merge: (B, B) => B,
      split: B => Option[(B, B)]
  ): Mapping[(A, C), B] = Mapping.product(this, mapping)(merge, split)

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

  def product[A, B, C](left: Mapping[A, C], right: Mapping[B, C])(
      merge: (C, C) => C,
      split: C => Option[(C, C)]
  ): Mapping[(A, B), C] = new Mapping[(A, B), C]:
    override val values: NonEmptyList[(A, B)] = left.values.flatMap(a => right.values.map(b => (a, b)))
    override def inj: ((A, B)) => C = { case (a, b) => merge(left.inj(a), right.inj(b)) }
    override def prj: C => Option[(A, B)] = c => split(c).flatMap { case (a, b) => (left.prj(a), right.prj(b)).tupled }

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]
