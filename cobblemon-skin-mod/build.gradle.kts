import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.ow2.asm:asm:9.7") }
}

plugins {
    id("fabric-loom") version "1.8.9"
    kotlin("jvm") version "2.0.20"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

// ── Strip @kotlin.Metadata from class entries ────────────────────────────────
// Cobblemon 1.7.3 is compiled with Kotlin 2.2.x (metadata version [2,2,0]).
// Loom 1.8.9's tiny-remapper uses kotlinx-metadata-jvm ≤ 2.0.x which refuses
// to write metadata for "future" versions. We strip the annotation entirely so
// tiny-remapper treats each class as plain Java → remap succeeds cleanly.
// Cobblemon API is then accessed via Java interop (INSTANCE.getXxx()) in code.
fun stripKotlinMetadata(bytes: ByteArray): ByteArray {
    val reader = ClassReader(bytes)
    val writer = ClassWriter(0)
    val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? =
            if (descriptor == "Lkotlin/Metadata;") null
            else super.visitAnnotation(descriptor, visible)
    }
    reader.accept(visitor, 0)
    return writer.toByteArray()
}

// ── Publish a stripped Cobblemon JAR to a local Maven repo ───────────────────
// We need modCompileOnly via Maven coordinates so loom remaps intermediary→Mojmap.
// We also remove embedded Minecraft/Mojang/FabricAPI bytecode and all client-side
// Cobblemon classes to avoid classpath conflicts.
// After stripping, only com/cobblemon/** server-side API remains.
run {
    val cobblemonJars = fileTree("libs") { include("*.jar") }.files
    if (cobblemonJars.isNotEmpty()) {
        val src = cobblemonJars.first()
        val repo = layout.buildDirectory.dir("local-cobblemon-maven").get().asFile
        val dir = File(repo, "com/cobblemon/cobblemon-fabric/local")
        dir.mkdirs()
        val strippedJar = File(dir, "cobblemon-fabric-local.jar")

        // Prefixes to STRIP from the JAR
        val stripPrefixes = listOf(
            "net/minecraft/",
            "com/mojang/",
            "net/fabricmc/fabric/",
            "net/fabricmc/loader/",
            "io/netty/",
            "org/lwjgl/",
            "com/google/gson/",
            "com/google/common/",
            "org/apache/"
        )

        ZipFile(src).use { zf ->
            ZipOutputStream(strippedJar.outputStream()).use { zos ->
                zf.entries().asSequence()
                    .filter { entry ->
                        stripPrefixes.none { prefix -> entry.name.startsWith(prefix) }
                    }
                    .forEach { entry ->
                        try {
                            zos.putNextEntry(ZipEntry(entry.name))
                            if (!entry.isDirectory) {
                                val raw = zf.getInputStream(entry).use { it.readBytes() }
                                val out = if (entry.name.endsWith(".class")) stripKotlinMetadata(raw) else raw
                                zos.write(out)
                            }
                            zos.closeEntry()
                        } catch (_: ZipException) { /* skip duplicates */ }
                    }
            }
        }

        File(dir, "cobblemon-fabric-local.pom").writeText(
            """<?xml version="1.0" encoding="UTF-8"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.cobblemon</groupId>
  <artifactId>cobblemon-fabric</artifactId>
  <version>local</version>
  <packaging>jar</packaging>
</project>"""
        )
    }
}

repositories {
    maven {
        url = uri(layout.buildDirectory.dir("local-cobblemon-maven").get().asFile)
        content { includeGroup("com.cobblemon") }
    }
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.terraformersmc.com/")
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")

    // Cobblemon via local Maven so loom remaps intermediary → Yarn properly.
    modCompileOnly("com.cobblemon:cobblemon-fabric:local") {
        isTransitive = false
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    inputs.property("fabric_kotlin_version", project.property("fabric_kotlin_version"))

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
