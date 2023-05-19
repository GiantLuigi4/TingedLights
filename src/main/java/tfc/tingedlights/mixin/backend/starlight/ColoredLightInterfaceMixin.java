package tfc.tingedlights.mixin.backend.starlight;

import org.spongepowered.asm.mixin.Mixin;
import tfc.tingedlights.util.asm.annotation.Hook;
import tfc.tingedlights.util.starlight.ColoredLightInterface;

@Mixin(ColoredLightInterface.class)
@Hook(ColoredLightInterface.class)
public class ColoredLightInterfaceMixin {
	// dummy mixin: allows me to switch which class the colored starlight engine extends
}
