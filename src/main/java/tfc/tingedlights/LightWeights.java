package tfc.tingedlights;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LightWeights {
	public static float[] get(Vec3 vertex, Direction pDirection) {
		int x = pDirection.getStepX();
		int y = pDirection.getStepY();
		int z = pDirection.getStepZ();
		
		Vec2 pos2d = new Vec2(0, 0);
		if (z != 0) {
			if (z > 0) pos2d = new Vec2(1 - (float) vertex.x, (float) vertex.y);
			else pos2d = new Vec2((float) vertex.x, (float) vertex.y);
		} else if (x != 0) {
			if (x > 0) pos2d = new Vec2((float) vertex.z, (float) vertex.y);
			else pos2d = new Vec2(1 - (float) vertex.z, (float) vertex.y);
		} else if (y != 0) {
			if (y > 0) pos2d = new Vec2(1 - (float) vertex.x, 1 - (float) vertex.z);
			else pos2d = new Vec2(1 - (float) vertex.x, (float) vertex.z);
		}
		
		float bl = pos2d.x;
		float tl = pos2d.x;
		float br = 1 - pos2d.x;
		float tr = 1 - pos2d.x;
		bl *= 1 - pos2d.y;
		br *= 1 - pos2d.y;
		tl *= pos2d.y;
		tr *= pos2d.y;
		
		return new float[]{
				tl,
				bl,
				br,
				tr,
		};
	}
}
