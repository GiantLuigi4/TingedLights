package tfc.tingedlights.itf;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tfc.tingedlights.data.Color;

public interface VertexBufferConsumerExtensions {
	void setColorDone(boolean val);
	boolean isColorDone();
	void setDefault(Color color);
	Color getDefaultColor();
}
