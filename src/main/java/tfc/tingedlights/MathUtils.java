package tfc.tingedlights;

public class MathUtils {
	public static float sigmoid(float input) {
		return (float) (1 / (Math.sqrt(1 + Math.pow(input / 15d, 2))));
	}
	
	public static float smoothLight(float input) {
		return (float) (1 - (1 / (Math.sqrt(1 + input))));
	}
}
