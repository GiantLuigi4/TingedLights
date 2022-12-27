package tfc.tingedlights;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;

import java.util.BitSet;

public class AOFace {
	BakedQuad bakedQuad;
	float[] pShape;
	
	public AOFace(BakedQuad bakedQuad, float[] pShape) {
		this.bakedQuad = bakedQuad;
		this.pShape = pShape;
	}
	
	Color[] colors = null;
	
	public void calculate(Direction pDirection, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, BitSet pShapeFlags) {
//		BlockPos lightProbePos = pShapeFlags.get(0) ? pPos.relative(bakedQuad.getDirection()) : pPos;
		LightManager manager = ((ILightEngine) pLevel.getLightEngine()).getManager();
		colors = new Color[4];
		
//		BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
		BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
		
		ModelBlockRenderer.AdjacencyInfo modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(pDirection);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		blockpos$mutableblockpos.set(blockpos);
		// fallback srcColor
		Color self = getLightColor(manager, pLevel.getBlockState(blockpos), pLevel, blockpos$mutableblockpos);
		
//		ModelBlockRenderer.Cache modelblockrenderer$cache = ModelBlockRenderer.CACHE.get();
		
		BlockState state;
		
		// check neighbor blocks
		// set a srcColor based off that
		blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]);
		state = pLevel.getBlockState(blockpos$mutableblockpos);
		Color corner0Light = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
		
		blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]);
		state = pLevel.getBlockState(blockpos$mutableblockpos);
		Color corner1Light = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
		
		blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]);
		state = pLevel.getBlockState(blockpos$mutableblockpos);
		Color corner2Light = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
		
		blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]);
		state = pLevel.getBlockState(blockpos$mutableblockpos);
		Color corner3Light = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
		
		int lb;
		
		// check light blocking blocks
		// update corner colors based off that
		state = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]));
		lb = state.getLightBlock(pLevel, blockpos$mutableblockpos);
		if (lb != 0 /* TODO: find a good threshold */) corner0Light = self; // set fallback
		boolean flag = !state.isViewBlocking(pLevel, blockpos$mutableblockpos) || lb == 0;
		
		state = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]));
		lb = state.getLightBlock(pLevel, blockpos$mutableblockpos);
		if (lb != 0) corner1Light = self; // set fallback
		boolean flag1 = !state.isViewBlocking(pLevel, blockpos$mutableblockpos) || lb == 0;
		
		state = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]));
		lb = state.getLightBlock(pLevel, blockpos$mutableblockpos);
		if (lb != 0) corner2Light = self; // set fallback
		boolean flag2 = !state.isViewBlocking(pLevel, blockpos$mutableblockpos) || lb == 0;
		
		state = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]));
		lb = state.getLightBlock(pLevel, blockpos$mutableblockpos);
		if (lb != 0) corner3Light = self; // set fallback
		boolean flag3 = !state.isViewBlocking(pLevel, blockpos$mutableblockpos) || lb == 0;
		
		Color trueColor0;
		if (!flag2 && !flag) {
			trueColor0 = corner0Light;
		} else {
			blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[2]);
			state = pLevel.getBlockState(blockpos$mutableblockpos);
			if (state.getLightBlock(pLevel, blockpos$mutableblockpos) == 0) {
				trueColor0 = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
			} else {
				trueColor0 = self;
			}
		}
		
		Color trueCorner1;
		if (!flag3 && !flag) {
			trueCorner1 = corner1Light;
		} else {
			blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[3]);
			state = pLevel.getBlockState(blockpos$mutableblockpos);
			if (state.getLightBlock(pLevel, blockpos$mutableblockpos) == 0) {
				trueCorner1 = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
			} else {
				trueCorner1 = self;
			}
		}
		
		Color trueCorner2;
		if (!flag2 && !flag1) {
			trueCorner2 = corner2Light;
		} else {
			blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[2]);
			state = pLevel.getBlockState(blockpos$mutableblockpos);
			if (state.getLightBlock(pLevel, blockpos$mutableblockpos) == 0) {
				trueCorner2 = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
			} else {
				trueCorner2 = self;
			}
		}
		
		Color trueCorner3;
		if (!flag3 && !flag1) {
			trueCorner3 = corner3Light;
		} else {
			blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[3]);
			state = pLevel.getBlockState(blockpos$mutableblockpos);
			if (state.getLightBlock(pLevel, blockpos$mutableblockpos) == 0) {
				trueCorner3 = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
			} else {
				trueCorner3 = self;
			}
		}
		
		// not really sure what this is
		Color something = getLightColor(manager, pState, pLevel, pPos);
		blockpos$mutableblockpos.setWithOffset(pPos, pDirection);
		state = pLevel.getBlockState(blockpos$mutableblockpos);
		if (pShapeFlags.get(0) || !state.isSolidRender(pLevel, blockpos$mutableblockpos)) {
			something = getLightColor(manager, state, pLevel, blockpos$mutableblockpos);
		}
		
		ModelBlockRenderer.AmbientVertexRemap modelblockrenderer$ambientvertexremap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(pDirection);
		if (pShapeFlags.get(1) && modelblockrenderer$adjacencyinfo.doNonCubicWeight) {
			// I think this is forge's doing
			// TODO: need to figure out how to test this
			float f13 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[1].shape];
			float f14 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[3].shape];
			float f15 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[5].shape];
			float f16 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[7].shape];
			float f17 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[1].shape];
			float f18 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[3].shape];
			float f19 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[5].shape];
			float f20 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[7].shape];
			float f21 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[1].shape];
			float f22 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[3].shape];
			float f23 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[5].shape];
			float f24 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[7].shape];
			float f25 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[0].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[1].shape];
			float f26 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[2].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[3].shape];
			float f27 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[4].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[5].shape];
			float f28 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[6].shape] * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[7].shape];
			Color i2 = this.maxBlend(self, corner3Light, corner0Light, trueCorner1, something);
			Color j2 = this.maxBlend(self, corner2Light, corner0Light, trueColor0, something);
			Color k2 = this.maxBlend(self, corner2Light, corner1Light, trueCorner2, something);
			Color l2 = this.maxBlend(self, corner3Light, corner1Light, trueCorner3, something);
			this.colors[modelblockrenderer$ambientvertexremap.vert0] = this.blend(i2, j2, k2, l2, f13, f14, f15, f16);
			this.colors[modelblockrenderer$ambientvertexremap.vert1] = this.blend(i2, j2, k2, l2, f17, f18, f19, f20);
			this.colors[modelblockrenderer$ambientvertexremap.vert2] = this.blend(i2, j2, k2, l2, f21, f22, f23, f24);
			this.colors[modelblockrenderer$ambientvertexremap.vert3] = this.blend(i2, j2, k2, l2, f25, f26, f27, f28);
		} else {
			// this I understand pretty well
			this.colors[modelblockrenderer$ambientvertexremap.vert0] = this.maxBlend(self, corner3Light, corner0Light, trueCorner1, something);
			this.colors[modelblockrenderer$ambientvertexremap.vert1] = this.maxBlend(self, corner2Light, corner0Light, trueColor0, something);
			this.colors[modelblockrenderer$ambientvertexremap.vert2] = this.maxBlend(self, corner2Light, corner1Light, trueCorner2, something);
			this.colors[modelblockrenderer$ambientvertexremap.vert3] = this.maxBlend(self, corner3Light, corner1Light, trueCorner3, something);
		}
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
