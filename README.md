# enumeration ext

> Derive enumeration values and codecs for enums and nested sealed class/trait hierarchies

## Installation

```sbt
libraryDependencies ++=
  "io.taig" %% "enumeration-ext-core" % "x.y.z" ::
  "io.taig" %% "enumeration-ext-circe" % "x.y.z" ::
  "io.taig" %% "enumeration-ext-skunk" % "x.y.z" ::
  Nil
```

## Usage

```scala
import io.taig.enumeration.ext.*

enum Animal:
  case Bird
  case Cat
  case Dog

valuesOf[Animal]
// > List(Bird, Cat, Dog)

given mapping: Mapping[Animal, String] = Mapping.enumeration:
  case Animal.Bird => "bird"
  case Animal.Cat => "cat"
  case Animal.Dog => "dog"

mapping.values
// > List(Bird, Cat, Dog)

mapping.values.map(mapping.inj)
// > List(bird, cat, dog)

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
import io.taig.enumeration.ext.circe.*

Decoder[Animal].decodeJson(Json.fromString("dog"))
// > Right(Dog)

Decoder[Animal].decodeJson(Json.fromString("whale"))
// > "Couldn't decode value 'whale.' Allowed values: 'bird,cat,dog'"

Encoder[Animal].apply(Animal.Dog)
// > "dog"
```

### Skunk

```scala
import skunk.Codec
import skunk.data.Type
import io.taig.enumeration.ext.skunk.*

val animal: Codec[Animal] = enumeration[Animal](Type("animal"))
```