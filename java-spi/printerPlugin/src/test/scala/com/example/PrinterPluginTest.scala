package com.example

import munit.FunSuite

import java.util.ServiceLoader
import scala.jdk.CollectionConverters.*

class PrinterPluginTest extends FunSuite {
  test("The plugin loads") {
    assert {
      ServiceLoader.load(classOf[Plugin]).asScala.exists {
        case p if p.isInstanceOf[PrinterPlugin] => true
      }
    }
  }
}
