plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.9.436" apply false
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

for (node in stonecutter.tree.nodes) {
    if (node.metadata != stonecutter.current) continue
    for (type in listOf("Client", "Server")) tasks.register("runActive$type") {
        group = "project"
        dependsOn("${node.hierarchy}:run$type")
    }
}
