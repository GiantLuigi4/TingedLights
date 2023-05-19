package tfc.tingedlights.util;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;

import java.util.Arrays;

public enum BetterAdjacencyInfo {
	DOWN(ModelBlockRenderer.AdjacencyInfo.DOWN, Direction.DOWN),
	UP(ModelBlockRenderer.AdjacencyInfo.UP, Direction.UP),
	NORTH(ModelBlockRenderer.AdjacencyInfo.NORTH, Direction.NORTH),
	SOUTH(ModelBlockRenderer.AdjacencyInfo.SOUTH, Direction.SOUTH),
	WEST(ModelBlockRenderer.AdjacencyInfo.WEST, Direction.WEST),
	EAST(ModelBlockRenderer.AdjacencyInfo.EAST, Direction.EAST),
	;
	
	public Direction[] edges = new Direction[4];
	
	private static final BetterAdjacencyInfo[] valuesCache = values();
	
	public static BetterAdjacencyInfo get(ModelBlockRenderer.AdjacencyInfo info) {
		return valuesCache[info.ordinal()];
	}
	
	BetterAdjacencyInfo(ModelBlockRenderer.AdjacencyInfo fromFacing, Direction facing) {
		Arrays.fill(edges, Direction.NORTH);
		if (facing.equals(Direction.UP)) {
			edges = new Direction[]{
					fromFacing.corners[1],
					fromFacing.corners[0],
					fromFacing.corners[3],
					fromFacing.corners[2],
			};
		} else if (facing.equals(Direction.DOWN)) {
			edges = fromFacing.corners;
		} else if (facing.equals(Direction.EAST)) {
			edges = new Direction[]{
					fromFacing.corners[3],
					fromFacing.corners[2],
					fromFacing.corners[0],
					fromFacing.corners[1],
			};
		} else if (facing.equals(Direction.WEST)) {
			edges = new Direction[]{
					fromFacing.corners[2],
					fromFacing.corners[3],
					fromFacing.corners[1],
					fromFacing.corners[0],
			};
		} else if (facing.equals(Direction.NORTH)) {
			edges = new Direction[]{
					fromFacing.corners[2],
					fromFacing.corners[3],
					fromFacing.corners[1],
					fromFacing.corners[0],
			};
		} else if (facing.equals(Direction.SOUTH)) {
			edges = new Direction[]{
					fromFacing.corners[0],
					fromFacing.corners[1],
					fromFacing.corners[2],
					fromFacing.corners[3],
			};
		}
	}
}
