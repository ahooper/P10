package ca.nevdull.p10a.compiler;

public class ObjectType extends Type.Reference {
	protected ClassSymbol csym;

	public ObjectType(ClassSymbol csym) {
		super(csym.getName());
		this.csym = csym;
	}

	public ClassSymbol getKlass() {
		return csym;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectType other = (ObjectType) obj;
		if (csym == null) {
			if (other.csym != null)
				return false;
		} else if (!csym.equals(other.csym))
			return false;
		return true;
	}

	public boolean isSupertypeOf(Type t) {
		if (!(t instanceof ObjectType)) return false;
		ClassSymbol k = getKlass();
		for (ClassSymbol tk = ((ObjectType)t).getKlass(); tk != null; tk = tk.getSuperClass()) {
			if (tk == k) return true;
		}
		return false;
	}

}
