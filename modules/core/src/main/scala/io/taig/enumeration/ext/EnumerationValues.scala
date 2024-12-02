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

  inline given sum[A, B <: Tuple](using
      mirror: Mirror.SumOf[A] { type MirroredElemTypes = B },
      values: EnumerationValues.Aux[B, A]
  ): EnumerationValues.Aux[A, A] = EnumerationValues(values = values.toNonEmptyList)

  inline given product[A](using
      mirror: Mirror.ProductOf[A],
      values: EnumerationValues.Aux[mirror.MirroredElemTypes, mirror.MirroredElemTypes]
  ): EnumerationValues.Aux[A, A] = EnumerationValues(values = values.toNonEmptyList.map(mirror.fromTuple))

  inline given sum1[A, B >: A](using values: EnumerationValues.Aux[A, A]): EnumerationValues.Aux[A *: EmptyTuple, B] =
    EnumerationValues(values = values.toNonEmptyList)

  inline given sumN[A, B <: Tuple, C >: A](using
      head: EnumerationValues.Aux[A, A],
      tail: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] =
    EnumerationValues(values = head.toNonEmptyList.concatNel(tail.toNonEmptyList))

  inline given singleton[A <: Singleton]: EnumerationValues.Aux[A, A] =
    EnumerationValues(values = NonEmptyList.one(valueOf[A]))

  inline given product1[A](using
      values: EnumerationValues.Aux[A, A]
  ): EnumerationValues.Aux[A *: EmptyTuple, A *: EmptyTuple] =
    EnumerationValues(values = values.toNonEmptyList.map(_ *: EmptyTuple))

  inline given productN[A, B <: Tuple](using
      head: EnumerationValues.Aux[A, A],
      tail: EnumerationValues.Aux[B, B]
  ): EnumerationValues.Aux[A *: B, A *: B] =
    EnumerationValues(values = head.toNonEmptyList.flatMap(head => tail.toNonEmptyList.map(head *: _)))

def valuesOf[A](using values: EnumerationValues.Aux[A, A]): NonEmptyList[A] = values.toNonEmptyList
