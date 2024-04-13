Minecraft Camera
================

Minecraft Camera plugin for Spigot. Adds craft-able cameras to your Minecraft server and the ability to take pictures. 

This is an updated version of the [Cameras plugin by Cup0fCode](https://github.com/Cup0fCode/Cameras/). Tested on Minecraft 1.20.4.

This version contains 2 major performance improvements:

* **Initializing the plugin at server boot is much faster**<br>
The color palette is now loaded from a config file. Previously a zipped file containing textures for all block types was downloaded,
unzipped - then 1 pixel from each texture was picked determine the color.

* **Taking pictures is much more performant**<br>
Rendering is new distributed across ticks. Previously rendering would make the main thread hang, and the whole server would lag. 


Other improvements:

* In Overworld the sky color changes depending on the time of day.
* Amount of rendered pixels per ticks can be adjusted through the config.
* Render distance can be adjusted through the config.
* Camera skin can be changed through the config.
* Updated colors for newer block types.
* All logging is prefixed with `[Camera]`.

## Pictures

<img src="https://i.imgur.com/Bzi99fL.png" width="225"> <img src="https://i.imgur.com/YRiBxGn.png" width="225"> <img src="https://i.imgur.com/pstXzfc.png" width="225">

## Installation

1. [Download the plugin](https://github.com/SnorklingTurtle/Minecraft-Camera/releases/)
2. Move the jar-file into the server plugin-folder
3. Start the server

If you don't already have a config file, one will be created on first boot of the server. The config is located at `plugins/Minecraft-Camera/config.yml`. Restart the server after editing the config. 

Images are saved as txt-files in `plugins/Minecraft-Camera/maps`. Each file is usually at a size of 3-12 KB.

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
```

## Usage
Players can craft cameras using the following recipe:

![crafting](https://i.imgur.com/GsrxLPY.png)

Operators on the server (`/op <username>`) can take pictures with the command `/takepicture`. When using this command a camera and paper is 
not required, however you still need space in your inventory.

## Limitations

* Each surface displays just one color. If you see any colors that feels off, let me know.
* Players and mobs won't show up on pictures - *X-Files theme plays*
* Image are distorted when looking up/down.

## Todo

* [QA] Test with Geyser
* [QA] See if permissions works as intended
* [Bug] Water seems to be transparent even with `transparentWater: false`
* [Optimize] Can converting colors from string be improved
* [Optimize] See if loading maps at boot can be improved
* [Feature] Add option to place camera on the ground (might conflict with claims plugin).

## Done

* [Bug] When shooting multiple times, subsequent pictures will stop rendering when the first one finishes
* [Feature] Let sky color depend on time of day
* [Optimize] Set default render distance to 128 (instead of 256)
* [Feature] Allow changing render distance from config
* [Optimize] Don't take picture, when cancelling placement of camera.
* [Optimize] No need to read all every single image at server boot. Read colors from a color-mapping.config instead
* [Optimize] Prefix all console logging with `[Camera]`
* [Optimize] Render X amount of pixels per tick, for better performance
* [Bug] It's possible to take pictures with a full inventory using `/takepicture`
* [Optimize] ~~See if it's possible to remove despawned images from the `maps` folder~~

## Issues

Any features requests or issues should be made through here:

https://github.com/SnorklingTurtle/Minecraft-Camera/issues
