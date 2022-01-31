package net.bdew.remapper

case class Replacement(start: Int, end: Int, content: String)

object MultiReplacer {
  def replace(input: String, replacements: List[Replacement]): String = {
    val reps = replacements.sortBy(_.start)

    var cpos = 0
    var offs = 0

    var str = input

    for (rep <- reps) {
      require(rep.start > cpos, s"Replacement overlap in $rep")
      cpos = rep.end
      str = str.substring(0, rep.start + offs) + rep.content + str.substring(rep.end + offs)
      offs = offs + rep.content.length - (rep.end - rep.start)
    }

    str
  }
}
