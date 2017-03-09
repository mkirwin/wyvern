package wyvern.target.corewyvernIL.support;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import wyvern.target.corewyvernIL.expression.Expression;
import wyvern.target.corewyvernIL.expression.Let;
import wyvern.target.corewyvernIL.expression.New;
import wyvern.target.corewyvernIL.expression.Variable;
import wyvern.target.corewyvernIL.type.StructuralType;
import wyvern.target.corewyvernIL.type.ValueType;

public class GenUtil {

	public static final String javaTypesObjectName = "java$types";
	private static final Variable javaTypesObject = new Variable(javaTypesObjectName);
	private static ValueType javaTypes = null;

	/**
	 * Linking of single modules </br>
	 *
	 *  for a simple module:
	 * 	It is a value declaration, simply add the value into the context </br>
	 *
	 *  for a resource module:
	 * 	It is a method declaration, wrap it into an object, add the object into the context </br>
	 *
	 *  for a type declaration:
	 * 	It is a type declaration, add a new variable that has the same name as the type into the context </br>
	 *
	 * @param genCtx origin context
	 * @param decl the IL Declaration generated by top level generation
	 * @return new context
	 */
	public static GenContext link(GenContext genCtx, wyvern.target.corewyvernIL.decl.Declaration decl) {
		if(decl instanceof wyvern.target.corewyvernIL.decl.ValDeclaration) {
			wyvern.target.corewyvernIL.decl.ValDeclaration vd = (wyvern.target.corewyvernIL.decl.ValDeclaration) decl;
			return genCtx.extend(vd.getName(), new Variable(vd.getName())/*vd.getDefinition()*/, vd.getType()); // manually adding instead of linking
		} else if (decl instanceof wyvern.target.corewyvernIL.decl.TypeDeclaration) {
			wyvern.target.corewyvernIL.decl.TypeDeclaration td = (wyvern.target.corewyvernIL.decl.TypeDeclaration) decl;
			return genCtx.extend(td.getName(), new Variable(td.getName()), (ValueType) td.getSourceType()); // manually adding instead of linking
		} else if (decl instanceof wyvern.target.corewyvernIL.decl.DefDeclaration) {
			wyvern.target.corewyvernIL.decl.Declaration methodDecl = decl;
			List<wyvern.target.corewyvernIL.decl.Declaration> decls =
					new LinkedList<wyvern.target.corewyvernIL.decl.Declaration>();
			List<wyvern.target.corewyvernIL.decltype.DeclType> declts =
					new LinkedList<wyvern.target.corewyvernIL.decltype.DeclType>();
			decls.add(methodDecl);
			declts.add(methodDecl.typeCheck(genCtx, genCtx));
			ValueType type = new StructuralType(decl.getName(), declts);

			/* manually wrap the method into an object*/
			Expression newExp = new New(decls, decl.getName(), type, decl.getLocation());

			return genCtx.extend(decl.getName(), newExp, type); // adding the object into the environment, instead of linking
		}

		return genCtx;
	}

	public static Expression genExp(List<wyvern.target.corewyvernIL.decl.Declaration> decls, GenContext genCtx) {
		Iterator<wyvern.target.corewyvernIL.decl.Declaration> ai = decls.iterator();

		if (!ai.hasNext()) {
			throw new RuntimeException("expected an expression in the list");
		}

		return GenUtil.genExpByIterator(genCtx, ai);
	}

	private static Expression genExpByIterator(GenContext genCtx, Iterator<wyvern.target.corewyvernIL.decl.Declaration> ai) {
		if (ai.hasNext()) {
			wyvern.target.corewyvernIL.decl.Declaration decl = ai.next();
			if(decl instanceof wyvern.target.corewyvernIL.decl.ValDeclaration) {
				wyvern.target.corewyvernIL.decl.ValDeclaration vd = (wyvern.target.corewyvernIL.decl.ValDeclaration) decl;
				return new Let(vd.getName(), vd.getType(), vd.getDefinition(), genExpByIterator(genCtx, ai));
			} else if (decl instanceof wyvern.target.corewyvernIL.decl.TypeDeclaration) {
				//return new Let(td.getName(), new Variable(td.getName()), genExpByIterator(genCtx, ai)); // manually adding instead of linking
				return genExpByIterator(genCtx, ai);
			} else if (decl instanceof wyvern.target.corewyvernIL.decl.DefDeclaration) {
				wyvern.target.corewyvernIL.decl.Declaration methodDecl = decl;
				List<wyvern.target.corewyvernIL.decl.Declaration> decls =
						new LinkedList<wyvern.target.corewyvernIL.decl.Declaration>();
				List<wyvern.target.corewyvernIL.decltype.DeclType> declts =
						new LinkedList<wyvern.target.corewyvernIL.decltype.DeclType>();
				decls.add(methodDecl);
				declts.add(methodDecl.typeCheck(genCtx, genCtx));
				ValueType type = new StructuralType(decl.getName(), declts);

				/* manually wrap the method into an object*/
				Expression newExp = new New(decls, decl.getName(), type, decl.getLocation());
				if(!ai.hasNext()) {
					//return newExp;
					return new Let(decl.getName(), type, newExp, new wyvern.target.corewyvernIL.expression.MethodCall(new Variable("main"), "main", new LinkedList<Expression>(), decl));
				} else {
					return new Let(decl.getName(), type, newExp, genExpByIterator(genCtx, ai));
				}
			}
		} else {
			// Cannot happen
			return null;
		}
		return null;
	}

	public static ValueType javaClassToWyvernType(Class<?> javaClass, TypeContext ctx) {
		//return javaClassToWyvernTypeRec(javaClass, new HashSet<String>());
		// TODO: extend to types other than int, and structural types based on that
		if (javaClass.getName().equals("int")) {
			return Util.intType();
		}

		if (javaClass.getName().equals("java.math.BigInteger")) {
			return Util.intType();
		}

		if (javaClass.getName().equals("java.lang.String")) {
			return Util.stringType();
		}

		if (javaClass.getName().equals("boolean")) {
			return Util.booleanType();
		}

		// TODO: might be unnecessary
		if (javaClass.getName().equals("wyvern.target.corewyvernIL.expression.ObjectValue")) {
			return Util.dynType();
		}

		// TODO: might be unnecessary
		if (javaClass.getName().equals("java.lang.Object")) {
			return Util.dynType();
		}

		if (javaClass.getName().equals("java.util.List")) {
			return Util.listType();
		}

		StructuralTypesFromJava type = (StructuralTypesFromJava) ctx.lookupTypeOf(javaTypesObjectName);
		return type.getJavaType(javaClass, ctx);
	}

	public static Variable getJavaTypesObject() {
		return javaTypesObject;
	}

	public static GenContext ensureJavaTypesPresent(GenContext ctx) {
		if (ctx.isPresent(javaTypesObjectName, true)) {
			return ctx;
		}
		// we just reuse the Java structural types object
		// no harm in this provided we aren't loading multiple versions of the same Java class
		if (javaTypes == null) {
			javaTypes = new StructuralTypesFromJava();
		}
		return ctx.extend(javaTypesObjectName, javaTypesObject, javaTypes);
	}
}
