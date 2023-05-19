package tfc.tingedlights.util.asm;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.tingedlights.util.asm.annotation.Hook;
import tfc.tingedlights.util.asm.annotation.MethodRedir;
import tfc.tingedlights.util.asm.annotation.RemoveMethods;
import tfc.tingedlights.util.asm.patches.RedirPatch;
import tfc.tingedlights.util.asm.patches.RemoveMethodPatch;
import tfc.tingedlights.util.asm.struct.HookStruct;
import tfc.tingedlights.util.asm.struct.MethodRedirStruct;
import tfc.tingedlights.util.asm.struct.RemoveMethodsStruct;

import java.util.ArrayList;
import java.util.List;

import static tfc.tingedlights.util.asm.util.AnnotationParser.allAnnotations;
import static tfc.tingedlights.util.asm.util.AnnotationParser.parseInto;

public class HookParser {
	public static boolean isAnnotation(Class<?> clazz, AnnotationNode node) {
		String anno = "L" + clazz.getName().replace(".", "/") + ";";
		return node.desc.equals(anno);
	}
	
	public static HookPatcher parse(ClassNode node) {
		String[] target = null;
		
		List<Patch> patches = new ArrayList<>();
		
		for (AnnotationNode allAnnotation : allAnnotations(node)) {
			if (isAnnotation(Hook.class, allAnnotation)) {
				HookStruct struct = parseInto(allAnnotation, HookStruct.class);
				target = struct.value;
			} else if (isAnnotation(RemoveMethods.class, allAnnotation)) {
				RemoveMethodsStruct struct = parseInto(allAnnotation, RemoveMethodsStruct.class);
				patches.add(new RemoveMethodPatch(node.name, struct.targets));
			}
		}
		
		if (target == null) return null;
		
		for (MethodNode method : node.methods) {
			for (AnnotationNode allAnnotation : allAnnotations(method)) {
				if (isAnnotation(MethodRedir.class, allAnnotation)) {
					MethodRedirStruct struct = parseInto(allAnnotation, MethodRedirStruct.class);
					
					patches.add(new RedirPatch(
							node.name, struct.method,
							struct.redirTarget, struct.exclude,
							method
					));
				}
			}
		}
		
		return new HookPatcher(target, patches);
	}
}
