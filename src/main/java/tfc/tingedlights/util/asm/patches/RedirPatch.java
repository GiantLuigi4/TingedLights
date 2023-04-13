package tfc.tingedlights.util.asm.patches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tfc.tingedlights.util.asm.Patch;
import tfc.tingedlights.util.asm.struct.template.MethodTargetStruct;
import tfc.tingedlights.util.asm.util.InsnCopy;
import tfc.tingedlights.util.asm.util.MethodCall;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RedirPatch extends Patch {
	String target;
	Predicate<String> targetPattern;
	boolean isNameOnly = true;
	String[] redirOwner;
	String[] redir;
	MethodTargetStruct[] exclude;
	
	final MethodNode node;
	String methodName;
	
	public RedirPatch(String src, String target, String[] redirs, MethodTargetStruct[] exclude, MethodNode node) {
		super(src);
		
		if (target.contains("(")) isNameOnly = false;
		this.target = target;
		
		// ^ ensures that the pattern starts at the start of the string
		// $ ensures that the pattern ends at the end of the string
		// in return, this matches the entire string exactly
		targetPattern = Pattern.compile("^" + target + "$").asMatchPredicate();
		
		this.redir = new String[redirs.length];
		this.redirOwner = new String[redirs.length];
		
		for (int i = 0; i < redirs.length; i++) {
			String redir = redirs[i];
			
			String[] split = redir.split(";", 2);
			this.redirOwner[i] = split[0].substring(1);
			this.redir[i] = split[1];
			if (!redir.contains("(")) {
				String desc = node.desc;
				desc = "(" + desc.substring(desc.indexOf(";") + 1);
				this.redir[i] = this.redir[i] + desc;
			}
		}
		
		this.exclude = exclude; // TODO: regex?
		
		this.node = node;
		this.methodName = node.name;
		this.node.name = "inject$" + srcClass.replace("/", "$") + "$_call_$" + methodName;
	}
	
	protected MethodNode copy(MethodNode oldMethod, String newOwner) {
		MethodNode newNode = new MethodNode();
		for (AbstractInsnNode instruction : oldMethod.instructions) {
			AbstractInsnNode copy = InsnCopy.copyOf(instruction);
			if (copy instanceof MethodInsnNode method) {
				if (method.owner.equals(srcClass)) {
					method.owner = newOwner;
				}
			}
			newNode.instructions.add(copy);
		}
		
		newNode.name = oldMethod.name;
		newNode.desc = oldMethod.desc;
		newNode.access = oldMethod.access;
		
		newNode.maxStack = oldMethod.maxStack;
		newNode.maxLocals = oldMethod.maxLocals;
		
		newNode.attrs = oldMethod.attrs;
		newNode.signature = oldMethod.signature;
		newNode.parameters = oldMethod.parameters;
		newNode.exceptions = oldMethod.exceptions;
		
		newNode.localVariables = oldMethod.localVariables;
		newNode.tryCatchBlocks = oldMethod.tryCatchBlocks;
		
		newNode.annotationDefault = oldMethod.annotationDefault;
		
		newNode.invisibleAnnotations = oldMethod.invisibleAnnotations;
		newNode.invisibleTypeAnnotations = oldMethod.invisibleTypeAnnotations;
		newNode.invisibleParameterAnnotations = oldMethod.invisibleParameterAnnotations;
		newNode.invisibleAnnotableParameterCount = oldMethod.invisibleAnnotableParameterCount;
		newNode.invisibleLocalVariableAnnotations = oldMethod.invisibleLocalVariableAnnotations;
		
		newNode.visibleAnnotations = oldMethod.visibleAnnotations;
		newNode.visibleTypeAnnotations = oldMethod.visibleTypeAnnotations;
		newNode.visibleParameterAnnotations = oldMethod.visibleParameterAnnotations;
		newNode.visibleAnnotableParameterCount = oldMethod.visibleAnnotableParameterCount;
		newNode.visibleLocalVariableAnnotations = oldMethod.visibleLocalVariableAnnotations;
		
		return newNode;
	}
	
	@Override
	public int apply(ClassNode node) {
		int hits = super.apply(node);
		if (hits != 0)
			node.methods.add(copy(this.node, node.name));
		return hits;
	}
	
	@Override
	public int apply(ClassNode classNode, MethodNode method) {
		int countHits = 0;
		
		for (MethodTargetStruct s : exclude)
			if (s.matches(method))
				return 0;
		
		if (targetPattern.test(isNameOnly ? method.name : method.name + method.desc)) {
			HashMap<AbstractInsnNode, AbstractInsnNode> replacements = new HashMap<>();
			
			for (AbstractInsnNode instruction : method.instructions) {
				if (instruction instanceof MethodInsnNode methodInsnNode) {
					for (String s : redir) {
						if ((methodInsnNode.name + methodInsnNode.desc).equals(s)) {
							MethodInsnNode newNode = new MethodInsnNode(
									Modifier.isStatic(node.access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL,
									classNode.name,
									node.name,
									node.desc,
									false
							);
							
							replacements.put(instruction, newNode);
							
							countHits++;
						}
					}
				}
			}
			
			for (Map.Entry<AbstractInsnNode, AbstractInsnNode> abstractInsnNodeAbstractInsnNodeEntry : replacements.entrySet()) {
				MethodCall methodCall = new MethodCall(false, Modifier.isStatic(method.access), (MethodInsnNode) abstractInsnNodeAbstractInsnNodeEntry.getKey());
				if (!Modifier.isStatic(node.access) && !methodCall.hasThis) {
					method.instructions.insertBefore(
							methodCall.start,
							new VarInsnNode(Opcodes.ALOAD, 0)
					);
				}
				method.instructions.insertBefore(
						abstractInsnNodeAbstractInsnNodeEntry.getKey(),
						abstractInsnNodeAbstractInsnNodeEntry.getValue()
				);
				method.instructions.remove(abstractInsnNodeAbstractInsnNodeEntry.getKey());
			}
		}
		
		return countHits;
	}
}
