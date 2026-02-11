# Mapping

> An extension of cats.Inject built on top of singleton structure derivation

## Installation

```sbt
libraryDependencies ++=
  "io.taig" %% "mapping-core" % "x.y.z" ::
  "io.taig" %% "mapping-circe" % "x.y.z" ::
  "io.taig" %% "mapping-skunk" % "x.y.z" ::
  Nil
```

## Usage

```scala
import io.taig.mapping.*

enum Animal:
  case Bird
  case Cat
  case Dog

singletonValues[Animal]
// > NonEmptyList(Bird, Cat, Dog)

given mapping: Mapping[Animal, String] = Mapping.of:
  case Animal.Bird => "bird"
  case Animal.Cat => "cat"
  case Animal.Dog => "dog"

mapping.values
// > NonEmptyList(Bird, Cat, Dog)

mapping.values.map(mapping.inj)
// > NonEmptyList(bird, cat, dog)

mapping.inj(Animal.Dog)
// > "dog"

mapping.prj("cat")
// > Some(Cat)

mapping.prj("whale")
// > None
```

### Circe

```scala
import io.circe.{Decoder, Encoder}
import io.taig.mapping.circe.*

Decoder[Animal].decodeJson(Json.fromString("dog"))
// > Right(Dog)

Decoder[Animal].decodeJson(Json.fromString("whale"))
// > "Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'"

Encoder[Animal].apply(Animal.Dog)
// > "dog"
```

### Ciris

```scala
import skunk.Codec
import skunk.data.Type
import io.taig.mapping.ciris.*

summon[ConfigDecoder[String, Animal]]
```