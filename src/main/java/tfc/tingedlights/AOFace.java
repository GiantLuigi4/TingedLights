package tfc.tingedlights;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.util.BetterAdjacencyInfo;

import java.util.BitSet;

public class AOFace {
	BakedQuad bakedQuad;
	float[] pShape;
	
	public AOFace(BakedQuad bakedQuad, float[] pShape) {
		this.bakedQuad = bakedQuad;
		this.pShape = pShape;
	}
	
	Color[] colors = null;
	boolean[] dimmed = null;
	
	public void calculate(Direction pDirection, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, BitSet pShapeFlags) {
		BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
		LightManager manager = (LightManager) pLevel.getLightEngine();
		
		BetterAdjacencyInfo adjacency = new BetterAdjacencyInfo(ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection), pDirection);
		
		Vec3[] vertices = new Vec3[4];
		for (int i = 0; i < 4; i++) {
			int index = i * 8;
			vertices[i] = new Vec3(
					Float.intBitsToFloat(bakedQuad.getVertices()[index + 0]),
					Float.intBitsToFloat(bakedQuad.getVertices()[index + 1]),
					Float.intBitsToFloat(bakedQuad.getVertices()[index + 2])
			);
		}
		
		Color none = new Color(0, 0, 0);
		
		Color fallback = LightBlender.getLight(pLevel, manager, blockpos, none);
		
		BlockPos.MutableBlockPos posMut = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
		
		colors = new Color[4];
		dimmed = new boolean[4];
		for (int i = 0; i < vertices.length; i++) {
			colors[i] = LightBlender.blend(
					vertices[i]
							.subtract(0.5, 0.5, 0.5)
							.multiply(
									.1 * Math.abs(pDirection.getStepX()) + 1,
									.1 * Math.abs(pDirection.getStepY()) + 1,
									.1 * Math.abs(pDirection.getStepZ()) + 1
							)
							.add(0.5, 0.5, 0.5)
							.add(pPos.getX(), pPos.getY(), pPos.getZ())
					,
					manager, pLevel
			);
			
			BlockState state;
			int lb;
			
			// TODO: fix corners
			if (i == 0) {
				offset.set(0, 0, 0).move(adjacency.edges[0]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[3]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[3]).move(adjacency.edges[0]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
			}
			if (i == 1) {
				offset.set(0, 0, 0).move(adjacency.edges[0]);
				
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[2]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[2]).move(adjacency.edges[0]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
			}
			if (i == 2) {
				offset.set(0, 0, 0).move(adjacency.edges[1]);
				
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[2]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[2]).move(adjacency.edges[1]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
			}
			if (i == 3) {
				offset.set(0, 0, 0).move(adjacency.edges[1]);
				
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[3]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
				
				offset.set(0, 0, 0).move(adjacency.edges[3]).move(adjacency.edges[1]);
				posMut.set(blockpos).move(offset);
				state = pLevel.getBlockState(posMut);
				lb = lightObstruction(state, pLevel, posMut);
				if (lb == 15) {colors[i] = none; dimmed[i] = true;}
			}
			
			if (colors[i] == null || colors[i].equals(none)) {
				colors[i] = new Color(fallback.r() / 2, fallback.g() / 2, fallback.b() / 2);
			}
		}
	}
	
	protected int lightObstruction(BlockState state, BlockAndTintGetter pLevel, BlockPos blockPos) {
		if (state.getBlock().equals(Blocks.WATER))
			return 0; // TODO: I'd like to make this a bit less hardcoded if possible
		int lb = state.getLightBlock(pLevel, blockPos);
		return lb;
	}
	
	protected Color getLightColor(LightManager manager, BlockState blockstate, BlockAndTintGetter pLevel, BlockPos blockpos$mutableblockpos) {
		// TODO: do this more correctly? cache?
		return manager.getColor(blockpos$mutableblockpos);
	}
	
	protected Color blend(Color color0, Color color1, Color color2, Color color3, float f0, float f1, float f2, float f3) {
		// TODO: WHAT
		return blend(color0, color1, color2, color3);
	}
	
	protected Color blend(Color... colors) {
		float rOut = 1;
		float gOut = 1;
		float bOut = 1;
		
		for (Color color : colors) {
			rOut = Math.min(rOut, color.r());
			gOut = Math.min(gOut, color.g());
			bOut = Math.min(bOut, color.b());
		}
		
		return new Color(rOut, gOut, bOut);
	}
	
	protected Color maxBlend(Color min, Color... colors) {
		float rOut = min.r();
		float gOut = min.g();
		float bOut = min.b();
		
		for (Color color : colors) {
			rOut = Math.min(rOut, color.r());
			gOut = Math.min(gOut, color.g());
			bOut = Math.min(bOut, color.b());
		}

//		return new Color(Math.max(rOut, min.r()), Math.max(gOut, min.g()), Math.max(bOut, min.b()));
		return new Color(rOut, gOut, bOut);
	}
	
	public Color[] getColors() {
		return colors;
	}
}
