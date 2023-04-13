package tfc.tingedlights.util.asm.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class StackTracker {
	public static int[] getIO(AbstractInsnNode insnNode) {
		int add = 0;
		int sub = 0;
		if (
				insnNode instanceof LineNumberNode ||
						insnNode instanceof LabelNode
		) {
			return new int[]{add, sub};
		} else if (insnNode instanceof MethodInsnNode node) {
			String desc = node.desc;
			int count = 0;
			boolean inType = false;
			for (char c : desc.toCharArray()) {
				//@formatter:off
				if (c == '(') continue; // start of desc
				else if (c == ')') break; // terminator for parameters in a descriptor
				else if (c == ';') {inType = false; continue;} // L defines the type, so this has already been counted
				else if (inType) continue;
				else if (c == 'L') inType = true;
				else if (c == '[') continue; // doesn't really mean anything to the count
				count++;
				//@formatter:on
			}
			
			if (node.getOpcode() != Opcodes.INVOKESTATIC) count += 1;
			
			sub = count;
			add = ((MethodInsnNode) insnNode).desc.endsWith("V") ? 0 : 1;
		} else if (insnNode instanceof VarInsnNode) {
			sub = 1;
		} else if (insnNode instanceof FieldInsnNode) {
			if (insnNode.getOpcode() != Opcodes.GETSTATIC)
				sub = 1;
			add = 1;
		} else if (insnNode instanceof TypeInsnNode typeNode) {
			return switch (typeNode.getOpcode()) {
				case Opcodes.ANEWARRAY, Opcodes.NEW -> new int[]{1, 0};
				case Opcodes.CHECKCAST -> new int[]{0, 0};
				default -> throw new RuntimeException("NYI: TypeNode " + typeNode.getOpcode());
			};
		} else {
			throw new RuntimeException("NYI: " + insnNode.getClass() + " opcode: " + insnNode.getOpcode());
		}
		return new int[]{add, sub};
	}
	
	public static AbstractInsnNode findStart(MethodInsnNode node) {
		int size = getIO(node)[1];
		AbstractInsnNode current = node.getPrevious();
		while (size > 0) {
			int[] io = getIO(current);
			size += io[0] - io[1];
			
			if (size > 0)
				current = current.getPrevious();
		}
		if (size < 0) {
			throw new RuntimeException("Failed to track size of insn");
		}
		return current;
	}
}
