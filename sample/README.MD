# Making a resource pack

## The format
You can define as many light types and providers in one JSON as you want
The JSONs have two main entries, a list of light types, and a list of providers
Light types define properties about a light, such as color, distance fade, fade color, etc
Light providers define what light type a block should use, and what brightness it should be

Light types are only defined for the json which they are defined in

## Light types
**TODO**

## Finding a good light color
In my experience, good light colors come from
   - a highlight color for a typical block (such as amethyst)
   - a midtone for a moody block (such as lava)
   - a modified highlight for a vibetant block (such as glowstone, which uses a highlight but with the blue value dropped to 10, to get a very defined gold)

But sometimes, the ideal color has to just be guessed (such as a redstone torch, which I wanted to be extremely red so it can set a mood better than any color on the torch)

## Notes
If two light sources have equivalent light types, they will not be able to add together, so having a redstone torch next to another redstone torch won't make the light any brighter
The reason for this, is because each unique light type functionally gets its own light engine
This means that it can't track when multiple light sources of the same type are lighting the same block (or at least, not yet, but I believe RAM would become problematic if it could)
This also means that the mod takes increasing amounts of RAM for each light type
So do try to keep the light types down, but it should still be able to withstand a fair amount
However, hopefully in the future, these notes will change
