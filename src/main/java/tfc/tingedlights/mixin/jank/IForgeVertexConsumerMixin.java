package tfc.tingedlights.mixin.jank;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfc.tingedlights.data.access.VertexFormatAccess;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(IForgeVertexConsumer.class)
public interface IForgeVertexConsumerMixin {
	@Shadow int applyBakedLighting(int packedLight, ByteBuffer data);
	
	@Shadow void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform);
	
	// TODO: ideally would be a mixin plugin
	/**
	 * @author
	 */
	@Overwrite(remap = false)
	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readExistingColor) {
		int[] aint = bakedQuad.getVertices();
		Vec3i faceNormal = bakedQuad.getDirection().getNormal();
		Vector3f normal = new Vector3f((float) faceNormal.getX(), (float) faceNormal.getY(), (float) faceNormal.getZ());
		Matrix4f matrix4f = pose.pose();
		normal.transform(pose.normal());
		int intSize = 8;
		int vertexCount = aint.length / intSize;
		
		try (MemoryStack memorystack = MemoryStack.stackPush()) {
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();
			
			for (int v = 0; v < vertexCount; ++v) {
				((Buffer) intbuffer).clear();
				intbuffer.put(aint, v * 8, 8);
				float f = bytebuffer.getFloat(0);
				float f1 = bytebuffer.getFloat(4);
				float f2 = bytebuffer.getFloat(8);
				float cr;
				float cg;
				float cb;
				float ca;
				if (readExistingColor) {
					float r = (float) (bytebuffer.get(12) & 255) / 255.0F;
					float g = (float) (bytebuffer.get(13) & 255) / 255.0F;
					float b = (float) (bytebuffer.get(14) & 255) / 255.0F;
					float a = (float) (bytebuffer.get(15) & 255) / 255.0F;
					cr = r * baseBrightness[v] * red;
					cg = g * baseBrightness[v] * green;
					cb = b * baseBrightness[v] * blue;
					ca = a * alpha;
				} else {
					cr = baseBrightness[v] * red;
					cg = baseBrightness[v] * green;
					cb = baseBrightness[v] * blue;
					ca = alpha;
				}
				
				int lightmapCoord = this.applyBakedLighting(lightmap[v], bytebuffer);
				float f9 = bytebuffer.getFloat(16);
				float f10 = bytebuffer.getFloat(20);
				Vector4f pos = new Vector4f(f, f1, f2, 1.0F);
				pos.transform(matrix4f);
				this.applyBakedNormals(normal, bytebuffer, pose.normal());
				((VertexConsumer) this).vertex(pos.x(), pos.y(), pos.z(), cr, cg, cb, ca, f9, f10, packedOverlay, lightmapCoord, normal.x(), normal.y(), normal.z());
			}
		}
	}
}
