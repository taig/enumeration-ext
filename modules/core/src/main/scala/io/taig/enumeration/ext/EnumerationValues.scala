package io.taig.enumeration.ext

import scala.deriving.Mirror

/** Derive all values of a (nested) enumeration which, may consist of Scala 3's `enum` or sealed ADTs */
abstract class EnumerationValues[A]:
  type Out
  def toList: List[Out]

object EnumerationValues:
  type Aux[A, B] = EnumerationValues[A] { type Out = B }

  inline given [A](using
      mirror: Mirror.SumOf[A],
      values: EnumerationValues.Aux[mirror.MirroredElemTypes, A]
  ): EnumerationValues.Aux[A, A] = new EnumerationValues[A]:
    override type Out = A
    override val toList: List[A] = values.toList

  inline given singleton[A <: Singleton, B <: Tuple, C >: A](using
      values: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = new EnumerationValues[A *: B]:
    override type Out = C
    override def toList: List[C] = List(valueOf[A]) ++ values.toList

  inline given nested[A, B <: Tuple, C >: A](using
      mirror: Mirror.SumOf[A],
      head: EnumerationValues.Aux[mirror.MirroredElemTypes, C],
      tail: EnumerationValues.Aux[B, C]
  ): EnumerationValues.Aux[A *: B, C] = new EnumerationValues[A *: B]:
    override type Out = C
    override def toList: List[C] = head.toList ++ tail.toList

  inline given [A]: EnumerationValues.Aux[EmptyTuple, A] = new EnumerationValues[EmptyTuple]:
    override type Out = A
    override def toList: List[A] = List.empty

def valuesOf[A](using values: EnumerationValues.Aux[A, A]): List[A] = values.toList
