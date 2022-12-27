package tfc.tingedlights.api.interfaces;

@FunctionalInterface
public interface TriFunction<T, U, V, W> {
	W accept(T t, U u, V v);
}
