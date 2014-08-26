package se.mistas.optimization.processors;

import java.lang.invoke.MethodType;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import se.mistas.optimization.EntryPoint;
import se.mistas.optimization.Processor;
import se.mistas.optimization.annotations.tailoptimization.TailMethod;

@SuppressWarnings("unchecked")
public class TailCheckProcessor implements Processor, Opcodes {
	private final EntryPoint ep;
	
	public TailCheckProcessor(EntryPoint ep) {
		this.ep = ep;
	}
	
	@Override
	public void visit(ClassNode c, AnnotationNode an) {
		for(MethodNode mn : (List<MethodNode>)c.methods) {
			if(ep.candidateMethod(mn, TailMethod.class) != null)
				visit(mn);
		}
	}
	
	public void visit(MethodNode mn) {
		MethodInsnNode invocation;
		AbstractInsnNode insns[] = mn.instructions.toArray(); 
		for(int i = 0; i < insns.length; i++) {
			if(insns[i] instanceof MethodInsnNode) {
				invocation = (MethodInsnNode) insns[i];
				if(invocation.owner.equals(ep.root) && invocation.name.equals(mn.name) && invocation.desc.equals(mn.desc) && insns.length != i+1 && isReturn(insns[i+1])) {
					LabelNode begin = new LabelNode();
					InsnList l = new InsnList();
					l.add(begin);
					l.add(new FrameNode(0, 0, new Object[0], 0, new Object[0]));
					mn.instructions.insert(l);
					MethodType mt = MethodType.fromMethodDescriptorString(mn.desc, null);
					Class<?> params[] = mt.parameterArray();
					l = new InsnList();
					int self = 0, offset = 0;
					if((mn.access & ACC_STATIC) == 0)
						self = 1;
					for(int j = 0; j < params.length; j++) {
						l.insert(new VarInsnNode(ISTORE+typeOpCode(params[j]), j+self+offset));
						if(params[j] == long.class || params[j] == double.class)
							offset++;
					}
					if(self == 1)
						l.add(new VarInsnNode(ASTORE, 0));
					l.add(new JumpInsnNode(GOTO, begin));
					mn.instructions.insert(insns[i], l);
					mn.instructions.remove(insns[i]);
					mn.instructions.remove(insns[i+1]);
				}
			}
		}
	}
	
	public int typeOpCode(Class<?> c) {
		if(c == int.class || c == char.class || c == byte.class || c == short.class)
			return 0;
		else if(c == long.class)
			return 1;
		else if(c == float.class)
			return 2;
		else if(c == double.class)
			return 3;
		else
			return 4;
	}
	
	public boolean isReturn(AbstractInsnNode insn) {
		if(insn instanceof InsnNode) {
			int op = ((InsnNode) insn).getOpcode();
			return op >= IRETURN && op <= RETURN;
		}
		else
			return false;
	}
}
