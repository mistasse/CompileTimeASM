package se.mistas.optimization;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public interface Processor {
	void visit(ClassNode c, AnnotationNode an);
}
