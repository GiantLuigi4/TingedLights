package tfc.tingedlights.api.interfaces;

@FunctionalInterface
public interface QuadFunction<T, U, V, W, X> {
	X accept(T t, U u, V v, W w);
}
