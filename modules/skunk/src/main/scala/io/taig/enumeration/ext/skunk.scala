package io.taig.enumeration.ext

import _root_.skunk.Codec
import _root_.skunk.data.Type

object skunk:
  def mapping[A](tpe: Type)(using mapping: Mapping[A, String]): Codec[A] = Codec.simple(
    mapping.inj,
    s => mapping.prj(s).toRight(s"${tpe.name}: no such element '$s', expected '${mapping.values.mkString(",")}'"),
    tpe
  )
