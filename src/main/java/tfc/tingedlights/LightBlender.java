package tfc.tingedlights;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.struct.LightNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LightBlender {
	/*protected static final float divisor = 9;
	protected static final float mul = 1 / MathUtils.smoothLight(15 / divisor);*/
	
	private static final Color defaultColor = new Color(0, 0, 0);
	
	public static Color blendLight(Collection<LightNode> nodes) {
		if (nodes == null)
			return defaultColor;
		return blend(flatten(nodes));
	}
	
	public static Color blend(Color[] colors) {
		float rOut = 0;
		float gOut = 0;
		float bOut = 0;
		int count = colors.length;
		for (Color color : colors) {
			// TODO: do this better?
			float lr = color.r() + rOut / (count * 15f);
			float lg = color.g() + gOut / (count * 15f);
			float lb = color.b() + bOut / (count * 15f);
			
			rOut = Math.max(rOut, lr);
			gOut = Math.max(gOut, lg);
			bOut = Math.max(bOut, lb);
		}
		if (rOut > 1) rOut = 1;
		if (gOut > 1) gOut = 1;
		if (bOut > 1) bOut = 1;
		
		return new Color(rOut, gOut, bOut);
	}
	
	public static Color[] flatten(Collection<LightNode> nodes) {
//		Collection<Color> colorsSet = new HashSet<>(nodes.size());
		int sz = nodes.size();
		Color[] colorsOut = new Color[sz];
		int index = 0;
		for (LightNode node : nodes) {
			int dist = node.brightness();
			if (dist < 0) dist = 0;
			if (dist > 15) dist = 15;
			
			final float divisor = 10;
			final float mul = 1 / MathUtils.smoothLight(15 / divisor);
			
			float d = dist / 15f;
//			float d = MathUtils.smoothLight((dist) / divisor) * mul;
//			if (dist != 0) d = Mth.lerp(maxLightmap / 40f, d, 1);
//			d /= (node.getColor().r() + node.getColor().g() + node.getColor().b()) / 2;
			
			Color blended = node.getLight().color();
			// TODO: redo this?
			if (dist < node.light().blendThreshold()) {
				Color blendTo = node.getLight().endColor();
				
				float blendDist = ((dist - node.light().blendThreshold()));
				blendDist = -blendDist;
				blendDist /= (node.light().blendThreshold());
//				blendDist *= 15;
//				blendDist = MathUtils.smoothLight((blendDist) / divisor) * mul;
				blendDist = 1 - blendDist;
				blendDist = (float) Math.pow(blendDist, 2.5);
				blendDist = 1 - blendDist;
				
				blended = new Color(
						(blended.r() * (1 - blendDist)) + blendTo.r() * blendDist,
						(blended.g() * (1 - blendDist)) + blendTo.g() * blendDist,
						(blended.b() * (1 - blendDist)) + blendTo.b() * blendDist
				);
//				blended = new Color(
//						0, 0, blendDist
//				);

//				double b1 = blended.getBrightness();
//				double v = (Math.max(b0, b1) - Math.min(b0, b1)) / 1.2;
//				d += v;
//				if (d > 1) d = 1;
			}
			
			// TODO: should I find a better way to do this..?
			if (node.light().distanceFade()) {
				colorsOut[index] = new Color(
						blended.r() * d,
						blended.g() * d,
						blended.b() * d
				);
			} else {
				colorsOut[index] = blended;
			}
			index++;
		}
		return colorsOut;
	}
	
	public static Color average(Color c0, Color c1, Color c2, Color c3, double weightX, double weightY, double weightZ) {
		double totalDiv = 1 + weightX + weightY + weightZ;
		c1 = c1.mul(weightX);
		c2 = c2.mul(weightY);
		c3 = c3.mul(weightZ);
		return new Color(
				(float) ((c0.r() + c1.r() + c2.r() + c3.r()) / totalDiv),
				(float) ((c0.g() + c1.g() + c2.g() + c3.g()) / totalDiv),
				(float) ((c0.b() + c1.b() + c2.b() + c3.b()) / totalDiv)
		);
	}
	
	public static Color blend(Vec3 pos, LightManager engine, Level level) {
		Color srcColor = engine.getColor(new BlockPos(pos));
		Color bx = srcColor;
		Color by = srcColor;
		Color bz = srcColor;
		double dx = Math.abs(pos.x - ((int) pos.x));
		if (pos.x < 0) dx = 1 - dx;
		double dy = Math.abs(pos.y - ((int) pos.y));
		if (pos.y < 0) dy = 1 - dy;
		double dz = Math.abs(pos.z - ((int) pos.z));
		if (pos.z < 0) dz = 1 - dz;
		// TODO: deal with light blocking blocks
		if (dx > 0.5) {
			bx = getLight(level, engine, new BlockPos(pos.x + 1, pos.y, pos.z), srcColor);
			dx -= 0.5;
			dx *= 2;
		} else if (dx < 0.5) {
			bx = getLight(level, engine, new BlockPos(pos.x - 1, pos.y, pos.z), srcColor);
			dx *= 2;
			dx = 1 - dx;
		} else {
			dx = 0;
		}
		if (dy > 0.5) {
			by = getLight(level, engine, new BlockPos(pos.x, pos.y + 1, pos.z), srcColor);
			dy -= 0.5;
			dy *= 2;
		} else if (dy < 0.5) {
			by = getLight(level, engine, new BlockPos(pos.x, pos.y - 1, pos.z), srcColor);
			dy *= 2;
			dy = 1 - dy;
		} else {
			dy = 0;
		}
		if (dz > 0.5) {
			bz = getLight(level, engine, new BlockPos(pos.x, pos.y, pos.z + 1), srcColor);
			dz -= 0.5;
			dz *= 2;
		} else if (dz < 0.5) {
			bz = getLight(level, engine, new BlockPos(pos.x, pos.y, pos.z - 1), srcColor);
			dz *= 2;
			dz = 1 - dz;
		} else {
			dz = 0;
		}
		return average(srcColor, bx, by, bz, dx, dy, dz);
	}
	
	public static Color getLight(Level level, LightManager manager, BlockPos pos, Color defaultColor) {
		BlockState state = level.getBlockState(pos);
		int lb = state.getLightBlock(level, pos);
		if (lb == 15) return defaultColor;
		return manager.getColor(pos);
	}
}
