package tfc.tingedlights.resource;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.data.LightProvider;

import java.util.ArrayList;
import java.util.List;

public class PerStateLightProvider extends LightProvider {
	List<Pair<StateIdentifier, LightProvider>> internal = new ArrayList<>();
	
	public void addProvider(StateIdentifier identifier, LightProvider provider) {
		internal.add(Pair.of(identifier, provider));
	}
	
	public void sort() {
		internal.sort((first, second) -> Integer.compare(second.getFirst().lenienceScore(), first.getFirst().lenienceScore()));
	}
	
	@Override
	public Light createLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		for (Pair<StateIdentifier, LightProvider> stateIdentifierLightProviderPair : internal) {
			if (stateIdentifierLightProviderPair.getFirst().validate(pState)) {
				return stateIdentifierLightProviderPair.getSecond().createLight(pState, pLevel, pPos);
			}
		}
		return null;
	}
	
	@Override
	public int getBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		for (Pair<StateIdentifier, LightProvider> stateIdentifierLightProviderPair : internal) {
			if (stateIdentifierLightProviderPair.getFirst().validate(pState)) {
				return stateIdentifierLightProviderPair.getSecond().getBrightness(pState, pLevel, pPos);
			}
		}
		return 0;
	}
	
	@Override
	public boolean providesLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		for (Pair<StateIdentifier, LightProvider> stateIdentifierLightProviderPair : internal) {
			if (stateIdentifierLightProviderPair.getFirst().validate(pState)) {
				return stateIdentifierLightProviderPair.getSecond().providesLight(pState, pLevel, pPos);
			}
		}
		return false;
	}
	
	@Override
	public boolean needsUpdate(BlockState pState, BlockState pOld, BlockGetter pLevel, BlockPos pPos) {
		LightProvider provider = getFor(pState);
		if (provider == null) return super.needsUpdate(pState, pOld, pLevel, pPos);
		if (provider.needsUpdate(pState, pOld, pLevel, pPos)) return true;
		if (pState.getBlock() == pOld.getBlock()) {
			LightProvider oldProvider = getFor(pOld);
			return provider != oldProvider;
		}
		return false;
	}
	
	public LightProvider getFor(BlockState state) {
		for (Pair<StateIdentifier, LightProvider> stateIdentifierLightProviderPair : internal) {
			if (stateIdentifierLightProviderPair.getFirst().validate(state)) {
				return stateIdentifierLightProviderPair.getSecond();
			}
		}
		return null;
	}
	
	public LightProvider maybeBake() {
		if (internal.size() == 1) {
			if (internal.get(0).getFirst().lenienceScore() == 0) {
				return internal.get(0).getSecond();
			}
		}
		this.internal = ImmutableList.copyOf(internal);
		return this;
	}
}
