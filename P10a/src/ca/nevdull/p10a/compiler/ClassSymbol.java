package ca.nevdull.p10a.compiler;

import java.util.ArrayList;
import org.antlr.v4.runtime.Token;

public class ClassSymbol extends Scope {
	static boolean trace;

	protected String qualifiedName;
	protected ClassSymbol superClass = null;
	protected ArrayList<ClassSymbol> interfaces = new ArrayList<ClassSymbol>();
	protected int nextFieldIndex=0,
	              nextMethodIndex=0;
	protected boolean referenced, loading, loaded;
	public final static String INITIALZER_METHOD_NAME = "new";
	public final static String NEW_INSTANCE_METHOD_NAME = "_newInstance";

	public ClassSymbol(Token name, Scope enclosingScope) {
		super(name, enclosingScope);
		this.setType(new ObjectType(this)); // one instance per class
	}

	public ClassSymbol(String name, Scope enclosingScope) {
		super(name, enclosingScope);
		this.setType(new ObjectType(this)); // one instance per class
	}

	public String getFullName() {
		return qualifiedName;
	}

	public void setFullName(String fullName) {
		this.qualifiedName = fullName;
	}

	public ClassSymbol getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ClassSymbol superClass) {
		if (this.superClass != null) {
			assert this.superClass == superClass;
			assert this.nextFieldIndex == superClass.nextFieldIndex;
			assert this.nextMethodIndex == superClass.nextMethodIndex;
			return;
		} else {
			assert nextFieldIndex==0; // not previously set
		    assert nextMethodIndex==0;
		}
		if (superClass == this) {
			// enforce this to prevent infinite loop in findMember
			throw new IllegalArgumentException("Set "+this.name+" super class to self");
		}
		this.superClass = superClass;
		if (superClass == null) { // std.lang.Object
			this.nextFieldIndex = CodeGen.INSTANCE_FIELD_OFFSET;
			this.nextMethodIndex = CodeGen.VIRTUAL_METHOD_OFFSET;
			if (trace) Main.debug(getName()+" setSuperClass null nextMethodIndex "+this.nextMethodIndex);
		} else {
			assert superClass.isLoaded();
			this.nextFieldIndex = superClass.nextFieldIndex;
			this.nextMethodIndex = superClass.nextMethodIndex;
			if (trace) Main.debug(getName()+" setSuperClass "+superClass.getName()+" nextMethodIndex "+this.nextMethodIndex);
		}
	}

	static class UnknownClass extends ClassSymbol {
		public UnknownClass(Token name) {
			super(name,null);
		}
	}

	public Symbol findMember(String name) {
		Symbol r = getMembers().get(name);
		ClassSymbol sup;
		if (r == null && (sup = getSuperClass()) != null) {
			assert sup != this;
			r = sup.findMember(name);
		}
		return r;
	}
	
	public Symbol findMember(Token name) {
		return findMember(name.getText());
	}

	void assignInstanceField(FieldSymbol symbol) {
		if (symbol.isStatic()) throw new IllegalArgumentException("must not be a static field");
		assert nextFieldIndex > 0;  // check superClass has been set
		symbol.setIndex(nextFieldIndex++);
	}

	void assignVirtualMethod(MethodSymbol symbol) {
		if (symbol.isStatic()) throw new IllegalArgumentException("must not be a static method");
		assert nextMethodIndex > 0;  // check superClass has been set
		symbol.setIndex(nextMethodIndex++);
	}

	public FieldSymbol[] getInstanceFields() {
		FieldSymbol[] instanceFields = new FieldSymbol[nextFieldIndex-CodeGen.INSTANCE_FIELD_OFFSET];
		for (ClassSymbol cs = this;  cs != null;  cs = cs.getSuperClass()) {
			for (Symbol s : cs.getMembers().values()) {
				if (s instanceof FieldSymbol && !s.isStatic()) {
					FieldSymbol fs = (FieldSymbol)s;
					int fx = fs.getIndex()-CodeGen.INSTANCE_FIELD_OFFSET;
					assert instanceFields[fx] == null;
					instanceFields[fx] = fs;
				}
			}
		}
		return instanceFields;
	}

	public MethodSymbol[] getVirtualMethods() {
		MethodSymbol[] virtualMethods = new MethodSymbol[nextMethodIndex-CodeGen.VIRTUAL_METHOD_OFFSET];
		if (trace) Main.debug(getName()+" virtual methods:");
		for (ClassSymbol cs = this;  cs != null;  cs = cs.getSuperClass()) {
			for (Symbol s : cs.getMembers().values()) {
				if (s instanceof MethodSymbol && !s.isStatic()) {
					for (MethodSymbol ms = (MethodSymbol)s;  ms != null;  ms = ms.getOverloads()) { 
						int mx = ms.getIndex()-CodeGen.VIRTUAL_METHOD_OFFSET;
						if (trace) Main.debug("    "+mx+" "+ms.getDefiningScope().getName()+" "+ms);
						if (virtualMethods[mx] == null) { // not overridden in subclass
							virtualMethods[mx] = ms;
						}
					}
				}
			}
		}
		return virtualMethods;
	}

	public boolean isReferenced() {
		return referenced;
	}

	public void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
}
