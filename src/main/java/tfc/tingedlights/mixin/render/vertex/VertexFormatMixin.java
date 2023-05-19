package tfc.tingedlights.mixin.render.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.data.access.VertexFormatAccess;
import tfc.tingedlights.vertex.VertexElements;

import java.util.ArrayList;
import java.util.List;

@Mixin(VertexFormat.class)
public abstract class VertexFormatMixin implements VertexFormatAccess {
	// @formatter:off
	@Shadow @Final @Mutable private ImmutableMap<String, VertexFormatElement> elementMapping;
	@Shadow @Final @Mutable private ImmutableList<VertexFormatElement> elements;
	@Shadow @Final @Mutable private IntList offsets;
	@Shadow @Final @Mutable  private int vertexSize;
	@Shadow public abstract int getVertexSize();@Unique int vanillaSize = 0;
	// @formatter:on
	
	@Inject(at = @At("TAIL"), method = "<init>")
	private void postInit(CallbackInfo ci) {
		vanillaSize = vertexSize;
		
		boolean doStuff = false;
		for (VertexFormatElement element : this.elements) {
			if (element.equals(DefaultVertexFormat.ELEMENT_UV2)) {
				doStuff = true;
				break;
			}
		}
		if (!doStuff) return;
		
		boolean insertBeforePadding = false;
		
		ImmutableMap.Builder<String, VertexFormatElement> builder = new ImmutableMap.Builder<>();
		int index = 0;
		int lastPadding = elements.lastIndexOf(DefaultVertexFormat.ELEMENT_PADDING);
		if (!insertBeforePadding) lastPadding = -1;
//		int lastPadding = -1;
		for (String element : elementMapping.keySet()) {
			if (index == lastPadding) {
				builder.put("LightColor", VertexElements.ELEMENT_LIGHT_COLOR);
			}
			builder.put(element, elementMapping.get(element));
			index++;
		}
		if (lastPadding == -1)
			builder.put("LightColor", VertexElements.ELEMENT_LIGHT_COLOR);
		this.elementMapping = builder.build();
		
		List<VertexFormatElement> elements = new ArrayList<>(this.elements);
		if (lastPadding != -1) elements.add(lastPadding, VertexElements.ELEMENT_LIGHT_COLOR);
		else elements.add(VertexElements.ELEMENT_LIGHT_COLOR);
		this.elements = ImmutableList.copyOf(elements);
		
		this.offsets.add(this.vertexSize);
		this.vertexSize = this.vertexSize + VertexElements.ELEMENT_LIGHT_COLOR.getByteSize();
	}
	
	@Override
	public int getVanillaSize() {
		if (vanillaSize == 0) return getVertexSize();
		return vanillaSize;
	}
}
