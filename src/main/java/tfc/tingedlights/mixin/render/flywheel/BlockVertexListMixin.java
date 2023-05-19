package tfc.tingedlights.mixin.render.flywheel;

import com.jozufozu.flywheel.core.vertex.BlockVertexList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.data.access.flw.VertexListExtension;

import java.nio.ByteBuffer;

@Mixin(BlockVertexList.class)
public abstract class BlockVertexListMixin implements VertexListExtension {
	@Shadow
	protected abstract int vertIdx(int vertexIndex);
	
	@Unique
	private ByteBuffer copyFrom;
	
	@Unique
	private int indx = -1;
	
	@Unique
	private int getIndex(int i) {
		if (indx != -1) return indx;
		return vertIdx(i);
	}
	
	public void markIndex(int indx) {
		this.indx = vertIdx(indx);
	}
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(ByteBuffer copyFrom, int vertexCount, int stride, CallbackInfo ci) {
		this.copyFrom = copyFrom;
	}
	
	@Override
	public byte getLightR(int vertIndex) {
		return this.copyFrom.get(this.getIndex(vertIndex) + 32);
	}
	
	@Override
	public byte getLightG(int vertIndex) {
		return this.copyFrom.get(this.getIndex(vertIndex) + 33);
	}
	
	@Override
	public byte getLightB(int vertIndex) {
		return this.copyFrom.get(this.getIndex(vertIndex) + 34);
	}
}
