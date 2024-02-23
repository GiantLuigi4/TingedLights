package tfc.tingedlights.itf;

import tfc.tingedlights.data.Color;

public interface VertexBufferConsumerExtensions {
	void setDefault(Color color);
	Color getDefaultColor();
}
