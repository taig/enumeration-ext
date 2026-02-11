package io.taig.mapping

import cats.Eq
import cats.Inject
import cats.Invariant
import cats.Order
import cats.data.NonEmptyList
import cats.data.NonEmptyMap
import cats.syntax.all.*
import io.taig.mapping.SingletonValues

import scala.annotation.targetName

sealed abstract class Mapping[A, B] extends Inject[A, B]:
  self =>

  def values: NonEmptyList[A]

  final def imap[C, D](fa: A => C, fc: C => A)(fb: B => D, fd: D => B): Mapping[C, D] =
    new Mapping[C, D]:
      override def values: NonEmptyList[C] = self.values.map(fa)
      override def inj: C => D = fc.andThen(self.inj).andThen(fb)
      override def prj: D => Option[C] = d => self.prj(fd(d)).map(fa)

  final def imapA[C](f: A => C)(g: C => A): Mapping[C, B] = imap(f, g)(identity, identity)

  final def imapB[D](f: B => D)(g: D => B): Mapping[A, D] = imap(identity, identity)(f, g)

  final def product[C](mapping: Mapping[C, B])(
      merge: (B, B) => B,
      split: B => Option[(B, B)]
  ): Mapping[(A, C), B] = Mapping.product(this, mapping)(merge, split)

object Mapping:
  inline def apply[A, B](using mapping: Mapping[A, B]): Mapping[A, B] = mapping

  def unsafe[A, B](_values: NonEmptyList[A], _inj: A => B, _prj: B => Option[A]): Mapping[A, B] = new Mapping[A, B]:
    override def values: NonEmptyList[A] = _values
    override def inj: A => B = _inj
    override def prj: B => Option[A] = _prj

  def of[A, B: Order](f: A => B)(using sv: SingletonValues[A]): Mapping[A, B] =
    val values = sv.values

    val lookup: NonEmptyMap[B, A] =
      NonEmptyMap.of((f(values.head), values.head), values.tail.map(a => (f(a), a))*)

    Mapping.unsafe(values, f, lookup.apply)

  def constant[A <: B: ValueOf, B: Eq]: Mapping[A, B] = new Mapping[A, B]:
    val a: A = valueOf[A]
    override def values: NonEmptyList[A] = NonEmptyList.one(a)
    override def inj: A => B = identity
    override def prj: B => Option[A] = b => Option.when(b === a)(a)

  @targetName("constantOf")
  def constant[A: Eq](value: A & Singleton): Mapping[value.type, A] = constant[value.type, A]

  def product[A, B, C](left: Mapping[A, C], right: Mapping[B, C])(
      merge: (C, C) => C,
      split: C => Option[(C, C)]
  ): Mapping[(A, B), C] = new Mapping[(A, B), C]:
    override val values: NonEmptyList[(A, B)] = left.values.flatMap(right.values.tupleLeft)
    override def inj: ((A, B)) => C = { case (a, b) => merge(left.inj(a), right.inj(b)) }
    override def prj: C => Option[(A, B)] = c => split(c).flatMap { case (a, b) => (left.prj(a), right.prj(b)).tupled }

  def invariantA[A]: Invariant[Mapping[*, A]] = new Invariant[Mapping[*, A]]:
    override def imap[B, C](fa: Mapping[B, A])(f: B => C)(g: C => B): Mapping[C, A] = fa.imapA(f)(g)

  given invariantB[A]: Invariant[Mapping[A, *]] with
    override def imap[B, C](fa: Mapping[A, B])(f: B => C)(g: C => B): Mapping[A, C] = fa.imapB(f)(g)
