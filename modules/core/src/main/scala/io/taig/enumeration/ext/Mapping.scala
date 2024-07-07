package io.taig.enumeration.ext

import cats.{Eq, Inject}
import cats.syntax.all.*
import cats.data.NonEmptyMap

import scala.annotation.targetName
import cats.Order

sealed abstract class Mapping[A, B] extends Inject[A, B], Mapping.Inject[A, B], Mapping.Project[A, B]

object Mapping:
  sealed trait Inject[A, B]:
    def values: List[A]

    def inj: A => B

  object Inject:
    inline def apply[A, B](using mapping: Mapping.Inject[A, B]): Mapping.Inject[A, B] = mapping

    def enumeration[A, B: Order](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping.Inject[A, B] =
      new Mapping.Inject[A, B]:
        override def values: List[A] = valuesOf[A].toList
        override def inj: A => B = f

    def constant[A <: B: ValueOf, B: Eq]: Mapping.Inject[A, B] = new Mapping.Inject[A, B]:
      override def values: List[A] = List(valueOf[A])
      override def inj: A => B = identity

    @targetName("constantOf")
    def constant[A: Eq](value: A & Singleton): Mapping.Inject[value.type, A] = constant[value.type, A]

  sealed trait Project[A, B]:
    def values: List[A]

    def prj: B => Option[A]

  object Project:
    inline def apply[A, B](using mapping: Mapping.Project[A, B]): Mapping.Project[A, B] = mapping

    def enumeration[A, B: Order](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping.Project[A, B] =
      new Mapping.Project[A, B]:
        val valuesNel = valuesOf[A]
        val lookup: NonEmptyMap[B, A] =
          NonEmptyMap.of((f(valuesNel.head), valuesNel.head), valuesNel.tail.map(a => (f(a), a))*)

        override def values: List[A] = valuesNel.toList
        override def prj: B => Option[A] = lookup.apply

    def constant[A <: B: ValueOf, B: Eq]: Mapping.Project[A, B] = new Mapping.Project[A, B]:
      val a: A = valueOf[A]
      override def values: List[A] = List(a)
      override def prj: B => Option[A] = b => Option.when(b === a)(a)

    @targetName("constantOf")
    def constant[A: Eq](value: A & Singleton): Mapping.Project[value.type, A] = constant[value.type, A]

  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def enumeration[A, B: Order](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping[A, B] = new Mapping[A, B]:
    val valuesNel = valuesOf[A]
    val lookup: NonEmptyMap[B, A] =
      NonEmptyMap.of((f(valuesNel.head), valuesNel.head), valuesNel.tail.map(a => (f(a), a))*)

    override def values: List[A] = valuesNel.toList
    override def inj: A => B = f
    override def prj: B => Option[A] = lookup.apply

  def constant[A <: B: ValueOf, B: Eq]: Mapping[A, B] = new Mapping[A, B]:
    override def values: List[A] = List(valueOf[A])
    override def inj: A => B = identity
    override def prj: B => Option[A] = b => Option.when(b === valueOf[A])(valueOf[A])

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]
