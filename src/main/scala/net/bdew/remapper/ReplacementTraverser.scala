package net.bdew.remapper

import scala.meta._

class ReplacementTraverser(importRenames: Map[Importer, List[ImportRename]], termRenames: Map[String, String]) extends Traverser {
  private val list = List.newBuilder[Replacement]

  private def createImport(orig: Importee, newName: Seq[String]): Importer = {
    if (newName.length < 2) throw new IllegalArgumentException(s"Invalid name $newName")
    val newImporteeTerm = TreeTools.convertName(newName.take(newName.length - 1))
    orig match {
      case Importee.Name(_) => Importer(newImporteeTerm, List(Importee.Name(Name.Indeterminate(newName.last))))
      case Importee.Rename(_, rename) => Importer(newImporteeTerm, List(Importee.Rename(Name.Indeterminate(newName.last), rename)))
      case _ => throw new IllegalArgumentException(s"Invalid rename importee $orig")
    }
  }

  private def renameImports(element: Import): Replacement = {
    val rebuilt = element.importers.flatMap(importer => {
      importRenames.get(importer).map(renames => {
        val renamesMap = renames.map(x => x.importee -> x).toMap
        importer.importees.map(importee => {
          renamesMap.get(importee) match {
            case Some(rename) => createImport(importee, rename.newName)
            case None => Importer(importer.ref, List(importee))
          }
        })
      }).orElse(Some(List(importer))).toList
    })
    val replacement = Import(rebuilt.flatten)
    Replacement(element.pos.start, element.pos.end, replacement.toString())
  }


  override def apply(tree: Tree): Unit = tree match {
    case a: Import =>
      list += renameImports(a)
    case a: Name if termRenames.isDefinedAt(a.value) =>
      list += Replacement(a.pos.start, a.pos.end, termRenames(a.value))
    case node =>
      super.apply(node)
  }

  def result: List[Replacement] = list.result()
}

object ReplacementTraverser {
  def run(tree: Tree, importRenames: Map[Importer, List[ImportRename]], termRenames: Map[String, String]): List[Replacement] = {
    val traverser = new ReplacementTraverser(importRenames, termRenames)
    traverser.apply(tree)
    traverser.result
  }
}