package tfc.tingedlights;

import tfc.tingedlights.data.struct.LightNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LightSet {
	public void forEach(Consumer<LightNode> noduleConsumer) {
		lookup.noduleHashMap.forEach(m -> {
			m.noduleHashMap.forEach(e -> {
				e.noduleHashMap.forEach(a -> {
					for (LightNode nodule : a) {
						noduleConsumer.accept(nodule);
					}
				});
			});
		});
	}
	
	XLookup lookup = new XLookup(new CoordMap<>());
	
	public void add(LightNode nodule) {
		if (nodule.reference() == null) {
			lookup.put(0, 0, 0, nodule);
			return;
		}
		int relX = nodule.pos.getX() - nodule.reference().pos.getX();
		int relY = nodule.pos.getY() - nodule.reference().pos.getY();
		int relZ = nodule.pos.getZ() - nodule.reference().pos.getZ();
		lookup.put(relX, relY, relZ, nodule);
	}
	
	public void clear() {
		lookup.clear();
	}
	
	public boolean isEmpty() {
		return lookup.isEmpty();
	}
	
	public void removeAll(Set<LightNode> finishedNodules) {
		for (LightNode finishedNodule : finishedNodules) {
			remove(finishedNodule);
		}
	}
	
	public void addAll(LightSet addedNodules) {
		addedNodules.forEach(this::add);
	}
	
	public void remove(LightNode nodule) {
		Set<LightNode> nodules;
		if (nodule.reference() == null) {
			nodules = lookup.get(0, 0, 0);
		} else {
			int relX = nodule.pos.getX() - nodule.reference().pos.getX();
			int relY = nodule.pos.getY() - nodule.reference().pos.getY();
			int relZ = nodule.pos.getZ() - nodule.reference().pos.getZ();
			nodules = lookup.get(relX, relY, relZ);
		}
		if (nodules != null) nodules.remove(nodule);
	}
	
	public static class CoordMap<T> {
		T[] map = (T[]) new Object[15 + 15 + 1];
		boolean isEmpty = true;
		
		public void set(int pos, T nodule) {
			map[pos + 15] = nodule;
			isEmpty = false;
		}
		
		public T computeIfAbsent(int pos, Supplier<T> defaultValue) {
			T val = get(pos);
			if (val != null) return val;
			set(pos, val = defaultValue.get());
			return val;
		}
		
		public T get(int pos) {
			return map[pos + 15];
		}
		
		public void clear() {
			Arrays.fill(map, null);
			isEmpty = true;
		}
		
		public void forEach(Consumer<T> function) {
			for (T t : map) {
				if (t != null)
					function.accept(t);
			}
		}
		
		public void forEach(Function<T, Boolean> function) {
			for (T t : map) {
				if (t != null)
					if (function.apply(t))
						return;
			}
		}
		
		public boolean isEmpty() {
			return isEmpty;
		}
	}
	
	public record XLookup(CoordMap<YLookup> noduleHashMap) {
		public boolean isEmpty() {
			if (!noduleHashMap.isEmpty) {
				boolean[] endedEarly = new boolean[]{false};
				noduleHashMap.forEach((l) -> {
					if (!l.isEmpty())
						endedEarly[0] = true;
					return endedEarly[0];
				});
				return !endedEarly[0];
			}
			return true;
		}
		
		public record YLookup(CoordMap<ZLookup> noduleHashMap) {
			public boolean isEmpty() {
				if (!noduleHashMap.isEmpty) {
					boolean[] endedEarly = new boolean[]{false};
					noduleHashMap.forEach((l) -> {
						if (!l.isEmpty())
							endedEarly[0] = true;
						return endedEarly[0];
					});
					return !endedEarly[0];
				}
				return true;
			}
			
			public record ZLookup(CoordMap<Set<LightNode>> noduleHashMap) {
				public boolean isEmpty() {
					return noduleHashMap.isEmpty;
				}
				
				public Set<LightNode> get(int z) {
					return noduleHashMap.get(z);
				}
				
				public void put(int z, LightNode nodule) {
					Set<LightNode> nodules = noduleHashMap.computeIfAbsent(z, HashSet::new);
					nodules.add(nodule);
				}
				
				public void clear() {
					noduleHashMap.clear();
				}
			}
			
			public ZLookup get(int y) {
				return noduleHashMap.get(y);
			}
			
			public void put(int y, int z, LightNode nodule) {
				ZLookup lookup = noduleHashMap.computeIfAbsent(y, () -> new ZLookup(new CoordMap<>()));
				lookup.put(z, nodule);
			}
			
			
			public void clear() {
				noduleHashMap.forEach(ZLookup::clear);
			}
		}
		
		public Set<LightNode> get(int x, int y, int z) {
			YLookup yLookup = noduleHashMap.get(x);
			if (yLookup == null) return null;
			YLookup.ZLookup zLookup = yLookup.get(y);
			if (zLookup == null) return null;
			return zLookup.get(z);
		}
		
		public YLookup get(int x) {
			return noduleHashMap.get(x);
		}
		
		public void put(int x, int y, int z, LightNode nodule) {
			YLookup lookup = noduleHashMap.computeIfAbsent(x, () -> new YLookup(new CoordMap<>()));
			lookup.put(y, z, nodule);
		}
		
		public void clear() {
			noduleHashMap.forEach(YLookup::clear);
		}
	}
}
