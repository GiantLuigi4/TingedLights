package tfc.tingedlights.mixin.render.others;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

import java.util.Iterator;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Shadow
	protected ClientLevel level;
	
	@Inject(at = @At(shift = At.Shift.BEFORE, value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
	public void preDrawParticle(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks, Frustum clippingHelper, CallbackInfo ci, PoseStack posestack, Iterator var8, ParticleRenderType particlerendertype, Iterable iterable, Tesselator tesselator, BufferBuilder bufferbuilder, Iterator var13, Particle particle) {
		LightManager manager = ((ILightEngine) level.getLightEngine()).getManager();
		Color c = LightBlender.blend(
				new Vec3(particle.x, particle.y, particle.z),
				manager, level
		);
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(c);
		}
		if (bufferbuilder instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(c);
		}
	}
	
	// TODO: this can probably be @TAIL
	@Inject(at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
	public void postDrawParticle(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks, Frustum clippingHelper, CallbackInfo ci, PoseStack posestack, Iterator var8, ParticleRenderType particlerendertype, Iterable iterable, Tesselator tesselator, BufferBuilder bufferbuilder, Iterator var13, Particle particle) {
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(new Color(0, 0, 0));
		}
		if (bufferbuilder instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(new Color(0, 0, 0));
		}
	}
}
