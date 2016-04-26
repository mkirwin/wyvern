package wyvern.target.corewyvernIL.expression;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wyvern.target.corewyvernIL.Environment;
import wyvern.target.corewyvernIL.astvisitor.ASTVisitor;
import wyvern.target.corewyvernIL.support.TypeContext;
import wyvern.target.corewyvernIL.support.Util;
import wyvern.target.corewyvernIL.type.ValueType;
import wyvern.target.oir.OIREnvironment;
import wyvern.tools.errors.FileLocation;

public class IntegerLiteral extends AbstractValue implements Invokable {

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntegerLiteral other = (IntegerLiteral) obj;
		if (value != other.value)
			return false;
		return true;
	}

	private int value;

	public IntegerLiteral(int value) {
		this(value, FileLocation.UNKNOWN);
	}
	public IntegerLiteral(int value, FileLocation loc) {
		super(Util.intType(), loc);
		this.value = value;
	}

	@Override
	public void doPrettyPrint(Appendable dest, String indent) throws IOException {
		dest.append(Integer.toString(value));
	}

	public int getValue() {
		return value;
	}

	@Override
	public ValueType typeCheck(TypeContext env) {
		return Util.intType();
	}

	@Override
	public <T> T acceptVisitor(ASTVisitor <T> emitILVisitor,
			Environment env, OIREnvironment oirenv) {
		return emitILVisitor.visit(env, oirenv, this);
	}
	
	@Override
	public Set<String> getFreeVariables() {
		return new HashSet<>();
	}


	@Override
	public ValueType getType() {
		return Util.intType();
	}

	@Override
	public Value invoke(String methodName, List<Value> args) {
		switch (methodName) {
		case "+": return new IntegerLiteral(this.value + ((IntegerLiteral)args.get(0)).getValue());
		case "-": return new IntegerLiteral(this.value - ((IntegerLiteral)args.get(0)).getValue());
		case "*": return new IntegerLiteral(this.value * ((IntegerLiteral)args.get(0)).getValue());
		case "/": return new IntegerLiteral(this.value / ((IntegerLiteral)args.get(0)).getValue());
		case "<": return new BooleanLiteral(this.value < ((IntegerLiteral)args.get(0)).getValue());
		case ">": return new BooleanLiteral(this.value > ((IntegerLiteral)args.get(0)).getValue());
		default: throw new RuntimeException("runtime error: integer operation " + methodName + "not supported by the runtime");
		}
	}

	@Override
	public Value getField(String fieldName) {
		throw new RuntimeException("no fields");
	}
}
