package se.mistas.optimization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import se.mistas.optimization.annotations.DollarClass;
import se.mistas.optimization.annotations.JREVersion;
import se.mistas.optimization.annotations.TailClass;
import se.mistas.optimization.processors.DollarProcessor;
import se.mistas.optimization.processors.JREVersionProcessor;
import se.mistas.optimization.processors.TailCheckProcessor;

@SuppressWarnings("unchecked")
public class EntryPoint implements Opcodes {
	private final HashMap<String, Processor> features = new HashMap<String, Processor>();
	private List<List<?>> deleteFrom = new LinkedList<>();
	private List<Object> toDelete = new LinkedList<>();
	private String file;
	public ClassNode root;
	
	public static final String desc(Class<?> c) {
		return "L"+c.getName().replace('.', '/')+";";
	}
	
	public EntryPoint(String name) {
		this.file = name;
		
		features.put(desc(TailClass.class), new TailCheckProcessor(this));
		features.put(desc(DollarClass.class), new DollarProcessor(this));
		features.put(desc(JREVersion.class), new JREVersionProcessor(this));
		
		try(InputStream in = new FileInputStream(name)) {
			new ClassReader(in).accept(root = new ClassNode(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process() {
		if(root.visibleAnnotations != null) {
			System.out.println("Processing "+root.name);
			Processor p;
			for(AnnotationNode an : (List<AnnotationNode>)root.visibleAnnotations) {
				if((p = features.get(an.desc)) != null) {
					deferDeletion(root.visibleAnnotations, an);
					p.visit(root, an);
				}
			}
			for(int i = 0; i < deleteFrom.size(); i++) {
				deleteFrom.get(i).remove(toDelete.get(i));
			}
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			root.accept(cw);
			try(OutputStream out = new FileOutputStream(file)) {
				out.write(cw.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deferDeletion(List<?> l, Object o) {
		deleteFrom.add(l);
		toDelete.add(o);
	}

	public AnnotationNode candidateMethod(MethodNode mn, Class<?> c) {
		if(mn.visibleAnnotations != null) {
			for(AnnotationNode an : (List<AnnotationNode>)mn.visibleAnnotations) {
				if(an.desc.equals(EntryPoint.desc(c))) {
					deferDeletion(mn.visibleAnnotations, an);
					return an;
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		for(String s : args) {
			if(s.endsWith(".class")) {
				new EntryPoint(s).process();
			}
		}
	}
}
