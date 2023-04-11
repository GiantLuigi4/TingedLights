# TingedLights

A 100% client side approach to vanilla-style colored lights

![740C3F9B-D1FF-4460-BF85-10AC554A9679](https://user-images.githubusercontent.com/49770992/230512195-6a2ad1ba-c090-4346-94f8-e40fa6a2f7b8.png)
![C8B10D9C-2CC4-48BC-B93B-C1941F24473C](https://user-images.githubusercontent.com/49770992/230509891-885ef0bf-9c1b-4528-9de1-6158cc91d511.png)
![D081ADFE-819F-4757-A8D8-43954B54ED2F](https://user-images.githubusercontent.com/49770992/230509894-81ba166a-03fb-43ab-b1f9-0f6550df5b28.png)

## But why?
After all, Shimmer and Colored Lights both already exist

Shimmer and Colored Lights both don't use lighting engines for their colored lights
They simply use a lighting manager, and tint vanilla's light based on distance

Tinged Lights however, used a lighting engine (or well, currently multiple engines)
This allows lights to properly be obstructed by walls instead of either having light go through, or having colors go through

In the future, when I have a more custom lighting engine, This would also allow me to have glass tint the light color

## Finding a good light color?
In my experience, good light colors come from
   A) a highlight color for a typical block (such as amethyst)
   B) a midtone for a moody block (such as lava)
   C) a modified highlight for a vibetant block (such as glowstone, which uses a highlight but with the blue value dropped to 10, to get a very defined gold)

## So what else does this mod do?
The mod also changes some stuff about block rendering, implementing vertex sorting, for sake of having smooth lighting look more smooth, and also having amambient occlusion look a bit more consistent

All of the vertex sorting options are on configs

## A place to talk
I have a [discord server](https://discord.gg/qFEBSsm), in which my mods are discussed
