plugins {
    id("cobblemon.root-conventions")
}

version = "${project.property("mod_version")}+${project.property("mc_version")}"

val isSnapshot = project.property("snapshot")?.equals("true") == true
if (isSnapshot) {
    val fixedBranchName = versioning.info.branch.substringAfter("/")
    version = "$version-${fixedBranchName}-${versioning.info.build}"
}
