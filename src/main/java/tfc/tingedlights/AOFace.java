package tfc.tingedlights;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.util.BetterAdjacencyInfo;
import tfc.tingedlights.utils.config.Config;

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
	
	private static final int[][] MAPPINGS = new int[][]{
			new int[]{0, 3},
			new int[]{0, 2},
			new int[]{1, 2},
			new int[]{1, 3}
	};
	private static final int COUNT = 4;
	
	public void calculate(Direction pDirection, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, BitSet pShapeFlags) {
		BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
		LightManager manager = (LightManager) pLevel.getLightEngine();
		
		ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection);
		BetterAdjacencyInfo adjacency = new BetterAdjacencyInfo(adjacencyInfo, pDirection);
		
		Color fallback = getLightColor(manager, pLevel.getBlockState(blockpos), pLevel, blockpos);
		
		BlockPos.MutableBlockPos posMut = new BlockPos.MutableBlockPos();
		
		colors = new Color[COUNT];
		dimmed = new boolean[COUNT];
		
//		Vec3[] vertices = new Vec3[COUNT];
//		for (int i = 0; i < COUNT; i++) {
//			int index = i * 8;
//			vertices[i] = new Vec3(
//					Float.intBitsToFloat(bakedQuad.getVertices()[index]),
//					Float.intBitsToFloat(bakedQuad.getVertices()[index + 1]),
//					Float.intBitsToFloat(bakedQuad.getVertices()[index + 2])
//			);
//		}
//		BlockState selfState = pLevel.getBlockState(pPos);
		
		int lightBlock;
		BlockState state;
		
		for (int i = 0; i < COUNT; i++) {
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]);
			Color d0 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
//			if (state.isViewBlocking(pLevel, posMut)) d0 = fallback;
			
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) dimmed[i] = true;
			
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][1]]);
			Color d1 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
//			if (state.isViewBlocking(pLevel, posMut)) d1 = fallback;
			
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) dimmed[i] = true;
			
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]).move(adjacency.edges[MAPPINGS[i][1]]);
			Color d2 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
//			if (state.isViewBlocking(pLevel, posMut)) d2 = fallback;
			
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) dimmed[i] = true;
			
			colors[i] = maxBlend(fallback, d0, d1, d2);
			
//			if (pShapeFlags.get(0) && !selfState.isSolidRender(pLevel, pPos)) {
//				dimmed[i] = false;
//
//				colors[i] = maxBlend(
//						fallback,
//						LightBlender.blend(
//								vertices[i]
//										.add(-0.5, -0.5, -0.5)
//										.scale(0.99)
//										.add(0.5, 0.5, 0.5)
//										.add(pPos.getX(), pPos.getY(), pPos.getZ()),
//								manager, pLevel
//						),
//						colors[i]
//				);
//
//				if (colors[i].getBrightness() < fallback.getBrightness())
//					dimmed[i] = true;
//			}
			
			if (dimmed[i]) {
				float intensity = (1 - Config.TesselationOptions.aoIntensity);
				if (Config.TesselationOptions.aoIntensity != 0) {
					colors[i] = new Color(
							colors[i].r() * intensity,
							colors[i].g() * intensity,
							colors[i].b() * intensity
					);
				}
			}
		}
	}
	
	protected int lightObstruction(BlockState state, BlockAndTintGetter pLevel, BlockPos blockPos) {
		if (state.getBlock().equals(Blocks.WATER))
			return 0; // TODO: I'd like to make this a bit less hardcoded if possible
		return state.getLightBlock(pLevel, blockPos);
	}
	
	protected Color getLightColor(LightManager manager, BlockState blockstate, BlockAndTintGetter pLevel, BlockPos blockpos$mutableblockpos) {
		// TODO: do this more correctly? cache?
		return manager.getColor(blockpos$mutableblockpos);
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
//			rOut = Math.max(rOut, color.r());
//			gOut = Math.max(gOut, color.g());
//			bOut = Math.max(bOut, color.b());
			rOut += color.r();
			gOut += color.g();
			bOut += color.b();
		}
		rOut /= colors.length + 1;
		gOut /= colors.length + 1;
		bOut /= colors.length + 1;
		
		return new Color(rOut, gOut, bOut);
	}
	
	public Color[] getColors() {
		return colors;
	}
}
