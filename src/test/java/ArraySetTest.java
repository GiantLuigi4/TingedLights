import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.antlr.v4.runtime.misc.AbstractEqualityComparator;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import tfc.tingedlights.util.set.Array3dHashSet;
import tfc.tingedlights.util.set.HashAlgorithm;

import java.util.*;

public class ArraySetTest {
	protected static HashAlgorithm<int[]> algorithm = new HashAlgorithm<>() {
		@Override
		public int hash0(int[] obj) {
			int hc = 0;
			for (int i = 0; i < obj.length / 2; i++)
				hc += obj[i];
			return hc;
		}
		
		@Override
		public int hash1(int[] obj) {
			int hc = 0;
			for (int i = obj.length - 1; i >= obj.length / 2; i--)
				hc += obj[i];
			return hc;
		}
		
		@Override
		public boolean equals(int[] obj0, int[] obj1) {
			return Arrays.equals(obj0, obj1);
		}
	};
	
	protected static AbstractEqualityComparator<int[]> equalityFunction = new AbstractEqualityComparator<>() {
		@Override
		public int hashCode(int[] obj) {
			int hc = 0;
			for (int i : obj)
				hc += i;
			return hc;
		}
		
		@Override
		public boolean equals(int[] a, int[] b) {
			return Arrays.equals(a, b);
		}
	};
	
	static int[][] ARRAYS;
	
	static {
		Random rng = new Random(43827498);
		ARRAYS = new int[1000][];
		for (int i = 0; i < ARRAYS.length; i++) {
			int len = rng.nextInt(3000) + 300000;
			int[] ints = new int[len];
			for (int x = 0; x < len; x++) {
				ints[x] = rng.nextInt(15);
			}
			ARRAYS[i] = ints;
		}
	}
	
	protected static void testSet(Set<int[]> set) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < ARRAYS.length; i++) {
			set.add(ARRAYS[i]);
		}
		System.out.println("- Add Elements: " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		int total = 0;
		int amountIterated = 0;
		for (int[] ints : set) {
			for (int anInt : ints) {
				total += anInt;
			}
			amountIterated++;
		}
		System.out.println("- Iter: " + (System.currentTimeMillis() - start));
		System.out.println("- Sum: " + total);
		System.out.println("- Iterated Over: " + amountIterated);
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			Set<int[]> set;
			System.out.println("Array3DHashSet");
			set = new Array3dHashSet<>(algorithm);
			testSet(set);
			System.out.println("Array2DHashSet");
			set = new Array2DHashSet<>(equalityFunction);
			testSet(set);
			System.out.println("LinkedHashSet");
			set = new LinkedHashSet<>();
			testSet(set);
			System.out.println("HashSet");
			set = new HashSet<>();
			testSet(set);
			System.out.println("ObjectOpenCustom");
			set = new ObjectOpenCustomHashSet<>(new Hash.Strategy<int[]>() {
				@Override
				public int hashCode(int[] o) {
					return equalityFunction.hashCode(o);
				}
				
				@Override
				public boolean equals(int[] a, int[] b) {
					return equalityFunction.equals(a, b);
				}
			});
			testSet(set);
		}
	}
}
