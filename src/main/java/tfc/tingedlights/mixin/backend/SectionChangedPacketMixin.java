package tfc.tingedlights.mixin.backend;

import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public class SectionChangedPacketMixin {
	@Inject(at = @At("HEAD"), method = "shouldSuppressLightUpdates", cancellable = true)
	public void preCheckSuppressLight(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
