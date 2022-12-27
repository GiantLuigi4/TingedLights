import tfc.tingedlights.data.struct.LightChunk;

public class Test {
	public static void main(String[] args) {
		for (int i = 0; i < 10000; i++) {
			// force JIT compilation
			LightChunk chunk = LightPropagationTest.dummyPropagation();
		}
		
		double avg = 0;
		for (int i = 0; i < 1000; i++) {
			long start = System.nanoTime();
			LightChunk chunk = LightPropagationTest.dummyPropagation();
			long end = System.nanoTime();
			System.out.println(end - start);
			System.out.println(chunk);
			if (avg == 0) avg = end - start;
			else {
				avg += end - start;
				avg /= 2;
			}
		}
		System.out.println("Average: " + avg);
	}
}
