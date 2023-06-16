package io.taig.enumeration.ext

import cats.{Hash, Inject}
import cats.syntax.all.*
import scala.collection.immutable.IntMap

abstract class Mapping[A, B] extends Inject[A, B]:
  def values: List[A]

object Mapping:
  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def enumeration[A, B: Hash](f: A => B)(using values: EnumerationValues.Aux[A, A]): Mapping[A, B] = new Mapping[A, B]:
    override val values: List[A] = valuesOf[A]
    val lookup: IntMap[A] = values.map(a => (f(a).hash, a)).to(IntMap)
    override def inj: A => B = f
    override def prj: B => Option[A] = b => lookup.get(b.hash)
