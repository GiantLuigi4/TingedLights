package tfc.tingedlights.resource;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;

public class StateIdentifier {
	Block block;
	Map<String, String> stateMap;
	Map<String, Property<?>> propertyHashMap = new HashMap<>();
	
	public StateIdentifier(Block block, Map<String, String> stateMap) {
		this.block = block;
		this.stateMap = stateMap;
		for (Property<?> property : block.getStateDefinition().getProperties()) {
			propertyHashMap.put(property.getName(), property);
		}
	}
	
	public int lenienceScore() {
		return stateMap.size();
	}
	
	public boolean validate(BlockState state) {
		if (state.getBlock() != block) return false;
		if (propertyHashMap.isEmpty()) return true;
		for (String s : stateMap.keySet()) {
			Property<?> property = propertyHashMap.get(s);
			Object o = state.getValue(property);
			if (!o.toString().equals(stateMap.get(s))) {
				return false;
			}
		}
		return true;
	}
}
