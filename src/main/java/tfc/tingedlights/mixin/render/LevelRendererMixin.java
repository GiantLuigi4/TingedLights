package tfc.tingedlights.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.api.DynamicLightApi;
import tfc.tingedlights.util.OnThread;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow @Nullable private ClientLevel level;
	
	// I would hope this doesn't get overwritten
	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void preRenderLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
//		if (level instanceof LevelWithColoredLightSupport lightSupport) {
//			LightManager manager = lightSupport.getManager();
//			manager.tick();
//		}
		DynamicLightApi.tick();
		OnThread.run();
	}
}
