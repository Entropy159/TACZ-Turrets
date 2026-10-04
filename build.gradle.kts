@file:Suppress("UnstableApiUsage")

import java.util.zip.ZipFile

plugins {
    id("dev.architectury.loom")
}

val minecraft = stonecutter.current.version
val loader = prop("loom.platform")!!
val isForge = loader == "forge"
stonecutter.consts(loader, "forge", "neoforge")

version = "${mod.version}-$minecraft"
group = mod.prop("group")
base {
    archivesName.set("${mod.id}-$loader")
}

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://maven.neoforged.net/releases/")
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://dl.cloudsmith.io/public/tslat/sbl/maven/")
    maven("https://maven.ftb.dev/releases") {
        content { includeGroup("dev.ftb.mods") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())
    if (isForge) {
        "forge"("net.minecraftforge:forge:$minecraft-${mod.dep("forge_loader")}")
        "forgeRuntimeLibrary"("com.eliotlash.mclib:mclib:20")
        compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${mod.dep("mixinextras")}")!!)
        implementation(include("io.github.llamalad7:mixinextras-forge:${mod.dep("mixinextras")}")!!)
    } else {
        "neoForge"("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
    }
    val geckolib = "software.bernie.geckolib:geckolib-$loader-$minecraft:${mod.dep("geckolib")}"
    val smartbrainlib = "net.tslat.smartbrainlib:SmartBrainLib-$loader-$minecraft:${mod.dep("smartbrainlib")}"
    modImplementation(geckolib)
    include(geckolib)
    modImplementation(smartbrainlib)
    include(smartbrainlib)
    val tacz = "maven.modrinth:${mod.dep("tacz_slug")}:${mod.dep("tacz")}"
    modImplementation(tacz)
    modCompileOnly("dev.ftb.mods:ftb-teams-$loader:${mod.dep("ftbteams")}") { isTransitive = false }
    modCompileOnly("maven.modrinth:open-parties-and-claims:${mod.dep("opac")}") { isTransitive = false }

    val taczNested = layout.buildDirectory.dir("tacz-jarjar/${mod.dep("tacz")}").get().asFile
    if (!taczNested.resolve(".done").exists()) {
        taczNested.mkdirs()
        extractJarJar(configurations.detachedConfiguration(project.dependencies.create(tacz)).apply { isTransitive = false }.singleFile, taczNested)
        taczNested.resolve(".done").createNewFile()
    }
    taczNested.listFiles { file -> file.extension == "jar" && !file.name.startsWith("mixinextras") }!!.forEach {
        if (isModJar(it)) modRuntimeOnly(files(it)) else "forgeRuntimeLibrary"(files(it))
    }
}

fun extractJarJar(jar: File, into: File) {
    ZipFile(jar).use { zip ->
        zip.entries().asSequence().filter { it.name.startsWith("META-INF/jarjar/") && it.name.endsWith(".jar") }.forEach { entry ->
            val out = into.resolve(entry.name.substringAfterLast('/'))
            zip.getInputStream(entry).use { input -> out.outputStream().use { input.copyTo(it) } }
            extractJarJar(out, into)
        }
    }
}

fun isModJar(jar: File) = ZipFile(jar).use { it.getEntry("META-INF/neoforge.mods.toml") != null || it.getEntry("META-INF/mods.toml") != null }

loom {
    if (isForge) forge {
        mixinConfig("mixins.${mod.id}.json")
    }
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../run/$minecraft"
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(if (stonecutter.eval(minecraft, ">=1.20.5")) 21 else 17))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    properties(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "authors" to mod.prop("authors"),
        "description" to mod.prop("description"),
        "license" to mod.prop("license"),
        "minecraft" to mod.prop("mc_dep"),
        "loader_range" to mod.prop("loader_range"),
        "pack_format" to mod.prop("pack_format"),
        "tacz" to mod.dep("tacz"),
        "geckolib" to mod.dep("geckolib"),
        "smartbrainlib" to mod.dep("smartbrainlib"),
        "ftbteams_min" to mod.dep("ftbteams_min"),
        "opac_min" to mod.dep("opac_min")
    )
    if (isForge) exclude("META-INF/neoforge.mods.toml", "data/*/recipe/**", "data/*/tags/entity_type/**")
    else exclude("META-INF/mods.toml", "data/*/recipes/**", "data/*/tags/entity_types/**")
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.dir("libs/${mod.version}"))
    dependsOn("build")
}
