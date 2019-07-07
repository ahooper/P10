package ca.nevdull.p10a.compiler;

public class ArrayType extends Type.Reference {
	private Type element;

	public ArrayType(Type element) {
		super(element.getName()+"[]");
		this.element = element;
	}

	public Type getElement() {
		return element;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayType other = (ArrayType) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}

}
