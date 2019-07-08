/*	EvalConstExp.java
 
	Copyright 2019 Andrew Hooper
	
	This file is part of the P10 Compiler.
	
	The P10 Compiler is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
// Not currently used

package ca.nevdull.p10a.compiler;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class EvalConstExp extends P10aBaseVisitor<Integer> {

	private String getChildText(ParseTree node, int index) {
		return ((TerminalNode)node.getChild(index)).getSymbol().getText();
	}

	@Override
	public Integer visitAddExpr(P10aParser.AddExprContext ctx) {
		// expression ( '+' | '-' ) NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}
	
	static Integer Zero = new Integer(0);
	static Integer One = new Integer(1);

	private Integer dyadic(ParseTree oper, List<P10aParser.ExpressionContext> exprs) {
		assert exprs.size() == 2;
		P10aParser.ExpressionContext expa = exprs.get(0);
    	P10aParser.ExpressionContext expb = exprs.get(1);
    	Integer vala = visit(expa);
    	if (vala == null) return null;
    	Integer valb = visit(expb);
    	if (valb == null) return null;
		String opText = ((TerminalNode)oper).getSymbol().getText();
		switch (opText.charAt(0)) {
		case '*':
	    	return new Integer(vala.intValue() * valb.intValue());
		case '/':
	    	return new Integer(vala.intValue() / valb.intValue());
		case '%':
	    	return new Integer(vala.intValue() % valb.intValue());
		case '+':
	    	return new Integer(vala.intValue() + valb.intValue());
		case '-':
	    	return new Integer(vala.intValue() - valb.intValue());
		case '&':
	    	return new Integer(vala.intValue() & vala.intValue());
		case '^':
	    	return new Integer(vala.intValue() ^ vala.intValue());
		case '|':
	    	return new Integer(vala.intValue() | valb.intValue());
		case '<':
			if (opText.length() > 1) return (vala.intValue() <= valb.intValue()) ? One : Zero;
	    	return (vala.intValue() < valb.intValue()) ? One : Zero;
		case '>':
			if (opText.length() > 1) return (vala.intValue() >= valb.intValue()) ? One : Zero;
	    	return (vala.intValue() > valb.intValue()) ? One : Zero;
		case '=':
	    	return (vala.intValue() == valb.intValue()) ? One : Zero;
		case '!':
			if (opText.length() > 1) return (vala.intValue() != valb.intValue()) ? One : Zero;
	    	return (vala.intValue() < valb.intValue()) ? One : Zero;
		default:
			return null;
		}
	}

	private Integer monadic(ParseTree oper, P10aParser.ExpressionContext exp) {
    	Integer val = visit(exp);
    	if (val == null) return null;
		switch (((TerminalNode)oper).getSymbol().getText().charAt(0)) {
		case '~':
	    	return new Integer(~ val.intValue());
		case '-':
	    	return new Integer(- val.intValue());
		default:
			return null;
		}
	}

	@Override
	public Integer visitBitAndExpr(P10aParser.BitAndExprContext ctx) {
		// expression '&' NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}

	@Override
	public Integer visitBitExclExpr(P10aParser.BitExclExprContext ctx) {
		// expression '^' NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}

	@Override
	public Integer visitNotExpr(P10aParser.NotExprContext ctx) {
		// ( '~' | '!' ) NL* expression
		return monadic(ctx.getChild(0),ctx.expression());
	}

	@Override
	public Integer visitBitOrExpr(P10aParser.BitOrExprContext ctx) {
		// expression '|' NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}

	@Override
	public Integer visitMinusExpr(P10aParser.MinusExprContext ctx) {
		// '-' NL* expression
		return monadic(ctx.getChild(0),ctx.expression());
	}

	@Override
	public Integer visitMultiplyExpr(P10aParser.MultiplyExprContext ctx) {
		// expression ( '*' | '/' | '%' ) NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}

	@Override
	public Integer visitCompareExpr(P10aParser.CompareExprContext ctx) {
		// expression ( '=' | '!=' | '<=' | '>=' | '<' | '>' ) NL* expression
		return dyadic(ctx.getChild(1),ctx.expression());
	}
	
	@Override
	public Integer visitOrElseExpr(P10aParser.OrElseExprContext ctx) {
		// TODO Auto-generated method stub
		return super.visitOrElseExpr(ctx);
	}
		// Identifier '(' NL* parameterList? ')'			# callExpr
		// '!' NL* expression								# notExpr
		// expression '&&' NL* expression					# andThenExpr
		// expression '||' NL* expression					# orElseExpr
		// reference										# referExpr
		// Float											# floatExpr
		// String											# stringExpr
		// 'null'											# nullExpr

	@Override
	public Integer visitAndThenExpr(P10aParser.AndThenExprContext ctx) {
		// TODO Auto-generated method stub
		return super.visitAndThenExpr(ctx);
	}

	@Override
	public Integer visitReferExpr(P10aParser.ReferExprContext ctx) {
		// TODO Auto-generated method stub
		return super.visitReferExpr(ctx);
	}

	@Override
	public Integer visitCallExpr(P10aParser.CallExprContext ctx) {
		// TODO Auto-generated method stub
		return super.visitCallExpr(ctx);
	}

	@Override
	public Integer visitIdRefer(P10aParser.IdReferContext ctx) {
		// TODO Auto-generated method stub
		return super.visitIdRefer(ctx);
	}

	@Override
	public Integer visitIndexRefer(P10aParser.IndexReferContext ctx) {
		// TODO Auto-generated method stub
		return super.visitIndexRefer(ctx);
	}

	@Override
	public Integer visitParenPrim(P10aParser.ParenPrimContext ctx) {
		// '(' NL* expression ')'
		P10aParser.ExpressionContext exp = ctx.expression();
		if (exp == null) return null;
		return visit(exp);
	}

	@Override
	public Integer visitIntegerPrim(P10aParser.IntegerPrimContext ctx) {
    	// Integer
    	return Integer.decode(ctx.Integer().getText());
	}

	@Override
	public Integer visitFloatPrim(P10aParser.FloatPrimContext ctx) {
		// TODO Auto-generated method stub
		return super.visitFloatPrim(ctx);
	}

	@Override
	public Integer visitCharacterPrim(P10aParser.CharacterPrimContext ctx) {
		// Character
		return new Integer(ctx.Character().getText().codePointAt(1));
	}

	@Override
	public Integer visitStringPrim(P10aParser.StringPrimContext ctx) {
		// TODO Auto-generated method stub
		return super.visitStringPrim(ctx);
	}

	@Override
	public Integer visitNullPrim(P10aParser.NullPrimContext ctx) {
		// TODO Auto-generated method stub
		return super.visitNullPrim(ctx);
	}

	@Override
	public Integer visitFalsePrim(P10aParser.FalsePrimContext ctx) {
		return Zero;
	}

	@Override
	public Integer visitTruePrim(P10aParser.TruePrimContext ctx) {
		return One;
	}

}
