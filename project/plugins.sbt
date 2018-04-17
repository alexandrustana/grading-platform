addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.3.0")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.0")

resolvers += "Flyway" at "https://davidmweber.github.io/flyway-sbt.repo"

resolvers += "jmcardon at bintray" at "https://dl.bintray.com/jmcardon/tsec"
