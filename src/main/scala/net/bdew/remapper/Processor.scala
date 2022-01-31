package net.bdew.remapper

import scala.meta._

case class ImportRename(importer: Importer, importee: Importee, newName: Seq[String])

class Processor(remaps: Remaps) {
  def collectRenames(imp: Import): Seq[ImportRename] = {
    imp.importers.flatMap(importer => {
      importer.importees.flatMap(importee => {
        TreeTools.readImport(importer, importee).flatMap(parsed =>
          remaps.mapElem(parsed).map(rename =>
            ImportRename(importer, importee, rename)
          )
        )
      })
    })
  }

  def processTree(tree: Tree): String = {
    val source = tree.toString()

    val renames = tree.collect({
      case imp: Import => collectRenames(imp)
    }).flatten

    val termRenames = renames.flatMap(rename => rename.importee collect {
      case Importee.Name(oldName) if oldName.value != rename.newName.last => oldName.value -> rename.newName.last
    }).toMap

    val importRenames = renames.groupBy(_.importer)

    val termReplacements = ReplacementTraverser.run(tree, importRenames, termRenames)

    MultiReplacer.replace(source, termReplacements)
  }
}
