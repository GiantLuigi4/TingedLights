package tfc.tingedlights.mixin.render.flywheel;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.layout.LayoutItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.tingedlights.vertex.FlwVertexItems;

@Mixin(value = BufferLayout.Builder.class, remap = false)
public class FormatBuilderMixin {
	@Shadow
	@Mutable
	@Final
	private ImmutableList.Builder<LayoutItem> allItems;
	
	@Inject(at = @At("HEAD"), method = "build")
	public void preBuild(CallbackInfoReturnable<BufferLayout> cir) {
		ImmutableList<LayoutItem> list = allItems.build();
		allItems = new ImmutableList.Builder<>();
		boolean hasLight = false;
		for (LayoutItem layoutItem : list) {
			if (
					layoutItem.equals(CommonItems.LIGHT_SHORT) ||
							layoutItem.equals(CommonItems.LIGHT)
			) {
				hasLight = true;
				break;
			}
		}
		if (hasLight) {
			int lastPadding = list.lastIndexOf(CommonItems.PADDING_BYTE);
			for (int i = 0; i < list.size(); i++) {
				if (i == lastPadding)
					allItems.add(FlwVertexItems.LIGHT_COLOR);
				allItems.add(list.get(i));
			}
			if (lastPadding == -1)
				allItems.add(FlwVertexItems.LIGHT_COLOR);
		}
	}
}
