package tfc.tingedlights.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class OnThread {
	private static final ArrayList<Event> events = new ArrayList<>();
	
	private static long gameTime() {
		Level lvl = Minecraft.getInstance().level;
		if (lvl == null) return 0;
		return Minecraft.getInstance().level.getGameTime();
	}
	
	// light addition must be called after lighting is enabled for a chunk
	// the "later" parameter tells OnThread to run the events after any non-later events are run
	public static void runOnMainThread(Runnable r, int delay) {
		//@formatter:off
		if (delay == 0 && RenderSystem.isOnRenderThread()) r.run();
		else synchronized (events) { events.add(new Event(r, gameTime() + delay)); }
		//@formatter:on
	}
	
	public static void run() {
		if (Minecraft.getInstance().screen instanceof LevelLoadingScreen) return;
		if (Minecraft.getInstance().screen instanceof ReceivingLevelScreen) return;
		
		if (!events.isEmpty()) {
			synchronized (events) {
				ArrayList<Integer> toRemove = new ArrayList<>();
				for (int i = events.size() - 1; i >= 0; i--) {
					Event e = events.get(i);
					if (e.shouldRun()) {
						e.run();
						toRemove.add(i);
						break;
					}
				}
				toRemove.forEach((i) -> events.remove((int) i));
			}
		}
	}
	
	static class Event {
		Runnable r;
		long time;
		
		public Event(Runnable r, long time) {
			this.r = r;
			this.time = time;
		}
		
		boolean shouldRun() {
			return gameTime() - time >= 0;
		}
		
		void run() {
			r.run();
		}
	}
}
