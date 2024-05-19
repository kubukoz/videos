package com.example

class PrinterPlugin extends Plugin {
  def name: String = "printer"
  def run(data: Data): Unit = println(data)
}
