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
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;
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
		
		int lightBlock;
		BlockState state;
		
		for (int i = 0; i < COUNT; i++) {
			boolean fullDimmed = false;
			
			
			/* edge 1 */
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]);
			state = pLevel.getBlockState(posMut);
			
			// if the block obstructs light and is not a light source,
			// then don't bother getting the light color
			// it will be BLACK anyway
			Color d0 = Color.BLACK;
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) dimmed[i] = true;
			else
				// smooth light
				d0 = getLightColor(manager, state, pLevel, posMut);
			
			
			/* edge 2 */
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][1]]);
			state = pLevel.getBlockState(posMut);
			
			Color d1 = Color.BLACK;
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) {
				if (dimmed[i]) fullDimmed = true;
				dimmed[i] = true;
			} else
				// smooth light
				d1 = getLightColor(manager, state, pLevel, posMut);
			
			
			/* corner */
			Color d2 = Color.BLACK;
			
			// if both sides are dimmed, then that means it shouldn't check the corner
			// this prevents light bleeding
			if (!fullDimmed) {
				posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]])
						.move(adjacency.edges[MAPPINGS[i][1]]);
				state = pLevel.getBlockState(posMut);
				
				// ao
				lightBlock = lightObstruction(state, pLevel, posMut);
				if (lightBlock == 15) dimmed[i] = true;
				else
					// smooth light
					d2 = getLightColor(manager, state, pLevel, posMut);
			}
			
			colors[i] = maxBlend(fallback, d0, d1, d2);
			
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
		if (state.getBlock() instanceof TingedLightsBlockAttachments attachments) {
			int b = attachments.getBrightness(state, pLevel, blockPos);
			if (b != 0)
				return 0;
		}
		
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
		
		if (Config.TesselationOptions.aoMode <= 1) {
			boolean alt = Config.TesselationOptions.aoMode == 1;
			int total = alt ? 1 : (colors.length + 1);
			
			for (Color color : colors) {
				if (alt && color.equals(Color.BLACK))
					continue;
				
				rOut += color.r();
				gOut += color.g();
				bOut += color.b();
				
				if (alt)
					total += 1;
			}
			
			rOut /= total;
			gOut /= total;
			bOut /= total;
		} else {
			for (Color color : colors) {
				rOut = Math.max(rOut, color.r());
				gOut = Math.max(gOut, color.g());
				bOut = Math.max(bOut, color.b());
			}
		}
		
		return new Color(rOut, gOut, bOut);
	}
	
	public Color[] getColors() {
		return colors;
	}
}
