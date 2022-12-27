package tfc.tingedlights.data.struct;

import net.minecraft.core.BlockPos;
import tfc.tingedlights.api.data.Light;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LightNode {
	public final LightChunk chunk;
	public final LightSource source;
	private byte v;
	public final BlockPos pos;
	public int hash = -1;
	
	public LightNode(LightChunk chunk, LightSource source, byte v, BlockPos pos) {
		this.chunk = chunk;
		this.source = source;
		this.v = v;
		this.pos = pos;
		hash = -1;
		hash = hashCode();
	}
	
	public LightNode(LightChunk chunk, Set<LightNode> children, Map<BlockPos, LightNode> system, Light light, byte brightness, BlockPos pos) {
		this.chunk = chunk;
		this.source = new LightSource(
				this, children,
				system, light
		);
		this.v = brightness;
		this.pos = pos;
		hash = -1;
		hash = hashCode();
	}
	
	public Light getLight() {
		return source.light();
	}
	
	public void add() {
		chunk.addNode(clampedPos(new BlockPos.MutableBlockPos()), this);
	}
	
	public void remove() {
		system().remove(pos);
		source.childNodes().remove(this);
		
		if (source.reference() == this)
			for (LightNode childNode : source.childNodes().toArray(new LightNode[0]))
				childNode.remove();
		
		chunk.removeNode(clampedPos(new BlockPos.MutableBlockPos()), this);
	}
	
	public void addChild(LightNode node) {
		if (node.source.equals(this.source)) {
			source.childNodes().add(node);
			getSystem().put(node.pos, node);
		}
	}
	
	protected Map<BlockPos, LightNode> getSystem() {
		return source.system();
	}
	
	public BlockPos clampedPos(BlockPos.MutableBlockPos pos) {
		return pos.set(this.pos.getX() & 15, this.pos.getY(), this.pos.getZ() & 15);
	}
	
	@Override
	public String toString() {
		return "LightNode{" +
				", v=" + v +
				", pos=" + pos +
				'}';
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LightNode node = (LightNode) o;
		return Objects.equals(pos, node.pos) && Objects.equals(reference().pos, node.reference().pos);
	}
	
	@Override
	public int hashCode() {
		if (hash == -1)
			return hash = Objects.hash(source.light(), pos, reference().pos);
		return hash;
	}
	
	public void setBrightness(int v) {
		this.v = (byte) v;
	}
	
	public Map<BlockPos, LightNode> system() {
		return source.system();
	}
	
	public LightNode reference() {
		return source.reference();
	}
	
	public Light light() {
		return source.light();
	}
	
	public byte brightness() {
		return v;
	}
}
