package tfc.tingedlights.util;

import java.util.ArrayList;

public class OffThread {
	protected ArrayList<Runnable> actions = new ArrayList<>();
	
	protected final Thread thread = new Thread(() -> {
		while (true) {
			try {
				if (!actions.isEmpty()) {
					ArrayList<Runnable> actions;
					synchronized (this.actions) {
						actions = this.actions;
						this.actions = new ArrayList<>();
					}
					for (Runnable runnable : actions) {
						try {
							runnable.run();
						} catch (Throwable ignored) {
							ignored.printStackTrace();
						}
					}
				}
				if (actions.isEmpty()) {
					Thread.sleep(1);
				}
			} catch (Throwable ignored) {
			}
		}
	});
	
	public OffThread() {
		thread.start();
	}
	
	public void addAction(Runnable r) {
		synchronized (actions) {
			actions.add(r);
		}
	}
}
