package net.bdew.remapper

import java.nio.file.{Files, Path}
import scala.meta._

object TreeTools {
  def loadSourceFile(fn: Path): Source = {
    val bytes = Files.readAllBytes(fn)
    val str = new String(bytes, "UTF-8")
    val input = Input.VirtualFile(fn.toString, str)
    input.parse[Source].get
  }

  def readTerm(term: Term): Seq[String] = {
    term match {
      case Term.Name(name) =>
        Seq(name)
      case Term.Select(term, name) =>
        readTerm(term) :+ name.value
    }
  }

  def readImport(importer: Importer, importee: Importee): Option[Seq[String]] = {
    importee match {
      case Importee.Name(name) => Some(readTerm(importer.ref) :+ name.value)
      case Importee.Rename(name, _) => Some(readTerm(importer.ref) :+ name.value)
      case Importee.Wildcard() => None
    }
  }

  def convertName(name: Seq[String]): Term.Ref = {
    if (name.length > 1)
      Term.Select(convertName(name.take(name.length - 1)), Term.Name(name.last))
    else
      Term.Name(name.head)
  }
}
