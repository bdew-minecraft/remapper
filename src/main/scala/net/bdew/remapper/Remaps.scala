package net.bdew.remapper

import java.nio.file.Path
import scala.annotation.tailrec

sealed trait MapElem

case class MapLeaf(v: Seq[String]) extends MapElem

case class MapPack(convs: Map[String, MapElem]) extends MapElem

case class Remaps(convs: MapPack) {
  @tailrec
  private def matchImport(term: Seq[String], tree: MapPack): Option[MapLeaf] = {
    term match {
      case Nil => None
      case x :: rest => tree.convs.get(x) match {
        case Some(fin: MapLeaf) if rest == Nil => Some(fin)
        case Some(more: MapPack) => matchImport(rest, more)
        case _ => None
      }
    }
  }

  def mapElem(term: Seq[String]): Option[Seq[String]] = {
    matchImport(term, convs) map (_.v)
  }
}

object Remaps {
  private def addToPackage(pkg: MapPack, srcPath: List[String], dstPath: List[String]): MapPack = {
    require(srcPath.nonEmpty)
    if (srcPath.length == 1) {
      pkg.copy(convs = pkg.convs + (srcPath.head -> MapLeaf(dstPath)))
    } else {
      val newPkg = pkg.convs.get(srcPath.head) match {
        case Some(x: MapPack) => addToPackage(x, srcPath.tail, dstPath)
        case Some(x: MapLeaf) => throw new RuntimeException(s"Trying to define rename for $srcPath but it's already a leaf $x")
        case None => addToPackage(MapPack(Map.empty), srcPath.tail, dstPath)
      }
      pkg.copy(convs = pkg.convs + (srcPath.head -> newPkg))
    }
  }

  def fromFile(fn: Path): Remaps = {
    println(s"Loading mappings from $fn")
    val bytes = java.nio.file.Files.readAllBytes(fn)
    val str = new String(bytes, "UTF-8")
    val lines = str.split("\n").map(_.trim).map(_.split(" "))

    print(s"Loaded ${lines.length} entries, constructing map... ")

    var root = MapPack(Map.empty)

    for (Array(src, dst) <- lines) {
      val srcPath = src.split('.').toList
      val dstPath = dst.split('.').toList
      root = addToPackage(root, srcPath, dstPath)
    }

    println("Done!")

    new Remaps(root)
  }
}
