package net.bdew.remapper

import java.nio.file.{Files, Path}

class MappingTable(val mappings: Map[String, String]) {
  def dump: String = mappings.map({ case (k, v) => s"$k -> $v" }).mkString("\n")
  def invert = new MappingTable(mappings.map(_.swap))
}

object MappingTable {
  def fromTSRG(fn: Path): MappingTable = {
    println(s"Loading $fn...")
    var lines = new String(Files.readAllBytes(fn), "UTF-8")
      .split("\n").toList
    if (lines.head.startsWith("tsrg2")) lines = lines.drop(1)
    val mappings = for (line <- lines if !line.startsWith("\t")) yield {
      val split = line.trim.split(" ")
      split(0) -> split(1).replace('/', '.')
    }
    println(s"Loaded ${mappings.length} entries")
    new MappingTable(mappings.toMap)
  }

  def fromMoj(fn: Path): MappingTable = {
    println(s"Loading $fn...")
    val lines = new String(Files.readAllBytes(fn), "UTF-8")
      .split("\n").toList.filter(x => !x.startsWith("#"))
    val mappings = for (line <- lines if !line.startsWith(" ")) yield {
      val split = line.trim.split(" ")
      split(0) -> split(2).replace(":", "")
    }
    println(s"Loaded ${mappings.length} entries")
    new MappingTable(mappings.toMap)
  }
}