package tfc.tingedlights.util.set;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class Array3dHashSet<T> implements Set<T> {
	HashAlgorithm<T> algorithm;
	
	int size = 0;
	int sizeX = 0;
	
	int[] hashes = new int[0];
	int[] sizes = new int[0];
	int[][] hashesY = new int[0][];
	T[][][] vals = (T[][][]) new Object[0][][];
	int[][] listSizes = new int[0][];
	
	public Array3dHashSet(HashAlgorithm<T> algorithm) {
		this.algorithm = algorithm;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	
	int binSearchX(int trg) {
		if (sizeX == 0) return -1;
		int lIndex = Integer.MAX_VALUE;
		int hIndex = -1;
		int index = (sizeX - 1) / 2;
		
		int[] hashes = this.hashes;
		int[] sizes = this.sizes;
		T[][][] vals = this.vals;
		
		int half = index;
		
		{
			int hash = hashes[index];
			if (trg == hash) return index;
			
			if (trg > hash) index = (index + sizeX) / 2;
			else index /= 2;
			
			if (index < half) {
				lIndex = 0;
				hIndex = half;
			} else {
				lIndex = half;
				hIndex = sizeX - 1;
			}
		}
		
		while (index != lIndex) {
			if (hashes[index] != trg) {
				int cHash = hashes[index];
				
				if (trg == cHash) return index;
				
				// TODO: do this better
				int lastIndex = index;
				if (trg > cHash) {
					index = (index + hIndex) / 2;
					if (index == lastIndex) index = hIndex;
					lIndex = lastIndex;
				} else {
					index = (index + lIndex) / 2;
					if (index == lastIndex) index = lIndex;
					hIndex = lastIndex;
				}
			} else {
				return index;
			}
		}
		int cHash = hashes[index];
		if (trg == cHash) return index;
		
		return -1;
	}
	
	int binSearchY(int[] hashes, int trg) {
		int sizeX = hashes.length;
		if (sizeX == 0) return -1;
		int lIndex = Integer.MAX_VALUE;
		int hIndex = -1;
		int index = (sizeX - 1) / 2;
		
		int half = index;
		
		{
			int hash = hashes[index];
			if (trg == hash) return index;
			
			if (trg > hash) index = (index + sizeX) / 2;
			else index /= 2;
			
			if (index < half) {
				lIndex = 0;
				hIndex = half;
			} else {
				lIndex = half;
				hIndex = sizeX - 1;
			}
		}
		
		while (index != lIndex) {
			if (hashes[index] != trg) {
				int cHash = hashes[index];
				
				if (trg == cHash) return index;
				
				// TODO: do this better
				int lastIndex = index;
				if (trg > cHash) {
					index = (index + hIndex) / 2;
					if (index == lastIndex) index = hIndex;
					lIndex = lastIndex;
				} else {
					index = (index + lIndex) / 2;
					if (index == lastIndex) index = lIndex;
					hIndex = lastIndex;
				}
			} else {
				return index;
			}
		}
		int cHash = hashes[index];
		if (trg == cHash) return index;
		
		return -1;
	}
	
	@Override
	public boolean contains(Object o) {
		T t = (T) o;
		int index = binSearchX(algorithm.hash0(t));
		if (index == -1) return false;
		int[] hashesY = this.hashesY[index];
		int indexY = binSearchY(hashesY, algorithm.hash1(t));
		if (indexY == -1) return false;
		
		T[] vals = this.vals[index][indexY];
		for (T val : vals)
			if (algorithm.equals(t, val))
				return true;
		return false;
	}
	
	@NotNull
	@Override
	public Iterator<T> iterator() {
		if (size == 0) {
			return new Iterator<T>() {
				@Override
				public boolean hasNext() {
					return false;
				}
				
				@Override
				public T next() {
					throw new RuntimeException("Cannot get an element from an empty iterator");
				}
			};
		}
		
		T[][][] vals = this.vals;
		
		// TODO: deal with empty arrays
		return new Iterator<T>() {
			int x = 0;
			int y = 0;
			int z = 0;
			T[][] arrayY = vals[0];
			int sizeY = arrayY.length;
			T[] arrayZ = vals[0][0];
			int sizeZ = listSizes[x][y];
			
			@Override
			public boolean hasNext() {
				return x < sizeX;
			}
			
			void nextIndex() {
				z++;
				if (z >= sizeZ) {
					z = 0;
					y++;
					if (y >= sizeY) {
						y = 0;
						x++;
					}
					if (x < sizeX) {
						arrayY = vals[x];
						arrayZ = arrayY[y];
						sizeY = arrayY.length;
						sizeZ = listSizes[x][y];
					}
				}
			}
			
			@Override
			public T next() {
				T obj = arrayZ[z];
				nextIndex();
				while (sizeY == 0 || sizeZ == 0) {
					nextIndex();
				}
				return obj;
			}
		};
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		T[] ts = (T[]) new Object[size];
		int index = 0;
		// TODO: optimize
		//		 index incrementation in a regular for loop is slow
		for (T t : this)
			ts[index++] = t;
		return ts;
	}
	
	@NotNull
	@Override
	public <T1> T1[] toArray(@NotNull T1[] a) {
		return (T1[]) toArray();
	}
	
	@Override
	public boolean add(T t) {
		int hash = algorithm.hash0(t);
		int index = binSearchX(hash);
		if (index == -1) {
			sizeX++;
			//@formatter:off
			  int[] newHashes = new int[sizeX];
			  int[] newSizes = new int[sizeX];
			int[][] newHashesY = new int[sizeX][];
			int[][] newListSizes = new int[sizeX][];
			T[][][] newValues = (T[][][]) new Object[sizeX][][];
			//@formatter:on
			int oset = 0;
			boolean added = false;
			for (int i = 0; i < sizeX - 1; i++) {
				int hs = hashes[i];
				if (hs > hash) {
					if (!added) {
						oset = 1;
						newHashes[i] = hash;
						newHashesY[i] = new int[0];
						newSizes[i] = 0;
						newValues[i] = (T[][]) new Object[0][];
						index = i;
						added = true;
					}
				}
				newHashes[i + oset] = hs;
				newSizes[i + oset] = sizes[i];
				newHashesY[i + oset] = hashesY[i];
				newListSizes[i + oset] = listSizes[i];
				newValues[i + oset] = vals[i];
			}
			
			if (!added) {
				int i = sizeX - 1;
				newHashes[i] = hash;
				newHashesY[i] = new int[0];
				newSizes[i] = 0;
				newListSizes[i] = new int[0];
				newValues[i] = (T[][]) new Object[0][];
				index = i;
			}
			
			this.hashes = newHashes;
			this.sizes = newSizes;
			this.listSizes = newListSizes;
			this.hashesY = newHashesY;
			this.vals = newValues;
		}
		
		int[] hashes = hashesY[index];
		int[] lSizes = listSizes[index];
		int indexY = binSearchY(hashes, algorithm.hash1(t));
		T[][] values = vals[index];
		if (indexY == -1) {
			int sizeX = sizes[index] = sizes[index] + 1;
			
			//@formatter:off
			int[] newHashesY = new int[sizeX];
			int[] newSizes = new int[sizeX];
			T[][] newValues = (T[][]) new Object[sizeX][];
			//@formatter:on
			int oset = 0;
			boolean added = false;
			for (int i = 0; i < sizeX - 1; i++) {
				int hs = hashes[i];
				if (hs > hash) {
					if (!added) {
						oset = 1;
						newHashesY[i] = algorithm.hash1(t);
						newSizes[i] = 0;
						newValues[i] = (T[]) new Object[1];
						indexY = i;
						added = true;
					}
				}
				newHashesY[i + oset] = hashes[i];
				newSizes[i + oset] = lSizes[i];
				newValues[i + oset] = values[i];
			}
			
			if (!added) {
				int i = sizeX - 1;
				newHashesY[i] = algorithm.hash1(t);
				newSizes[i] = 0;
				newValues[i] = (T[]) new Object[1];
				indexY = i;
			}
			
			this.hashesY[index] = newHashesY;
			values = this.vals[index] = newValues;
			lSizes = this.listSizes[index] = newSizes;
		}
		
		int lSize = lSizes[indexY];
		T[] array = values[indexY];
		for (int i = 0; i < lSize; i++) {
			T t1 = array[i];
			if (algorithm.equals(t1, t)) {
				return false; // TODO: check
			}
		}
		
		if (lSize >= array.length) {
			T[] newArray = (T[]) new Object[(array.length + 1) * 2];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[array.length] = t;
			values[indexY] = newArray;
		} else {
			array[lSize] = t;
		}
		size++;
		lSizes[indexY]++;
		
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		return false;
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return false;
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends T> c) {
		return false;
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return false;
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return false;
	}
	
	@Override
	public void clear() {
		hashes = new int[0];
		sizes = new int[0];
		hashesY = new int[0][];
		vals = (T[][][]) new Object[0][][];
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Array3dHashSet set) {
			// TODO: benchmark
			if (set.sizeX == sizeX) {
				if (set.size == size) {
					if (Arrays.equals(set.hashes, hashes)) {
						return Arrays.deepEquals(set.vals, vals);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int h = 0;
		for (T obj : this) {
			if (obj != null)
				h += obj.hashCode();
		}
		return h;
	}
}
