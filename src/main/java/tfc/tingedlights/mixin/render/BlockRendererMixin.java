package tfc.tingedlights.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tfc.tingedlights.AOFace;
import tfc.tingedlights.BlockTesselator;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

// TODO: move off overwrites?
@Mixin(ModelBlockRenderer.class)
public abstract class BlockRendererMixin {
	@Shadow
	@Final
	private static Direction[] DIRECTIONS;
	
	@Shadow
	protected abstract void calculateShape(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int[] pVertices, Direction pDirection, @Nullable float[] pShape, BitSet pShapeFlags);
	
	@Shadow
	@Final
	private BlockColors blockColors;
	
	@Unique
	private final ThreadLocal<BlockPos> posThreadLocal = new ThreadLocal<>();
	
	/**
	 * @author
	 */
	@Overwrite
	private void putQuadData(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay) {
		BlockTesselator.putQuadData(blockColors, pLevel, pState, pPos, pConsumer, pPose, pQuad, pBrightness0, pBrightness1, pBrightness2, pBrightness3, pLightmap0, pLightmap1, pLightmap2, pLightmap3, pPackedOverlay, posThreadLocal, false /* TODO: check mc options */, null);
	}
	
	/**
	 * @author
	 */
	@Overwrite
	private void renderModelFaceFlat(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int pPackedLight, int pPackedOverlay, boolean pRepackLight, PoseStack pPoseStack, VertexConsumer pConsumer, List<BakedQuad> pQuads, BitSet pShapeFlags) {
		for (BakedQuad bakedquad : pQuads) {
			if (pRepackLight) {
				this.calculateShape(pLevel, pState, pPos, bakedquad.getVertices(), bakedquad.getDirection(), (float[]) null, pShapeFlags);
				BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(bakedquad.getDirection().getOpposite()) : pPos;
				posThreadLocal.set(blockpos);
				pPackedLight = LevelRenderer.getLightColor(pLevel, pState, blockpos);
			}
			
			float f = pLevel.getShade(bakedquad.getDirection(), bakedquad.isShade());
			BlockTesselator.putQuadData(blockColors, pLevel, pState, pPos, pConsumer, pPoseStack.last(), bakedquad, f, f, f, f, pPackedLight, pPackedLight, pPackedLight, pPackedLight, pPackedOverlay, posThreadLocal, false, null);
			posThreadLocal.remove();
		}
	}
	
	/**
	 * @author
	 */
	@Overwrite
	private void renderModelFaceAO(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, List<BakedQuad> pQuads, float[] pShape, BitSet pShapeFlags, ModelBlockRenderer.AmbientOcclusionFace pAoFace, int pPackedOverlay) {
		for (BakedQuad bakedquad : pQuads) {
			this.calculateShape(pLevel, pState, pPos, bakedquad.getVertices(), bakedquad.getDirection(), pShape, pShapeFlags);
			pAoFace.calculate(pLevel, pState, pPos, bakedquad.getDirection(), pShape, pShapeFlags, bakedquad.isShade());
			AOFace face = new AOFace(bakedquad, pShape);
			face.calculate(bakedquad.getDirection(), pLevel, pState, pPos, pShapeFlags);
			BlockTesselator.putQuadData(blockColors, pLevel, pState, pPos, pConsumer, pPoseStack.last(), bakedquad, pAoFace.brightness[0], pAoFace.brightness[1], pAoFace.brightness[2], pAoFace.brightness[3], pAoFace.lightmap[0], pAoFace.lightmap[1], pAoFace.lightmap[2], pAoFace.lightmap[3], pPackedOverlay, posThreadLocal, true, face);
		}
	}
	
	/**
	 * @author
	 */
	@Overwrite(remap = false)
	public boolean tesselateWithoutAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, Random pRandom, long pSeed, int pPackedOverlay, net.minecraftforge.client.model.data.IModelData modelData) {
		boolean flag = false;
		BitSet bitset = new BitSet(3);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
		
		posThreadLocal.set(pPos);
		for (Direction direction : DIRECTIONS) {
			pRandom.setSeed(pSeed);
			List<BakedQuad> list = pModel.getQuads(pState, direction, pRandom, modelData);
			if (!list.isEmpty()) {
				blockpos$mutableblockpos.setWithOffset(pPos, direction);
				if (!pCheckSides || Block.shouldRenderFace(pState, pLevel, pPos, direction, blockpos$mutableblockpos)) {
					posThreadLocal.set(blockpos$mutableblockpos);
					int i = LevelRenderer.getLightColor(pLevel, pState, blockpos$mutableblockpos);
					this.renderModelFaceFlat(pLevel, pState, pPos, i, pPackedOverlay, false, pPoseStack, pConsumer, list, bitset);
					flag = true;
				}
			}
		}
		
		pRandom.setSeed(pSeed);
		List<BakedQuad> list1 = pModel.getQuads(pState, (Direction) null, pRandom, modelData);
		if (!list1.isEmpty()) {
			posThreadLocal.set(pPos);
			this.renderModelFaceFlat(pLevel, pState, pPos, -1, pPackedOverlay, true, pPoseStack, pConsumer, list1, bitset);
			flag = true;
		}
		
		posThreadLocal.remove();
		
		return flag;
	}
}
