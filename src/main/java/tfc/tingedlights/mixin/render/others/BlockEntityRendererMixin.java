package tfc.tingedlights.mixin.render.others;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
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

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRendererMixin<E extends BlockEntity> {
	@Inject(at = @At("HEAD"), method = "render")
	public void preRender(E pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci) {
		if (pBufferSource instanceof VertexBufferConsumerExtensions extensions) {
			BlockPos pos = pBlockEntity.getBlockPos();
			LightManager engine = ((ILightEngine) pBlockEntity.getLevel().getLightEngine()).getManager();
			Color color = engine.getColor(new BlockPos(pos));
			extensions.setColorDone(false);
			extensions.setDefault(color);
		}
	}
	
	@Inject(at = @At("RETURN"), method = "render")
	public void postRender(E pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, CallbackInfo ci) {
		if (pBufferSource instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setColorDone(false);
			extensions.setDefault(new Color(0, 0, 0));
		}
	}
}
