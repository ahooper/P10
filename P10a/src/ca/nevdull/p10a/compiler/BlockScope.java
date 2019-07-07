package ca.nevdull.p10a.compiler;

public class BlockScope extends Scope {

	public BlockScope(String name, Scope enclosingScope) {
		super(name, enclosingScope);
	}

}
