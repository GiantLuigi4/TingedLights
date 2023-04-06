package tfc.tingedlights.api.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

public abstract class LightProvider {
	/**
	 * When a block is placed, this method gets called to determine if a light should be placed
	 * It also determines the color of said light
	 * <p>
	 * This is also used for removing lights
	 *
	 * @param pState the block state being placed or removed
	 * @param pLevel the world
	 * @param pPos   the position of the block
	 * @return the light to add, or null if it should not add a light
	 */
	public abstract Light createLight(BlockState pState, BlockGetter pLevel, BlockPos pPos);
	
	/**
	 * When a block that provides a light is placed, this gets called to determine bright the light should be
	 * <p>
	 * This is also used for removing lights
	 *
	 * @param pState the block state being placed or removed
	 * @param pLevel the world
	 * @param pPos   the position of the block
	 * @return the brightness of the light
	 */
	public abstract int getBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos);
	
	/**
	 * The result of this method should not assume that an BlockEntity exists, nor that the block state in the world is the state being received by the method
	 * If you do not have the information needed to figure out what this should return, assume it should return true
	 * <p>
	 * This is used to determine if the block provides a light, mostly in {@link LightProvider#needsUpdate(BlockState, BlockState, Level, BlockPos)}
	 * This is used for checking if a lighting update may be needed
	 * <p>
	 * While this method could call createLight and check if that returns null, that's a slow and potentially problematic approach, so no default implementation is provided
	 *
	 * @param pState the block being replaced or added
	 * @param pLevel the world
	 * @param pPos   the position of the block in the world
	 *               this may be a mutable block pos, but it will generally be immutable
	 *               in the case that it is mutable, you should make sure that it does not change
	 * @return if the block provides (or may have been providing) light
	 */
	public abstract boolean providesLight(BlockState pState, BlockGetter pLevel, BlockPos pPos);
	
	/**
	 * This is used to determine if a lighting update is necessary
	 * If the old blockstate provides light and is not the same as the new block state, then a lighting update should occur
	 * <p>
	 * If you need another condition, you can add it to this method
	 *
	 * @param pState the new block state
	 * @param pOld   the old block state
	 * @param pLevel the world
	 * @param pPos   the position of the block
	 * @return whether or not a lighting update should occur
	 */
	public boolean needsUpdate(BlockState pState, BlockState pOld, BlockGetter pLevel, BlockPos pPos) {
		if (pOld instanceof TingedLightsBlockAttachments yep)
			yep.providesLight(pOld, pLevel, pPos);
		if (!pState.equals(pOld)) {
			// TODO: not sure if this check is worth while
			if (pOld.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				if (attachments.providesLight(pOld, pLevel, pPos)) {
					return true;
				}
			}
			int oldLb = pOld.getLightBlock(pLevel, pPos);
			int newLb = pState.getLightBlock(pLevel, pPos);
			return oldLb != newLb;
		}
		return false;
	}
}
