val Version = new {
  val Cats = "2.12.0"
  val Circe = "0.14.10"
  val Ciris = "3.6.0"
  val Munit = "1.0.2"
  val Scala = "3.3.3"
}

inThisBuild(
  Def.settings(
    developers := List(Developer("taig", "Niklas Klein", "mail@taig.io", url("https://taig.io/"))),
    dynverVTagPrefix := false,
    homepage := Some(url("https://github.com/taig/enumeration-ext/")),
    licenses := List("MIT" -> url("https://raw.githubusercontent.com/taig/enumeration-ext/main/LICENSE")),
    organization := "io.taig",
    organizationHomepage := Some(url("https://taig.io/")),
    versionScheme := Some("early-semver"),
    scalaVersion := Version.Scala
  )
)

lazy val root = crossProject(JVMPlatform)
  .in(file("."))
  .enablePlugins(BlowoutYamlPlugin)
  .settings(noPublishSettings)
  .settings(
    blowoutGenerators ++= {
      val workflows = file(".github") / "workflows"
      BlowoutYamlGenerator.lzy(workflows / "main.yml", GitHubActionsGenerator.main) ::
        BlowoutYamlGenerator.lzy(workflows / "branches.yml", GitHubActionsGenerator.branches) ::
        Nil
    },
    name := "enumeration-ext"
  )
  .aggregate(core, circe, ciris)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/core"))
  .settings(
    Compile / scalacOptions ++=
      "-source:future" ::
        "-rewrite" ::
        "-new-syntax" ::
        "-Wunused:all" ::
        Nil,
    libraryDependencies ++=
      "org.typelevel" %%% "cats-core" % Version.Cats ::
        "org.scalameta" %%% "munit" % Version.Munit % "test" ::
        Nil,
    name := "enumeration-ext-core"
  )

lazy val circe = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/circe"))
  .settings(
    libraryDependencies ++=
      "io.circe" %%% "circe-core" % Version.Circe ::
        Nil,
    name := "enumeration-ext-circe"
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val ciris = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("modules/ciris"))
  .settings(
    libraryDependencies ++=
      "is.cir" %% "ciris" % Version.Ciris ::
        Nil,
    name := "enumeration-ext-ciris"
  )
  .dependsOn(core % "compile->compile;test->test")
