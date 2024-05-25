package com.example

import coursier.Fetch
import coursier.parse.DependencyParser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

import java.awt.BorderLayout
import java.awt.Font
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceLoader
import java.{util => ju}
import scala.jdk.CollectionConverters.*
import scala.util.Using

object DragDropDemo extends App {
  SwingUtilities.invokeLater(() => new DragDropDemo().setVisible(true))
}

class DragDropDemo extends JFrame {
  // Set up the main window
  setTitle("Pluggable app")
  setSize(400, 200)
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setLocationRelativeTo(null)

  // Create and set up the label
  val label = new JLabel("Please drag&drop plugins here", SwingConstants.CENTER)
  label.setFont(new Font("Arial", Font.PLAIN, 16))
  add(label, BorderLayout.CENTER)

  val listener: DropTargetAdapter =
    event =>
      try {
        // Accept the drop first, important!
        event.acceptDrop(DnDConstants.ACTION_COPY)

        // Get the dropped files
        val transferable = event.getTransferable
        val droppedFiles = transferable
          .getTransferData(DataFlavor.javaFileListFlavor)
          .asInstanceOf[java.util.List[File]]

        val loader = new URLClassLoader(droppedFiles.asScala.map(_.toURI().toURL()).toArray)

        // format: off

        val plugins =
          ServiceLoader.load(
            classOf[Plugin],
            loader
          )

        val fileNames = plugins.asScala.toList
          .map(_.name)
          .mkString("<html>Loaded plugin(s):<br>", "<br>", "</html>")

        // Update the label with the file names
        label.setText(fileNames.toString)

        // Indicate drop success
        event.dropComplete(true)
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
          event.dropComplete(false)

      }

  new DropTarget(label, listener)

}
