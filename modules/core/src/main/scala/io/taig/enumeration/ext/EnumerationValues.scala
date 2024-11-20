package io.taig.enumeration.ext

import cats.data.NonEmptyList

import scala.deriving.Mirror

/** Derive all singelton values of a (nested) enumeration which may consist of Scala 3's `enum` or sealed traits and
  * classes
  */
abstract class EnumerationValues[A]:
  type Out
  def toNonEmptyList: NonEmptyList[Out]

object EnumerationValues:
  type Aux[A, B] = EnumerationValues[A] { type Out = B }

  inline def apply[A](using values: EnumerationValues[A]): EnumerationValues.Aux[A, values.Out] = values

  inline def apply[A, B](using values: EnumerationValues.Aux[A, B]): EnumerationValues.Aux[A, B] = values

  def apply[A, B](values: NonEmptyList[B]): EnumerationValues.Aux[A, B] = new EnumerationValues[A]:
    override type Out = B

    override def toNonEmptyList: NonEmptyList[Out] = values

  inline given [A](using
      mirror: Mirror.SumOf[A],
      values: EnumerationValues.Aux[mirror.MirroredElemTypes, A]
  ): EnumerationValues.Aux[A, A] = EnumerationValues(values = values.toNonEmptyList)

  inline given singleton[A <: Singleton, B <: Tuple, C >: A](using
      values: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = EnumerationValues(values = valueOf[A] :: values.toNonEmptyList)

  inline given nested[A, B <: Tuple, C >: A](using
      mirror: Mirror.SumOf[A],
      head: EnumerationValues.Aux[mirror.MirroredElemTypes, C],
      tail: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = EnumerationValues(values = head.toNonEmptyList.concatNel(tail.toNonEmptyList))

  inline given nestedOne[A, B >: A](using
      mirror: Mirror.SumOf[A],
      head: EnumerationValues.Aux[mirror.MirroredElemTypes, B]
  ): EnumerationValues.Aux[A *: EmptyTuple, B] = EnumerationValues(values = head.toNonEmptyList)

  inline given last[A <: Singleton, B >: A]: EnumerationValues.Aux[A *: EmptyTuple, B] =
    EnumerationValues(values = NonEmptyList.one(valueOf[A]))

def valuesOf[A](using values: EnumerationValues.Aux[A, A]): NonEmptyList[A] = values.toNonEmptyList
