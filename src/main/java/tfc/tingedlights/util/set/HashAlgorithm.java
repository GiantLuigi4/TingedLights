package tfc.tingedlights.util.set;

public interface HashAlgorithm<T> {
	int hash0(T obj);
	
	int hash1(T obj);
	
	boolean equals(T obj0, T obj1);
}
