import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "Zound"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.googlecode.soundlibs" % "jorbis" % "0.0.17-1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
