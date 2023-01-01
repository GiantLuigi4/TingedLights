import it.unimi.dsi.fastutil.Function;
import net.minecraft.util.Mth;

import java.util.Random;
import java.util.function.IntPredicate;

public class BinSearchTests {
	protected static int binSearchX(int minX, int sizeX, Function<Integer, Integer> map, int trg) {
		if (sizeX == minX) return -1;
		int lIndex = Integer.MAX_VALUE;
		int hIndex = -1;
		int index = ((sizeX + minX) - 1) / 2;
		
		int half = index;
		
		{
			int hash = map.apply(index);
			if (trg == hash) return index;
			
			if (trg > hash) index = (index + sizeX) / 2;
			else index = (index + minX) / 2;
			
			if (index < half) {
				lIndex = minX;
				hIndex = half;
			} else {
				lIndex = half;
				hIndex = sizeX - 1;
			}
		}
		
		while (index != lIndex) {
			if (map.apply(index) != trg) {
				int cHash = map.apply(index);
				
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
		return index;
	}
	
	public static int binarySearch(int minX, int sizeX, IntPredicate pIsTargetBeforeOrAt) {
		if (sizeX == minX) return -1;
		int lIndex = Integer.MAX_VALUE;
		int hIndex = -1;
		int index = ((sizeX + minX) - 1) / 2;
		
		int half = index;
		
		{
			if (pIsTargetBeforeOrAt.test(index)) index = (index + minX) / 2;
			else index = (index + sizeX) / 2;
			
			if (index < half) {
				lIndex = minX;
				hIndex = half;
			} else {
				lIndex = half;
				hIndex = sizeX - 1;
			}
		}
		
		while (index != lIndex) {
			boolean isBeforeOrAt = pIsTargetBeforeOrAt.test(index);
			if (isBeforeOrAt && pIsTargetBeforeOrAt.test(index - 1) != pIsTargetBeforeOrAt.test(index + 1)) {
				return index;
			}
			
			int lastIndex = index;
			if (isBeforeOrAt) {
				index = (index + lIndex) / 2;
				if (index == lastIndex) index = lIndex;
				hIndex = lastIndex;
			} else {
				index = (index + hIndex) / 2;
				if (index == lastIndex) index = hIndex;
				lIndex = lastIndex;
			}
		}
		return index;
	}
	
	public static void main(String[] args) {
		int min = Integer.MIN_VALUE;
		int max = Integer.MAX_VALUE;
//		int trg = new Random().nextInt(max - min) + min;
		int trg = new Random().nextInt(Integer.MAX_VALUE);
		
		System.out.println("-- [Test Info] --");
		System.out.println("Min bound: " + min);
		System.out.println("Max bound: " + max);
		System.out.println("Target   : " + trg);
		
		long minMoj = Long.MAX_VALUE;
		long minMineMoj = Long.MAX_VALUE;
		long minMine = Long.MAX_VALUE;
		long totalMoj = 0;
		long totalMineMoj = 0;
		long totalMine = 0;
		
		int testCount = 1;
		for (int i = 0; i < testCount; i++) {
			System.out.println("-- [Test " + i + "] --");
			{
				System.out.println("[Moj]");
				long start = System.nanoTime();
				System.out.println("result: " + Mth.binarySearch(
						min, max,
						(v) -> v >= trg
				));
				long duration;
				System.out.println("took: " + (duration = (System.nanoTime() - start)));
				
				minMoj = Math.min(minMoj, duration);
				totalMoj += duration;
			}
			
			{
				System.out.println("[Mine under Moj]");
				long start = System.nanoTime();
				System.out.println("result: " + binarySearch(
						min, max,
						(v) -> v >= trg
				));
				long duration;
				System.out.println("took: " + (duration = (System.nanoTime() - start)));
				
				minMineMoj = Math.min(minMineMoj, duration);
				totalMineMoj += duration;
			}
			
			{
				System.out.println("[Mine]");
				long start = System.nanoTime();
				System.out.println("result: " + binSearchX(
						min, max,
						(v) -> (Integer) v,
						trg
				));
				long duration;
				System.out.println("took: " + (duration = (System.nanoTime() - start)));
				
				minMine = Math.min(minMine, duration);
				totalMine += duration;
			}
		}
		System.out.println("-- [Mins] --");
		System.out.println("Moj			  : " + minMoj);
		System.out.println("Mine under Moj: " + minMineMoj);
		System.out.println("Mine		  : " + minMine);
		System.out.println("-- [Avgs] --");
		System.out.println("Moj			  : " + totalMoj / testCount);
		System.out.println("Mine under Moj: " + totalMineMoj / testCount);
		System.out.println("Mine		  : " + totalMine / testCount);
	}
}
