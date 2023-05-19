package tfc.tingedlights.mixin.render.flywheel;

import com.jozufozu.flywheel.core.vertex.BlockVertexListUnsafe;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import tfc.tingedlights.data.access.flw.VertexListExtension;

@Mixin(value = BlockVertexListUnsafe.class, remap = false)
public abstract class BlockVertexListUnsafeMixin implements VertexListExtension {
	@Shadow
	protected abstract long ptr(long index);
	
	@ModifyConstant(method = "ptr", constant = @Constant(longValue = 32))
	public long swap32(long constant) {
		return constant + 4;
	}
	
	@Override
	public void markIndex(int indx) {
		throw new RuntimeException();
	}
	
	@Override
	public byte getLightR(int vertIndex) {
		return MemoryUtil.memGetByte(this.ptr((long) vertIndex) + 32L);// 42
	}
	
	@Override
	public byte getLightG(int vertIndex) {
		return MemoryUtil.memGetByte(this.ptr((long) vertIndex) + 33L);// 42
	}
	
	@Override
	public byte getLightB(int vertIndex) {
		return MemoryUtil.memGetByte(this.ptr((long) vertIndex) + 34L);// 42
	}
}
