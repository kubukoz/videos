package com.example

trait Plugin {
  def name: String
  def run(data: Data): Unit
}

case class Data(name: String)
