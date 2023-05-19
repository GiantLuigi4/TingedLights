package tfc.tingedlights.mixin.render.flywheel;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.vertex.BlockWriterUnsafe;
import com.jozufozu.flywheel.core.vertex.VertexWriterUnsafe;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.data.access.flw.VertexListExtension;

import java.nio.ByteBuffer;

@Mixin(value = BlockWriterUnsafe.class, remap = false)
public abstract class BlockWriterUnsafeMixin<V extends VertexType> extends VertexWriterUnsafe<V> {
	@Inject(at = @At(value = "INVOKE", target = "Lcom/jozufozu/flywheel/core/vertex/BlockWriterUnsafe;putVertex(FFFFFBBBBIFFF)V", shift = At.Shift.BEFORE), method = "writeVertex")
	public void postWriteVertex(VertexList list, int i, CallbackInfo ci) {
		if (list instanceof VertexListExtension extension) {
			MemoryUtil.memPutByte(this.ptr + 32L, extension.getLightR(i));// 52
			MemoryUtil.memPutByte(this.ptr + 33L, extension.getLightG(i));// 52
			MemoryUtil.memPutByte(this.ptr + 34L, extension.getLightB(i));// 52
			MemoryUtil.memPutByte(this.ptr + 35L, Byte.MAX_VALUE);// 52
		} else {
			MemoryUtil.memPutByte(this.ptr + 32L, Byte.MAX_VALUE);// 52
			MemoryUtil.memPutByte(this.ptr + 33L, Byte.MAX_VALUE);// 52
			MemoryUtil.memPutByte(this.ptr + 34L, Byte.MAX_VALUE);// 52
			MemoryUtil.memPutByte(this.ptr + 35L, Byte.MAX_VALUE);// 52
		}
	}
	
	@Inject(at = @At("TAIL"), method = "putVertex")
	public void postPut(float x, float y, float z, float u, float v, byte r, byte g, byte b, byte a, int light, float nX, float nY, float nZ, CallbackInfo ci) {
		ptr += 4;
	}
	
	public BlockWriterUnsafeMixin(V type, ByteBuffer buffer) {
		super(type, buffer);
	}
}
