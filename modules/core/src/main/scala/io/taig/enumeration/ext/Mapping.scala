package io.taig.enumeration.ext

import cats.{Eq, Hash, Inject}
import cats.syntax.all.*

import scala.annotation.targetName
import scala.collection.immutable.IntMap

sealed abstract class Mapping[A, B] extends Inject[A, B]:
  def values: List[A]

object Mapping:
  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def enumeration[A, B: Hash](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping[A, B] = new Mapping[A, B]:
    override val values: List[A] = valuesOf[A]
    val lookup: IntMap[A] = values.map(a => (f(a).hash, a)).to(IntMap)
    override def inj: A => B = f
    override def prj: B => Option[A] = b => lookup.get(b.hash)

  def constant[A <: B: ValueOf, B: Eq]: Mapping[A, B] = new Mapping[A, B]:
    val a: A = valueOf[A]
    override def values: List[A] = List(a)
    override def inj: A => B = identity
    override def prj: B => Option[A] = b => Option.when(b === a)(a)

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]
