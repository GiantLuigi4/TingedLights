package tfc.tingedlights.mixin.render.others;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

@Mixin(GameRenderer.class)
public class FirstPersonHandMixin {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	
	@Shadow
	@Final
	public ItemInHandRenderer itemInHandRenderer;
	
	@Shadow
	@Final
	private Minecraft minecraft;
	
	@Inject(at = @At("HEAD"), method = "renderItemInHand")
	public void preRender(PoseStack pMatrixStack, Camera pActiveRenderInfo, float pPartialTicks, CallbackInfo ci) {
		MultiBufferSource pBuffer = this.renderBuffers.bufferSource();
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			Entity pEntity = this.minecraft.player;
			Vec3 pos = pEntity.getLightProbePosition(pPartialTicks);
			LightManager engine = ((LightManager) pEntity.getLevel());
			Color color = LightBlender.blend(pos, engine, pEntity.getLevel());
			extensions.setColorDone(false);
			extensions.setDefault(color);
		}
	}
	
	@Inject(at = @At("TAIL"), method = "renderItemInHand")
	public void postRender(PoseStack pMatrixStack, Camera pActiveRenderInfo, float pPartialTicks, CallbackInfo ci) {
		MultiBufferSource pBuffer = this.renderBuffers.bufferSource();
		if (pBuffer instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(new Color(0, 0, 0));
		}
	}
}
