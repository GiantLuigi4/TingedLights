package tfc.tingedlights.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.VertexElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(VertexFormat.class)
public class VertexFormatMixin {
	// @formatter:off
	@Shadow @Final @Mutable private ImmutableMap<String, VertexFormatElement> elementMapping;
	@Shadow @Final @Mutable private ImmutableList<VertexFormatElement> elements;
	@Shadow @Final @Mutable private IntList offsets;
	@Shadow @Final @Mutable  private int vertexSize;
	// @formatter:on
	
	@Inject(at = @At("TAIL"), method = "<init>")
	private void postInit(CallbackInfo ci) {
		boolean doStuff = false;
		for (VertexFormatElement element : this.elements) {
			if (element.equals(DefaultVertexFormat.ELEMENT_UV2)) {
				doStuff = true;
				break;
			}
		}
		if (!doStuff) return;
		
		Map<String, VertexFormatElement> elementMap = new HashMap<>(this.elementMapping);
		elementMap.put("LightColor", VertexElements.ELEMENT_LIGHT_COLOR);
		this.elementMapping = ImmutableMap.copyOf(elementMap);
		
		List<VertexFormatElement> elements = new ArrayList<>(this.elements);
		elements.add(VertexElements.ELEMENT_LIGHT_COLOR);
		this.elements = ImmutableList.copyOf(elements);
		
		this.offsets.add(this.vertexSize);
		this.vertexSize = this.vertexSize + VertexElements.ELEMENT_LIGHT_COLOR.getByteSize();
	}
}
