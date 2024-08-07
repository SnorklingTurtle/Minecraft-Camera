Minecraft Camera
================

Minecraft Camera plugin for Spigot. Adds craft-able cameras to your Minecraft server and the 
ability to take pictures. Works with Geyser, though the camera looks like a Steve head.

This is an updated version of the [Cameras plugin by Cup0fCode](https://github.com/Cup0fCode/Cameras/). Tested on Minecraft 1.21. 

This version contains 3 major performance improvements:

* **Initializing the plugin at server boot is much faster**<br>
The color palette is now loaded from a config file. Previously a zipped file containing textures for all block types was downloaded,
unzipped - then 1 pixel from each texture was picked to determine the color.

* **Taking pictures is much more performant**<br>
Rendering is now distributed across ticks. Previously rendering would make the main thread hang, and the whole server would lag. 

* **Pictures are compressed when stored**<br>
Each picture is usually at a size of 0.5-3 KB (3-30 KB without compression).


Other improvements:

* In Overworld the sky color changes depending on the time of day.
* Amount of rendered pixels per ticks can be adjusted through the config.
* Render distance can be adjusted through the config.
* Players can copy a picture to clipboard.
* Players can tag a picture and later fetch it by command - even across seeds.
* Camera skin can be changed through the config.
* Pictures that despawned or has been thrown into lava, will be removed from the database.
* Updated colors for newer block types.
* All logging is prefixed with `[Minecraft-Camera]`.

## Pictures

<img src="https://i.imgur.com/Bzi99fL.png" width="225" alt=""> <img src="https://i.imgur.com/YRiBxGn.png" width="225" alt=""> <img src="https://i.imgur.com/pstXzfc.png" width="225" alt="">

## Installation

1. [Download the plugin](https://github.com/SnorklingTurtle/Minecraft-Camera/releases/)
2. Move the jar-file into the server plugin-folder
3. Start the server

If you don't already have a config file, one will be created on first boot of the server. The config is located at `plugins/Minecraft-Camera/config.yml`. Restart the server after editing the config. 

Pictures are stored in a SQLite database at `plugins/Minecraft-Camera/pictures.db`. 

## Permissions

To allow players to craft and use the camera you must set permissions for at least `cameras.craft` and 
`cameras.useitem` and optionally `cameras.command`. You can either set the permissions in 
permissions.yml or through LuckPerms. Other permissions plugins might work as well, but haven't been tested.

Here's an example for permissions.yml:

```yml
cameras.craft:
  description: Allow crafting camera
  default: true
cameras.useitem:
  description: Allow using the camera
  default: false
cameras.command:
  description: Allow the command /takepicture
  default: op
cameras.usepaper:
  description: Allow taking pictures without paper
  default: true
```

## Usage
Players can craft cameras using the following recipe:

![crafting](https://i.imgur.com/GsrxLPY.png)

### Commands

* `/tagpicture <tag>` - This is intended for servers that resets their world seed every season, but still want to let players retrieve pictures from the last season. 
* `/fetchpicture <tag>` - Allows players to fetch a picture by its tag (see above).
* `/copypicture` - Lets you copy a picture to clipboard.
* `/takepicture` - Take pictures using command line. Camera not required.


## Limitations

* Each block displays just one color.
* Players and mobs won't show up on pictures - *X-Files theme plays*
* Pictures are distorted when looking up/down.

## Todo

* [Idea] Use world.getViewDistance() for render distance
* [Idea] Use getBlockData().getMapColor() and fallback to color-mapping.config only if needed
* [Optimize] Incorrect colors
* [Optimize] Can converting colors from string be improved
* [Optimize] Water seems to be transparent even with `transparentWater: false`
* [Idea] Prettier sky
* [Idea] Framing

## Done

* [Idea] Allow players to copy pictures to clipboard
* [Bug] When shooting multiple times, subsequent pictures will stop rendering when the first one finishes
* [QA] See if permissions works as intended
* [Feature] Let sky color depend on time of day
* [Optimize] Improve loading maps
* [Optimize] Set default render distance to 128 (instead of 256)
* [Feature] Allow changing render distance from config
* [Optimize] Don't take picture, when cancelling placement of camera.
* [Optimize] No need to read all every single image at server boot. Read colors from a color-mapping.config instead
* [Optimize] Prefix all console logging with `[Camera]`
* [Optimize] Render X amount of pixels per tick, for better performance
* [Bug] It's possible to take pictures with a full inventory using `/takepicture`
* [Optimize] See if it's possible to remove despawned images from the `maps` folder
* [Feature] ~~Add option to place camera on the ground (might conflict with claims plugin).~~

## Issues

Any features requests or issues should be made through here:

https://github.com/SnorklingTurtle/Minecraft-Camera/issues
