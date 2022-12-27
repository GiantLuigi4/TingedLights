package tfc.tingedlights.mixin.render.others;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

@Mixin(EntityRenderDispatcher.class)
public class EntityRendererMixin<E extends Entity> {
	@Inject(at = @At("HEAD"), method = "render")
	public void preRender(E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			Vec3 pos = pEntity.getLightProbePosition(pPartialTicks);
			LightManager engine = ((ILightEngine) pEntity.getLevel().getLightEngine()).getManager();
			Color color = LightBlender.blend(pos, engine, pEntity.getLevel());
			extensions.setColorDone(false);
			extensions.setDefault(color);
		}
	}
	
	@Inject(at = @At("RETURN"), method = "render")
	public void postRender(E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(new Color(0, 0, 0));
		}
	}
}
