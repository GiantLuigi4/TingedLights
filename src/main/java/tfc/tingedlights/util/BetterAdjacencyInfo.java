package tfc.tingedlights.util;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;

import java.util.Arrays;

public class BetterAdjacencyInfo {
	public Direction[] edges = new Direction[4];
	
	public BetterAdjacencyInfo(ModelBlockRenderer.AdjacencyInfo fromFacing, Direction facing) {
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
