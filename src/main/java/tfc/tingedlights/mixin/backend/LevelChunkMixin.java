package tfc.tingedlights.mixin.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.function.Consumer;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
	@Inject(at = @At("TAIL"), method = "replaceWithPacketData")
	public void postReplace(FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_187974_, CallbackInfo ci) {
		LevelChunk lvlChunk = (LevelChunk) (Object) this;
		for (LevelChunkSection section : lvlChunk.getSections()) {
			BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						BlockState state = section.getBlockState(x, y, z);
						blockPos.set(
								x + lvlChunk.getPos().getMinBlockX(),
								y + section.bottomBlockY(), // TODO: check?
								z + lvlChunk.getPos().getMinBlockZ()
						);
						
						LightManager manager = ((ILightEngine) lvlChunk.getLevel().getLightEngine()).getManager();
						if (state.getBlock() instanceof TingedLightsBlockAttachments attachments) {
							if (attachments.providesLight(state, lvlChunk.getLevel(), blockPos)) {
								// makes sure the mutable pos is still in the right spot
								blockPos.set(
										x + lvlChunk.getPos().getMinBlockX(),
										y + section.bottomBlockY(), // TODO: check?
										z + lvlChunk.getPos().getMinBlockZ()
								);
								
								Light light = attachments.createLight(state, lvlChunk.getLevel(), blockPos.immutable());
								if (light != null) {
									manager.addLight(light);
								}
							}
						}
					}
				}
			}
		}
	}
}
