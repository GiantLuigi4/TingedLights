package tfc.tingedlights.util.asm.util;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import sun.misc.Unsafe;
import tfc.tingedlights.util.asm.struct.BaseStruct;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnnotationParser {
	public static List<AnnotationNode> allAnnotations(ClassNode node) {
		ArrayList<AnnotationNode> nodes = new ArrayList<>();
		if (node.visibleAnnotations != null) nodes.addAll(node.visibleAnnotations);
		if (node.invisibleAnnotations != null) nodes.addAll(node.invisibleAnnotations);
		return nodes;
	}
	
	public static List<AnnotationNode> allAnnotations(MethodNode node) {
		ArrayList<AnnotationNode> nodes = new ArrayList<>();
		if (node.visibleAnnotations != null) nodes.addAll(node.visibleAnnotations);
		if (node.invisibleAnnotations != null) nodes.addAll(node.invisibleAnnotations);
		return nodes;
	}
	
	public static HashMap<String, Object> getValues(AnnotationNode node) {
		boolean alt = false;
		HashMap<String, Object> out = new HashMap<>();
		String v = null;
		for (Object value : node.values) {
			alt = !alt;
			if (alt) v = value.toString();
			else out.put(v, value);
		}
		return out;
	}
	
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Class<Unsafe> unsafeClass = Unsafe.class;
			Field f = unsafeClass.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public static <T> T parseInto(AnnotationNode node, Class<T> targetClass) {
		try {
			T t = (T) theUnsafe.allocateInstance(targetClass);
			BaseStruct baseStruct = null;
			if (t instanceof BaseStruct base) baseStruct = base;
			
			HashMap<String, Object> values = getValues(node);
			
			for (Field declaredField : targetClass.getDeclaredFields()) {
				if (!Modifier.isStatic(declaredField.getModifiers())) {
					long offset = theUnsafe.objectFieldOffset(declaredField);
					
					Class<?> clazz = declaredField.getType();
					Object o = values.get(declaredField.getName());
					
					// TODO: more sane way of handling arrays
					if (clazz.equals(String.class)) {
						if (o == null) {
							if (baseStruct != null && baseStruct.handleNull(declaredField.getName()))
								continue;
							o = "null";
						}
						theUnsafe.putObject(t, offset, o.toString());
					} else if (clazz.equals(String[].class)) {
						if (o == null) {
							if (baseStruct != null && baseStruct.handleNull(declaredField.getName()))
								continue;
							o = new ArrayList<>();
						}
						List<?> strings = (List<?>) o;
						String[] value = new String[strings.size()];
						for (int i = 0; i < strings.size(); i++)
							value[i] = strings.get(i).toString();
						theUnsafe.putObject(t, offset, value);
					} else if (BaseStruct.class.isAssignableFrom(clazz)) {
						if (o == null) {
							if (baseStruct != null && baseStruct.handleNull(declaredField.getName()))
								continue;
							theUnsafe.putObject(t, offset, null);
							continue;
						}
						theUnsafe.putObject(t, offset, parseInto((AnnotationNode) o, clazz));
					} else if (clazz.isArray() && BaseStruct.class.isAssignableFrom(clazz.getComponentType())) {
						if (o == null) {
							if (baseStruct != null && baseStruct.handleNull(declaredField.getName()))
								continue;
							theUnsafe.putObject(t, offset, null);
							o = new ArrayList<>();
						}
						List<?> nodes = (List<?>) o;
						Object[] value = (Object[]) Array.newInstance(clazz.getComponentType(), nodes.size());
						for (int i = 0; i < nodes.size(); i++) {
							AnnotationNode node1 = (AnnotationNode) nodes.get(i);
							value[i] = parseInto(node1, clazz.getComponentType());
						}
						theUnsafe.putObject(t, offset, value);
					} else if (clazz.equals(boolean.class)) {
						if (o == null) {
							if (baseStruct != null && baseStruct.handleNull(declaredField.getName()))
								continue;
							o = false;
						}
						theUnsafe.putBoolean(t, offset, (boolean) o);
					}
					//
					//
					else {
						throw new RuntimeException("Unsupported annotation value type " + clazz);
					}
				}
			}
			
			return t;
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
}
