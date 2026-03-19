# Cobblemon

An open-source Pokémon mod for Minecraft Java Edition, written in Kotlin for Fabric and NeoForge.

## Getting started

To set up the development workspace, first clone the project and open the build.gradle with Intellij. Make sure that you clone it to a folder that has no spaces in its path (for example, C:/Development/Cobblemon Stuff/cobblemon/ is bad) since Architectury Plugin seems to dislike it.

After it takes ages to load, you should hopefully have runnable configurations of the project in the top right, such as Minecraft Client (:fabric). If not, try running `./gradlew genEclipseRuns`.

Troubleshooting:
- Try running `./gradlew --refresh-dependencies`
- Try File -> Invalidate Caches.
- Try deleting the `.idea` folder in the project root (make sure IntelliJ is closed when you try it).
- Try completely reclone the thing lmao.

## Contributing

If you're interested in contributing to the project, you can simply fork the repository and comment on any of the issues 
[here](https://gitlab.com/cable-mc/cobblemon/-/issues?scope=all&state=opened&label_name[]=accepted&assignee_id=None).

If you want a more involved role in the project, involving branches on the main repository instead of forking and involvement
in design discussions, you can either apply in the `application-box` forum of our Discord or add and message Hiroku (`hiroku_dev`) on our Discord here: https://discord.gg/cobblemon.
