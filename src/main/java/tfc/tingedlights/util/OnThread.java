package tfc.tingedlights.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;

import java.util.ArrayList;

public class OnThread {
	private static final ArrayList<Runnable> events = new ArrayList<>();
	
	// light addition must be called after lighting is enabled for a chunk
	// the "later" parameter tells OnThread to run the events after any non-later events are run
	public static void runOnMainThread(Runnable r) {
		//@formatter:off
		if (RenderSystem.isOnRenderThread()) r.run();
		else synchronized (events) { events.add(r); }
		//@formatter:on
	}
	
	public static void run() {
		if (Minecraft.getInstance().screen instanceof LevelLoadingScreen) return;
		if (Minecraft.getInstance().screen instanceof ReceivingLevelScreen) return;
		
		if (!events.isEmpty()) {
			synchronized (events) {
				for (Runnable event : events) event.run();
				events.clear();
			}
		}
	}
}
