package net.bdew.remapper

import java.nio.file.{Files, Paths}

object GenerateMappings {
  // This generates the mapping from old class names used in 1.16 "official" mappings
  // to actual mojang class names used in 1.18
  // The various mapping files aren't included here because of licensing bs

  def main(args: Array[String]): Unit = {
    // From https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/1.16.5/joined.tsrg
    val tsrg1165 = MappingTable.fromTSRG(Paths.get("mappings", "1165-joined.tsrg"))

    // From https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/1.18.1/joined.tsrg
    val tsrg1181 = MappingTable.fromTSRG(Paths.get("mappings", "1181-joined.tsrg")).invert

    // From https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/1.16.5-20210115.111550/mcp_config-1.16.5-20210115.111550.zip
    val oldnames = MappingTable.fromTSRG(Paths.get("mappings", "oldnames.tsrg")).invert

    // From mojang
    // https://launchermeta.mojang.com/mc/game/version_manifest.json
    // https://launchermeta.mojang.com/v1/packages/6ad09383ac77f75147c38be806961099c02c1ef9/1.18.1.json
    // https://launcher.mojang.com/v1/objects/99ade839eacf69b8bed88c91bd70ca660aee47bb/client.txt
    val newmoj = MappingTable.fromMoj(Paths.get("mappings", "moj1181")).invert

    val out = List.newBuilder[String]

    for ((cls, oldObf) <- oldnames.mappings) {
      if (tsrg1165.mappings.isDefinedAt(oldObf)) {
        val oldSrg = tsrg1165.mappings(oldObf)
        if (tsrg1181.mappings.isDefinedAt(oldSrg)) {
          val newObf = tsrg1181.mappings(oldSrg)
          if (newmoj.mappings.isDefinedAt(newObf)) {
            val moj = newmoj.mappings(newObf)
            out += s"$cls $moj"
          } else {
            println(s"Warn $cls => $oldObf => $oldSrg => $newObf missing in mojang mappings")
          }
        } else {
          println(s"Warn $cls => $oldObf => $oldSrg missing in tsrg1181")
        }
      } else {
        println(s"Warn $cls => $oldObf missing in tsrg1165")
      }
    }

    val res = out.result()

    println(s"Writing ${res.length} mappings")
    Files.write(Paths.get("mappings.cfg"), res.mkString("\n").getBytes("UTF-8"))
  }

}
