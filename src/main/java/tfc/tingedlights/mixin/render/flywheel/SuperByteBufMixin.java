package tfc.tingedlights.mixin.render.flywheel;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.ColoredLightEngine;
import tfc.tingedlights.data.access.flw.VertexListExtension;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

@Mixin(value = SuperByteBuffer.class, remap = false)
public class SuperByteBufMixin {
	@Shadow
	@Final
	private VertexList template;
	@Shadow
	@Final
	private PoseStack transforms;
	@Shadow
	private Matrix4f lightTransform;
	@Shadow private boolean useWorldLight;
	@Shadow private boolean hasCustomLight;
	@Unique
	private int index;
	
	@Unique
	private VertexListExtension extension;
	
	@Unique
	private final BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
	@Unique
	private final Vector4f lightPos = new Vector4f();
	@Unique
	private VertexBufferConsumerExtensions bufferExtensions;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(BufferBuilder buf, CallbackInfo ci) {
		if (template instanceof VertexListExtension extension)
			this.extension = extension;
	}
	
	@Unique
	private static final Long2IntMap WORLD_LIGHT_CACHE = new Long2IntOpenHashMap();
	
	@Inject(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2IntMap;clear()V"), method = "renderInto")
	public void preClear(PoseStack input, VertexConsumer builder, CallbackInfo ci) {
		WORLD_LIGHT_CACHE.clear();
	}
	
	@Inject(at = @At("HEAD"), method = "renderInto")
	public void preStartRender(PoseStack input, VertexConsumer builder, CallbackInfo ci) {
		index = 0;
		if (builder instanceof VertexBufferConsumerExtensions extensions)
			this.bufferExtensions = extensions;
		else this.bufferExtensions = null;
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;endVertex()V"), method = "renderInto")
	public void preEnd(PoseStack input, VertexConsumer builder, CallbackInfo ci) {
		Matrix4f localTransforms = this.transforms.last().pose();
		
		if (extension != null && bufferExtensions != null) {
			float x = this.template.getX(index);
			float y = this.template.getY(index);
			float z = this.template.getZ(index);
			
			extension.markIndex(index);
			
			Color worldColor = Color.BLACK;
			if (useWorldLight || hasCustomLight) {
				lightPos.set((x - 0.5F) * 15.0F / 16.0F + 0.5F, (y - 0.5F) * 15.0F / 16.0F + 0.5F, (z - 0.5F) * 15.0F / 16.0F + 0.5F, 1.0F);
				lightPos.transform(localTransforms);
				if (this.lightTransform != null)
					lightPos.transform(this.lightTransform);
				
				worldColor = Color.fromInt(getLight(Minecraft.getInstance().level, lightPos));
			}
			
			int r = extension.getLightR(index);
			if (r < 0) r += 255;
			int g = extension.getLightG(index);
			if (g < 0) g += 255;
			int b = extension.getLightB(index);
			if (b < 0) b += 255;
			r = 0;
			g = 0;
			b = 0;
			
			r = (int) Math.max(r, worldColor.r() * 255);
			g = (int) Math.max(g, worldColor.g() * 255);
			b = (int) Math.max(b, worldColor.b() * 255);
			
			bufferExtensions.setDefault(Color.fromRGB(r, g, b));
			index++;
		}
	}
	
	@Unique
	private int getLight(Level world, Vector4f lightPos) {
		bp.set(lightPos.x(), lightPos.y(), lightPos.z());// 474
		return WORLD_LIGHT_CACHE.computeIfAbsent(bp.asLong(), $ -> {
			LevelLightEngine levelLightEngine = world.getLightEngine();
			if (levelLightEngine instanceof ColoredLightEngine engine) {
				bp.set(lightPos.x(), lightPos.y(), lightPos.z());
				Color color = engine.getColor(bp);
				return color.packInt();
			}
			return 0;
		});// 475
	}
}
