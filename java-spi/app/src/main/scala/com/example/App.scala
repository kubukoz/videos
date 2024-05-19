package com.example

import coursier.Fetch
import coursier.parse.DependencyParser

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceLoader
import scala.jdk.CollectionConverters.*

@main def app(extraDepsString: String) = {

  // 1. Prepare dependencies
  val extraDepJars: Seq[File] = Fetch()
    .addDependencies(
      DependencyParser.dependencies(extraDepsString.split(","), "3.4.1").either.toOption.get*
    )
    .run()

  extraDepJars.foreach(println)

  // 2. Build classloader
  val loader = new URLClassLoader(extraDepJars.map(_.toURI().toURL()).toArray)

  // 3. Load plugins
  val plugins = ServiceLoader.load(classOf[Plugin], loader).asScala.toList

  // 4. Use plugins
  println(s"Plugins loaded: ${plugins.map(_.name).mkString(", ")}\n")

  val data = Data("Hello, world!")

  plugins.foreach { plugin =>
    println(s"${plugin.name}:")
    plugin.run(data)
  }
}
