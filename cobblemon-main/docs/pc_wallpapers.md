### PC Box Wallpapers

PC box wallpapers have two sides, one on client and an optional one on server.

#### Client Side
This involves adding the texture PNG file to the `assets/cobblemon/textures/gui/pc/wallpaper/` directory. The mod will trawl
this directory on asset load and form a base list. The client will send this list to the server to validate
the list, with the server returning a list of valid wallpapers. The client will then display the wallpapers in the GUI.
The emissive screen glow is an optional asset. To add one, place the file in the `assets/cobblemon/textures/gui/pc/wallpaper/*/glow` directory with the same file name as the wallpaper.
An optional, alternate wallpaper can also be provided. This should be similar to the main wallpaper as it will share the custom glow if one is provided. This alternate wallpaper will be shown, replacing the main wallpaper, when holding the Shift key while the wallpaper selection list is open in the PC interface. To add an alternate wallpaper, place the file in the `assets/cobblemon/textures/gui/pc/wallpaper/*/alt` directory with the same file name as the main wallpaper.


#### Server Side
A datapack folder exists called `unlockable_pc_box_wallpapers`. This contains JSONs of a simplistic format.

For example, you could create a JSON file called `some_texture.json` with the following contents:

    {
        "enabled": true,
        "displayName": "Something",
        "texture": "cobblemon:textures/gui/pc/wallpaper/some_texture.png"
    }

The purpose of having a datapacked JSON for wallpapers is for when the wallpaper should not be immediately available. Any
wallpaper that is in the client's resources but is not in the server's datapack will be available in the GUI immediately
and will not be validated against when the client asks to use that wallpaper. But if the wallpaper is in the server's
datapack, the client will only see the wallpaper if they have specifically unlocked it.

The `enabled` field is used when you want to actively prevent a specific texture from being used. For example, if there
is a wallpaper supplied by Cobblemon that you want to remove, setting this to false will prevent its use despite existing
in the client's resources.

The `displayName` field is used to provide a name in the toast when the wallpaper is unlocked. It can be a lang key or text. If it is left out or null, the toast
will instead insert '???' in place of the name.

For enabled, datapacked wallpapers, the server will build a list of which of these have been unlocked by the player.
Unlocking can be done from MoLang scripts using `q.player.pc.unlock_wallpaper(id, [shouldNotify])`, from 
`/unlockpcboxwallpaper <player> <wallpaper>`, and from code unlocks. 

For MoLang and command unlocks, references to the wallpaper use the JSON's file ID, not the texture. For example, in the 
above example, unlocking from the command would be `/unlockpcboxwallpaper <player> cobblemon:some_texture`. 

Similarly, using the MoLang script would be `q.player.pc.unlock_wallpaper("cobblemon:some_texture", true)`. The second 
parameter is a boolean that determines if the player should be notified of this new texture using a toast.

A datapacked wallpaper results in a validation check when the client asks the service to apply a texture. If the texture
is mentioned by a datapacked wallpaper, and the player has not unlocked that ID, the client will be told that it is not
a usable wallpaper, and it will not be displayed until it is unlocked.
