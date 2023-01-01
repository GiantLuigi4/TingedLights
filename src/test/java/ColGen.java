import tfc.tingedlights.data.Color;

public class ColGen {
	public static void main(String[] args) {
		String template = """
				{
				  "color": [%col%],
				  "brightness": -1,
				  "state": {
				    "charges": %v%
				  }
				}""";
		System.out.println("\"minecraft:respawn_anchor\": [");
		Color startColor = new Color(96, 12, 169);
		Color endColor = new Color(250, 217, 118);
		for (int i = 1; i < 5; i++) {
			float d = (i - 1) / 4f;
			Color interp = new Color(
					(startColor.r() * (1 - d)) + (endColor.r() * d),
					(startColor.g() * (1 - d)) + (endColor.g() * d),
					(startColor.b() * (1 - d)) + (endColor.b() * d)
			);
			System.out.println(template.replace(
					"%col%",
					interp.r() + ", " + interp.g() + ", " + interp.b()
			).replace("%v%", "" + i) + ",");
		}
		System.out.println("]");
	}
}
