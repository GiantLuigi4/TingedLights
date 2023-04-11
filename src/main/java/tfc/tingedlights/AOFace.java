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
	
	public void calculate(Direction pDirection, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, BitSet pShapeFlags) {
		BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
		LightManager manager = (LightManager) pLevel.getLightEngine();
		
		ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection);
		BetterAdjacencyInfo adjacency = new BetterAdjacencyInfo(adjacencyInfo, pDirection);
		
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
		
		final int[][] MAPPINGS = new int[][]{
				new int[]{0, 3},
				new int[]{0, 2},
				new int[]{1, 2},
				new int[]{1, 3}
		};
		
		colors = new Color[4];
		dimmed = new boolean[4];
		for (int i = 0; i < vertices.length; i++) {
			int lb;
			
			int el = 0;
			if (i == 0) el = 0;
			if (i == 1) el = 1;
			if (i == 2) el = 2;
			if (i == 3) el = 3;
			
			colors[i] = fallback;
			
			BlockState state;
			
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]);
			Color d0 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
			if (state.isViewBlocking(pLevel, posMut)) d0 = fallback;
			
			// ao
			lb = lightObstruction(state, pLevel, posMut);
			if (lb == 15) dimmed[i] = true;
			
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][1]]);
			Color d1 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
			if (state.isViewBlocking(pLevel, posMut)) d1 = fallback;
			
			// ao
			lb = lightObstruction(state, pLevel, posMut);
			if (lb == 15) dimmed[i] = true;
			
			// smooth light
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]).move(adjacency.edges[MAPPINGS[i][1]]);
			Color d2 = getLightColor(manager, state = pLevel.getBlockState(posMut), pLevel, posMut);
			if (state.isViewBlocking(pLevel, posMut)) d2 = fallback;
			
			// ao
			lb = lightObstruction(state, pLevel, posMut);
			if (lb == 15) dimmed[i] = true;
			
			colors[i] = new Color(
					Math.max(fallback.r(), Math.max(d0.r(), Math.max(d1.r(), d2.r()))),
					Math.max(fallback.g(), Math.max(d0.g(), Math.max(d1.g(), d2.g()))),
					Math.max(fallback.b(), Math.max(d0.b(), Math.max(d1.b(), d2.b())))
			);
			
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
