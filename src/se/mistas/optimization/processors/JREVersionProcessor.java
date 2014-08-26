package se.mistas.optimization.processors;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import se.mistas.optimization.EntryPoint;
import se.mistas.optimization.Processor;

public class JREVersionProcessor implements Processor {
	
	public JREVersionProcessor(EntryPoint p) {
	}

	@Override
	public void visit(ClassNode c, AnnotationNode an) {
		c.version = (Integer)an.values.get(1);
	}
}
