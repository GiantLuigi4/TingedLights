package tfc.tingedlights;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;
import tfc.tingedlights.util.BetterAdjacencyInfo;
import tfc.tingedlights.utils.config.Config;

import java.util.Arrays;
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
	float[] shades = null;
	int[] skylight = null;
	
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
		BetterAdjacencyInfo adjacency = BetterAdjacencyInfo.get(adjacencyInfo);
		
		Color fallback = getLightColor(manager, pLevel.getBlockState(blockpos), pLevel, blockpos);
		int self = pLevel.getBrightness(LightLayer.SKY, blockpos);
		
		BlockPos.MutableBlockPos posMut = new BlockPos.MutableBlockPos();
		
		colors = new Color[COUNT];
		dimmed = new boolean[COUNT];
		
		skylight = new int[COUNT];
		
		if (pShapeFlags.get(1)) {
			Vec3[] vertices = new Vec3[COUNT];
			for (int i = 0; i < COUNT; i++) {
				int index = i * 8;
				vertices[i] = new Vec3(
						Float.intBitsToFloat(bakedQuad.getVertices()[index]),
						Float.intBitsToFloat(bakedQuad.getVertices()[index + 1]),
						Float.intBitsToFloat(bakedQuad.getVertices()[index + 2])
				);
			}
			
			Arrays.fill(colors, fallback);
			calcAOPartial(pDirection, pLevel, pState, pPos, pShapeFlags, blockpos, manager, adjacencyInfo, adjacency, vertices, fallback);
			return;
		}
		
		int lightBlock;
		BlockState state;
		
		if (Config.TesselationOptions.AOOptions.removeVanillaAO && Config.TesselationOptions.AOOptions.aoIntensity != 0) {
			shades = new float[COUNT];
			Arrays.fill(shades, 1);
		}
		
		for (int i = 0; i < COUNT; i++) {
			boolean hasSoft = false;
			boolean fullDimmed = false;
			
			
			/* edge 1 */
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]]);
			state = pLevel.getBlockState(posMut);
			
			// if the block obstructs light and is not a light source,
			// then don't bother getting the light color
			// it will be BLACK anyway
			Color d0 = Color.BLACK;
			int s0 = 0;
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) dimmed[i] = true;
			else {
				if (lightBlock == 16) hasSoft = true;
				// smooth light
				d0 = getLightColor(manager, state, pLevel, posMut);
				s0 = pLevel.getBrightness(LightLayer.SKY, posMut);
			}
			
			
			/* edge 2 */
			posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][1]]);
			state = pLevel.getBlockState(posMut);
			
			Color d1 = Color.BLACK;
			int s1 = 0;
			// ao
			lightBlock = lightObstruction(state, pLevel, posMut);
			if (lightBlock == 15) {
				if (dimmed[i]) fullDimmed = true;
				dimmed[i] = true;
			} else {
				if (lightBlock == 16) hasSoft = true;
				// smooth light
				d1 = getLightColor(manager, state, pLevel, posMut);
				s1 = pLevel.getBrightness(LightLayer.SKY, posMut);
			}
			
			
			/* corner */
			Color d2 = Color.BLACK;
			int s2 = 0;
			// if both sides are dimmed, then that means it shouldn't check the corner
			// this prevents light bleeding
			if (!fullDimmed) {
				posMut.setWithOffset(blockpos, adjacency.edges[MAPPINGS[i][0]])
						.move(adjacency.edges[MAPPINGS[i][1]]);
				state = pLevel.getBlockState(posMut);
				
				// ao
				lightBlock = lightObstruction(state, pLevel, posMut);
				if (lightBlock == 15) dimmed[i] = true;
				else {
					if (lightBlock == 16) hasSoft = true;
					// smooth light
					d2 = getLightColor(manager, state, pLevel, posMut);
					s2 = pLevel.getBrightness(LightLayer.SKY, posMut);
				}
			}
			
			// calculate final color
			colors[i] = confBlend(dimmed[i], fullDimmed, fallback, d0, d1, d2);
			skylight[i] = LightTexture.pack(0, maxBlend(self, s0, s1, s2));
			
			// AO
			if (dimmed[i] || hasSoft) {
				float intensity = (1 - Config.TesselationOptions.AOOptions.aoIntensity);
				if (Config.TesselationOptions.AOOptions.aoIntensity != 0) {
					// make corners darker than edges
					if (fullDimmed)
						intensity *= Config.TesselationOptions.AOOptions.cornerMul;
					if (hasSoft)
						intensity = (intensity + 1) * 0.5f;
					
					if (shades != null) {
						// for when vanilla AO is removed
						shades[i] = intensity;
					} else {
						// for when vanilla AO is not removed
						colors[i] = new Color(
								colors[i].r() * intensity,
								colors[i].g() * intensity,
								colors[i].b() * intensity
						);
					}
				}
			}
		}
	}
	
	protected int maxBlend(int self, int s0, int s1, int s2) {
		return Math.max(Math.max(Math.max(s0, s1), s2), self);
	}
	
	public void calcAOPartial(Direction pDirection, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, BitSet pShapeFlags, BlockPos blockpos, LightManager manager, ModelBlockRenderer.AdjacencyInfo adjacencyInfo, BetterAdjacencyInfo adjacency, Vec3[] vertices, Color fallback) {
		// TODO: figure out custom AO for non-full blocks
		BitSet flags = new BitSet();
		if (pShapeFlags.get(0)) flags.set(0);
		
		calculate(pDirection, pLevel, pState, pPos, flags);
		
		Color[] srcC = colors;
		float[] srcS = shades;
		
		colors = new Color[COUNT];
		if (srcS != null)
			shades = new float[COUNT];
		
		for (int i = 0; i < vertices.length; i++) {
			float[] weights = LightWeights.get(vertices[i], pDirection);
			for (int i1 = 0; i1 < weights.length; i1++) {
				colors[i] = blend(srcC, weights);
				if (srcS != null)
					shades[i] = blend(srcS, weights);
			}
		}
	}
	
	private Color blend(Color[] srcC, float[] weights) {
		return new Color(
				srcC[0].r() * weights[0] + srcC[1].r() * weights[1] + srcC[2].r() * weights[2] + srcC[3].r() * weights[3],
				srcC[0].g() * weights[0] + srcC[1].g() * weights[1] + srcC[2].g() * weights[2] + srcC[3].g() * weights[3],
				srcC[0].b() * weights[0] + srcC[1].b() * weights[1] + srcC[2].b() * weights[2] + srcC[3].b() * weights[3]
		);
	}
	
	private float blend(float[] srcS, float[] weights) {
		return
				srcS[0] * weights[0] +
						srcS[1] * weights[1] +
						srcS[2] * weights[2] +
						srcS[3] * weights[3];
	}
	
	protected int lightObstruction(BlockState state, BlockAndTintGetter pLevel, BlockPos blockPos) {
		if (state.getBlock().equals(Blocks.WATER))
			return 0; // TODO: I'd like to make this a bit less hardcoded if possible
		
		int lb = state.getLightBlock(pLevel, blockPos);
		
		if (lb == 15) {
			if (state.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				int b = attachments.getBrightness(state, pLevel, blockPos);
				if (b != 0)
					return Config.TesselationOptions.AOOptions.allowSoftAO ? 16 : 0;
			}
		}
		
		return lb;
	}
	
	protected Color getLightColor(LightManager manager, BlockState blockstate, BlockAndTintGetter pLevel, BlockPos blockpos$mutableblockpos) {
		// TODO: do this more correctly? cache?
		return manager.getColor(blockpos$mutableblockpos);
	}
	
	protected Color confBlend(boolean dimmed, boolean fullyDimmed, Color min, Color... colors) {
		float rOut = min.r();
		float gOut = min.g();
		float bOut = min.b();
		
		if (Config.TesselationOptions.aoMode <= 1) {
			boolean alt = Config.TesselationOptions.aoMode == 1;
			int total = alt ? 1 : (colors.length + 1);
			if (!alt) {
				if (dimmed) total -= 1;
				if (fullyDimmed) total -= 1;
			}
			
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
			rOut /= 1.1;
			gOut /= 1.1;
			bOut /= 1.1;
		}
		
		return new Color(rOut, gOut, bOut);
	}
	
	public Color[] getColors() {
		return colors;
	}
}
