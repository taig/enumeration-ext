import io.circe.Json
import io.circe.syntax._

object GitHubActionsGenerator {
  object Step {
    val SetupJava: Json = Json.obj(
      "name" := "Setup Java JDK",
      "uses" := "actions/setup-java@v4",
      "with" := Json.obj(
        "cache" := "sbt",
        "distribution" := "temurin",
        "java-version" := "17"
      )
    )

    val Checkout: Json = Json.obj(
      "name" := "Checkout",
      "uses" := "actions/checkout@v4"
    )
  }

  object Job {
    val Lint: Json = Json.obj(
      "name" := "Fatal warnings and code formatting",
      "runs-on" := "ubuntu-latest",
      "steps" := List(
        Step.Checkout,
        Step.SetupJava,
        Json.obj(
          "name" := "Workflows",
          "run" := "sbt blowoutCheck"
        ),
        Json.obj(
          "name" := "Code formatting",
          "run" := "sbt scalafmtCheckAll"
        ),
        Json.obj(
          "name" := "Fatal warnings",
          "run" := "sbt scalafixAll"
        )
      )
    )

    val Test: Json = Json.obj(
      "name" := "Unit tests",
      "runs-on" := "ubuntu-latest",
      "steps" := List(
        Step.Checkout,
        Step.SetupJava,
        Json.obj(
          "run" := "sbt test"
        )
      )
    )

    val Deploy: Json = Json.obj(
      "name" := "Deploy",
      "runs-on" := "ubuntu-latest",
      "needs" := List("lint", "test"),
      "steps" := List(
        Step.Checkout,
        Step.SetupJava,
        Json.obj(
          "name" := "Release",
          "run" := "sbt ci-release",
          "env" := Json.obj(
            "PGP_PASSPHRASE" := "${{secrets.PGP_PASSPHRASE}}",
            "PGP_SECRET" := "${{secrets.PGP_SECRET}}",
            "SONATYPE_PASSWORD" := "${{secrets.SONATYPE_PASSWORD}}",
            "SONATYPE_USERNAME" := "${{secrets.SONATYPE_USERNAME}}"
          )
        )
      )
    )
  }

  val main: Json = Json.obj(
    "name" := "CI & CD",
    "on" := Json.obj(
      "push" := Json.obj(
        "branches" := List("main")
      )
    ),
    "env" := Json.obj(
      "SBT_TPOLECAT_CI" := "true"
    ),
    "jobs" := Json.obj(
      "lint" := Job.Lint,
      "test" := Job.Test,
      "deploy" := Job.Deploy
    )
  )

  val tag: Json = Json.obj(
    "name" := "CD",
    "on" := Json.obj(
      "push" := Json.obj(
        "tags" := List("*.*.*")
      )
    ),
    "env" := Json.obj(
      "SBT_TPOLECAT_RELEASE" := "true"
    ),
    "jobs" := Json.obj(
      "lint" := Job.Lint,
      "test" := Job.Test,
      "deploy" := Job.Deploy
    )
  )

  val pullRequest: Json = Json.obj(
    "name" := "CI",
    "on" := Json.obj(
      "pull_request" := Json.obj(
        "branches" := List("main")
      )
    ),
    "env" := Json.obj(
      "SBT_TPOLECAT_CI" := "true"
    ),
    "jobs" := Json.obj(
      "lint" := Job.Lint,
      "test" := Job.Test
    )
  )
}
