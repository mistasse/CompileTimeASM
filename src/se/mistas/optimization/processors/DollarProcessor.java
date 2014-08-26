package se.mistas.optimization.processors;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import se.mistas.optimization.EntryPoint;
import se.mistas.optimization.Processor;
import se.mistas.optimization.annotations.Dollarify;

@SuppressWarnings("unchecked")
public class DollarProcessor implements Processor, Opcodes {
	private final EntryPoint p;
	private final List<MethodNode> toAdd = new LinkedList<>();
	
	public DollarProcessor(EntryPoint p) {
		this.p = p;
	}

	@Override
	public void visit(ClassNode c, AnnotationNode an) {
		AnnotationNode man;
		for(MethodNode mn : (List<MethodNode>) c.methods) {
			if((man = p.candidateMethod(mn, Dollarify.class)) != null) {
				String name = (man.values == null) ? "$" : (String)man.values.get(1);
				MethodNode newm = new MethodNode(mn.access, name, mn.desc, mn.signature, ((List<String>)mn.exceptions).toArray(new String[mn.exceptions.size()]));
				newm.instructions = mn.instructions;
				toAdd.add(newm);
			}
		}
		for(MethodNode n : toAdd)
			c.methods.add(n);
	}
}
