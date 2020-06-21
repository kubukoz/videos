package com.example

object Text {

  implicit class TextOps(val s: String) extends AnyVal {
    def backgroundMessage = Console.CYAN ++ ">>>bg<<< " ++ s ++ Console.RESET
    def supervisorMessage = Console.MAGENTA ++ s ++ Console.RESET
    def mainMessage = s
  }
}
