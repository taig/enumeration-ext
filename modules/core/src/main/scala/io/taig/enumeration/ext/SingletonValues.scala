package io.taig.enumeration.ext

import cats.data.NonEmptyList

import scala.quoted.*

abstract class SingletonValues[A]:
  def values: NonEmptyList[A]

object SingletonValues:
  inline def apply[A](using self: SingletonValues[A]): SingletonValues[A] = self

  private def apply[A](v: NonEmptyList[A]): SingletonValues[A] = new SingletonValues[A]:
    def values: NonEmptyList[A] = v

  inline given derived[A]: SingletonValues[A] = apply(singletonValues[A])

inline def singletonValues[A]: NonEmptyList[A] = ${ singletonValuesImpl[A] }

private def singletonValuesImpl[A: Type](using Quotes): Expr[NonEmptyList[A]] =
  import quotes.reflect.*

  def collectValues[T: Type]: List[Expr[T]] =
    val tpe = TypeRepr.of[T]
    tpe.dealias match
      case o: OrType            => unionValues[T](o)
      case _ if tpe.isSingleton => singletonValue[T](tpe) :: Nil
      case _                    =>
        val sym = tpe.typeSymbol
        if sym.flags.is(Flags.Sealed) then sumValues[T](sym)
        else if sym.flags.is(Flags.Case) && sym.caseFields.nonEmpty then productValues[T](tpe, sym)
        else
          report.errorAndAbort(
            s"Cannot derive values for ${tpe.show}. " +
              "Supported types: singleton types, enums, sealed traits/classes, " +
              "union types, and case classes/tuples whose fields are all enumerable."
          )

  def singletonValue[T: Type](tpe: TypeRepr): Expr[T] =
    tpe.asType match
      case '[t] =>
        Expr.summon[ValueOf[t]] match
          case Some(vo) => '{ $vo.value }.asExprOf[T]
          case None     =>
            report.errorAndAbort(s"Cannot determine value for singleton type: ${tpe.show}")

  def unionValues[T: Type](orType: OrType): List[Expr[T]] =
    def extract(tpe: TypeRepr): List[Expr[T]] =
      tpe.dealias match
        case o: OrType          => extract(o.left) ++ extract(o.right)
        case s if s.isSingleton => singletonValue[T](s) :: Nil
        case other              =>
          report.errorAndAbort(s"Unsupported type in union: ${other.show}. Only singleton types are supported.")
    extract(orType)

  def sumValues[T: Type](sym: Symbol): List[Expr[T]] =
    sym.children.flatMap { child =>
      if child.isTerm then
        child.termRef.asType match
          case '[t] =>
            Expr.summon[ValueOf[t]] match
              case Some(vo) => '{ $vo.value }.asExprOf[T] :: Nil
              case None     => report.errorAndAbort(s"Cannot get value for: ${child.name}")
      else
        child.typeRef.asType match
          case '[c] => collectValues[c].map(_.asExprOf[T])
    }

  def productValues[T: Type](tpe: TypeRepr, sym: Symbol): List[Expr[T]] =
    val constructorParams = sym.primaryConstructor.paramSymss.flatten.filter(_.isTerm)
    val fieldTypes = constructorParams.map(f => tpe.memberType(f).widen.dealias)
    val fieldValueExprs: List[List[Term]] = fieldTypes.map { ft =>
      ft.asType match
        case '[f] => collectValues[f].map(_.asTerm)
    }
    val cartesian = fieldValueExprs.foldRight(List(List.empty[Term])) { (vals, acc) =>
      for v <- vals; rest <- acc yield v :: rest
    }
    cartesian.map { args =>
      val companion = Ref(sym.companionModule)
      val applyMethod = sym.companionModule.methodMember("apply").head
      val typeParams = applyMethod.paramSymss.flatten.filter(_.isTypeParam)
      if typeParams.nonEmpty then Select.overloaded(companion, "apply", fieldTypes, args).asExprOf[T]
      else Select.overloaded(companion, "apply", Nil, args).asExprOf[T]
    }

  collectValues[A] match
    case head :: tail =>
      val tailExpr = Expr.ofList(tail)
      '{ NonEmptyList($head, $tailExpr) }
    case Nil =>
      report.errorAndAbort(s"Cannot derive values for ${Type.show[A]}: no values found")
