package io.taig.enumeration.ext

import _root_.skunk.Codec
import _root_.skunk.data.Type

trait skunk:
  def mapping[A](tpe: Type)(using mapping: Mapping[A, String]): Codec[A] = Codec.simple(
    mapping.inj,
    s => mapping.prj(s).toRight(s"${tpe.name}: no such element '$s', expected '${mapping.values.mkString(",")}'"),
    tpe
  )

  def enumeration[A](tpe: Type)(f: A => String)(using EnumerationValues.Aux[A, A]): Codec[A] =
    mapping(tpe)(using Mapping.enumeration(f))

object skunk extends skunk
