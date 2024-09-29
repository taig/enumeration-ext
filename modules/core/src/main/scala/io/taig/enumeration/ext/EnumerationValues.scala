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

  inline given [A](using
      mirror: Mirror.SumOf[A],
      values: EnumerationValues.Aux[mirror.MirroredElemTypes, A]
  ): EnumerationValues.Aux[A, A] = new EnumerationValues[A]:
    override type Out = A
    override val toNonEmptyList: NonEmptyList[A] = values.toNonEmptyList

  inline given singleton[A <: Singleton, B <: Tuple, C >: A](using
      values: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = new EnumerationValues[A *: B]:
    override type Out = C
    override def toNonEmptyList: NonEmptyList[C] = valueOf[A] :: values.toNonEmptyList

  inline given nested[A, B <: Tuple, C >: A](using
      mirror: Mirror.SumOf[A],
      head: EnumerationValues.Aux[mirror.MirroredElemTypes, C],
      tail: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = new EnumerationValues[A *: B]:
    override type Out = C
    override def toNonEmptyList: NonEmptyList[C] = head.toNonEmptyList.concatNel(tail.toNonEmptyList)

  inline given nestedOne[A, B >: A](using
      mirror: Mirror.SumOf[A],
      head: EnumerationValues.Aux[mirror.MirroredElemTypes, B]
  ): EnumerationValues.Aux[A *: EmptyTuple, B] = new EnumerationValues[A *: EmptyTuple]:
    override type Out = B
    override def toNonEmptyList: NonEmptyList[B] = head.toNonEmptyList

  inline given last[A <: Singleton, B >: A]: EnumerationValues.Aux[A *: EmptyTuple, B] =
    new EnumerationValues[A *: EmptyTuple]:
      override type Out = B
      override def toNonEmptyList: NonEmptyList[B] = NonEmptyList.one(valueOf[A])

def valuesOf[A](using values: EnumerationValues.Aux[A, A]): NonEmptyList[A] = values.toNonEmptyList
