package tfc.tingedlights.util;

import net.minecraftforge.coremod.api.ASMAPI;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MixinConnector implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	// tests if the classloader contains a .class file for the target
	protected static boolean testClass(String path) {
		ClassLoader loader = MixinConnector.class.getClassLoader();
		InputStream stream = loader.getResourceAsStream(path.replace(".", "/") + ".class");
		if (stream != null) {
			try {
				stream.close();
				return true;
			} catch (Throwable ignored) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("tfc.tingedlights.mixin.backend.vanilla"))
			return !testClass("ca.spottedleaf.starlight.mixin.common.lightengine.LevelLightEngineMixin");
		else if (mixinClassName.startsWith("tfc.tingedlights.mixin.backend.starlight"))
			return testClass("ca.spottedleaf.starlight.mixin.common.lightengine.LevelLightEngineMixin");
		return true;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}
	
	@Override
	public List<String> getMixins() {
		return null;
	}
	
	protected int transformAccess(int access) {
		int out = Modifier.PUBLIC;
		if (Modifier.isTransient(access)) out |= Modifier.TRANSIENT;
		if (Modifier.isAbstract(access)) out |= Modifier.ABSTRACT;
		if (Modifier.isStatic(access)) out |= Modifier.STATIC;
		if (Modifier.isSynchronized(access)) out |= Modifier.SYNCHRONIZED;
		if (Modifier.isStrict(access)) out |= Modifier.STRICT;
		if (Modifier.isInterface(access)) out |= Modifier.INTERFACE;
		if (Modifier.isNative(access)) out |= Modifier.NATIVE;
		if (Modifier.isVolatile(access)) out |= Modifier.VOLATILE;
		return out;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (mixinClassName.contains("starlight")) {
			if (
					mixinClassName.equals("tfc.tingedlights.mixin.backend.starlight.BlockStarLightEngineMixin") ||
							mixinClassName.equals("tfc.tingedlights.mixin.backend.starlight.StarLightEngineMixin") ||
							mixinClassName.equals("tfc.tingedlights.mixin.backend.starlight.StarLightInterfaceMixin")
			) {
				targetClass.access = transformAccess(targetClass.access);
				for (FieldNode field : targetClass.fields) field.access = transformAccess(field.access);
				for (MethodNode field : targetClass.methods) field.access = transformAccess(field.access);
			} else if (mixinClassName.equals("tfc.tingedlights.mixin.backend.starlight.ColoredStarlightEngineMixin")) {
				String oldSuper = targetClass.superName;
				
				targetClass.superName = "ca/spottedleaf/starlight/common/light/BlockStarLightEngine";
				
				for (MethodNode method : targetClass.methods) {
					for (AbstractInsnNode instruction : method.instructions) {
						if (instruction instanceof MethodInsnNode node) {
							// swap out method calls to match the correct super name
							if (node.owner.equals(oldSuper)) {
								node.owner = targetClass.superName;
							}
						}
					}
				}
			} else if (mixinClassName.equals("tfc.tingedlights.mixin.backend.starlight.ColoredLightInterfaceMixin")) {
				String oldSuper = targetClass.superName;
				
				targetClass.superName = "ca/spottedleaf/starlight/common/light/StarLightInterface";
				
				for (MethodNode method : targetClass.methods) {
					for (AbstractInsnNode instruction : method.instructions) {
						if (instruction instanceof MethodInsnNode node) {
							// swap out method calls to match the correct super name
							if (node.owner.equals(oldSuper)) {
								node.owner = targetClass.superName;
							}
						}
					}
				}
			}
		}
	}
	
	protected static HashMap<String, Object> values(AnnotationNode node) {
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
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (
				mixinClassName.startsWith("tfc.tingedlights.mixin.backend.starlight")
		) {
			MethodNode updateSection = null;
			MethodNode enableLights = null;
			MethodNode runUpdates = null;
			for (MethodNode method : targetClass.methods) {
				if (method.visibleAnnotations != null) {
					for (AnnotationNode invisibleAnnotation : method.visibleAnnotations) {
						if (invisibleAnnotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")) {
							String name = values(invisibleAnnotation).get("mixin").toString();
							
							if (name.equals("tfc.tingedlights.mixin.backend.starlight.LevelLightEngineMixin")) {
								if (method.name.contains("preUpdateSection")) updateSection = method;
								if (method.name.contains("postEnableLights")) enableLights = method;
								if (method.name.contains("preRunUpdates")) runUpdates = method;
							}
						}
					}
				}
			}
			
			for (MethodNode method : targetClass.methods) {
				if (method.name.equals(ASMAPI.mapMethod("updateSectionStatus"))) {
					InsnList list1 = new InsnList();
					list1.add(new VarInsnNode(Opcodes.ALOAD, 0));
					list1.add(new VarInsnNode(Opcodes.ALOAD, 1));
					list1.add(new VarInsnNode(Opcodes.ILOAD, 2));
					list1.add(ASMAPI.buildMethodCall(targetClassName.replace(".", "/"), updateSection.name, updateSection.desc, ASMAPI.MethodType.VIRTUAL));
					
					method.instructions.insert(list1);
				} else if (method.name.equals(ASMAPI.mapMethod("enableLightSources"))) {
					InsnList list1 = new InsnList();
					list1.add(new VarInsnNode(Opcodes.ALOAD, 0));
					list1.add(new VarInsnNode(Opcodes.ALOAD, 1));
					list1.add(new VarInsnNode(Opcodes.ILOAD, 2));
					list1.add(ASMAPI.buildMethodCall(targetClassName.replace(".", "/"), enableLights.name, enableLights.desc, ASMAPI.MethodType.VIRTUAL));
					
					method.instructions.insert(list1);
				} else if (method.name.equals(ASMAPI.mapMethod("runUpdates"))) {
					InsnList list1 = new InsnList();
					list1.add(new VarInsnNode(Opcodes.ALOAD, 0));
					list1.add(ASMAPI.buildMethodCall(targetClassName.replace(".", "/"), runUpdates.name, runUpdates.desc, ASMAPI.MethodType.VIRTUAL));
					
					method.instructions.insert(list1);
				}
			}
//			try {
//				FileOutputStream outputStream = new FileOutputStream(targetClass.name.substring(targetClass.name.lastIndexOf("/") + 1) + "-post.class");
//				ClassWriter writer = new ClassWriter(0);
//				targetClass.accept(writer);
//				outputStream.write(writer.toByteArray());
//				outputStream.flush();
//				outputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.out.println("Failed to transform class " + targetClassName + " with mixin " + mixinClassName);
//			}
		}
	}
}
