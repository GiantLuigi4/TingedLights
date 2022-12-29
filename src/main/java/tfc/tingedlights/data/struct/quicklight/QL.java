package tfc.tingedlights.data.struct.quicklight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.struct.LightChunk;
import tfc.tingedlights.data.struct.LightNode;
import tfc.tingedlights.data.struct.LightSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class QL {
	private static final Direction[][] ROTATIONS = new Direction[][]{
			new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST},
			new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST},
			new Direction[]{Direction.EAST, Direction.WEST},
			new Direction[]{Direction.EAST, Direction.WEST},
			new Direction[]{Direction.NORTH, Direction.SOUTH},
			new Direction[]{Direction.NORTH, Direction.SOUTH},
	};
	
	public static void fullLight(BlockGetter getter, LightChunk chunk, Light light, Function<BlockPos, BlockState> stateFunction, BiConsumer<BlockPos, LightNode> nodeAdder, Consumer<BlockPos> updateScheduler) {
		LightNode sourceNode = new LightNode(
				chunk, new HashSet<>(), new HashMap<>(),
				light, light.lightValue(), light.position()
		);
		LightSource source = sourceNode.source;
		nodeAdder.accept(sourceNode.pos, sourceNode);
		
		byte v = light.lightValue();
		BlockPos.MutableBlockPos pMut0 = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos pMut1 = new BlockPos.MutableBlockPos();
		for (Direction value : Direction.values()) {
			pMut0.set(
					value.getStepX() * v,
					value.getStepY() * v,
					value.getStepZ() * v
			);
			int ord = value.ordinal();
			for (byte off0 = 1; off0 < v; off0++) {
				for (Direction direction : ROTATIONS[ord]) {
					for (byte off1 = 0; off1 < (v - off0); off1++) {
						pMut1.set(
								pMut0.getX() + direction.getStepX() * v,
								pMut0.getY() + direction.getStepY() * v,
								pMut0.getZ() + direction.getStepZ() * v
						);
						
						BlockState state = stateFunction.apply(pMut1);
						pMut1.setWithOffset(pMut1, light.position());
						
						int lb = state.getLightBlock(getter, pMut1);
						if (lb != 0) {
							pMut1.setWithOffset(pMut1, direction.getOpposite());
							updateScheduler.accept(pMut1);
						} else {
							nodeAdder.accept(
									pMut1,
									new LightNode(
											chunk, source,
											off1, pMut1.immutable()
									)
							);
						}
					}
				}
			}
		}
	}
}
