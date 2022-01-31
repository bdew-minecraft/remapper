package net.bdew.remapper

import java.nio.file.{Files, Paths}

object Main {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) throw new RuntimeException("usage: converter <path>")

    println(args.mkString(":"))
    val remaps = Remaps.fromFile(Paths.get("mappings.cfg"))
    val processor = new Processor(remaps)

    Files.walk(Paths.get(args(0))).filter(fn => fn.toString.endsWith(".scala") && Files.isRegularFile(fn)).forEach(fn => {
      print(s"Processing $fn ... ")
      val tree = TreeTools.loadSourceFile(fn)
      val result = processor.processTree(tree)
      Files.write(fn, result.getBytes("UTF-8"))
      println("Done!")
    })
  }
}
