package tfc.tingedlights;

// TODO: config
public class Options {
	public static boolean dumpShaders = true;
	
	public static boolean dynamicLights = true;
	// vertex sort: used to avoid jagged edges and odd AO
	public static boolean sortVertices = true;
	
	/* not an option */
	public static boolean wroteLightShader = false;
}
