package tfc.tingedlights.data.access.flw;

public interface VertexListExtension {
	byte getLightR(int vertIndex);
	byte getLightG(int vertIndex);
	byte getLightB(int vertIndex);
	void markIndex(int indx);
}
