package com.example

import coursier.Fetch
import coursier.parse.DependencyParser

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceLoader
import scala.jdk.CollectionConverters.*

@main def app(extraDepsString: String) = {

  val extraDepJars = Fetch()
    .addDependencies(
      DependencyParser.dependencies(extraDepsString.split(","), "3.4.1").either.toOption.get*
    )
    .run()

  val loader = new URLClassLoader(extraDepJars.map(_.toURI().toURL()).toArray)

  val plugins = ServiceLoader.load(classOf[Plugin], loader).asScala.toList

  println(s"Plugins loaded: ${plugins.map(_.name).mkString(", ")}\n")

  val data = Data("Hello, world!")

  plugins.foreach { plugin =>
    println(s"${plugin.name}:")
    plugin.run(data)
  }
}
