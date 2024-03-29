package models;

import util.Node;
import util.OperationCodeGeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleStmtBlock extends SimpleStmt {

	List<SimpleStmt> children;
	private String function;

	/**
	 * Creates a new block
	 * @param children: the list of direct children elements of the block
	 */
	SimpleStmtBlock(List<SimpleStmt> children) {
		this.children = children;
	}

	public List<SemanticError> checkSemantics(EnvironmentVariables e, EnvironmentFunctions f) {
		//create scope for inner elements
		e.openScope();
		f.openScope();

		//initialize result variable
		LinkedList<SemanticError> result = new LinkedList<>();

		//check children semantics
		if(children!=null)
			for(SimpleStmt el:children)
				result.addAll(el.checkSemantics(e, f));

		//close scope for this block

		f.closeScope();
		e.closeScope();

		return result;
	}

	List<SemanticError> checkSemanticsFunction(EnvironmentVariables e, EnvironmentFunctions f) {

		//initialize result variable
		LinkedList<SemanticError> result = new LinkedList<>();

		//create scope for inner elements

		f.openScope();

		//check children semantics
		if(children!=null)
			for(SimpleStmt el:children)
				result.addAll(el.checkSemantics(e, f));

		//close scope for this block

		f.closeScope();
		e.closeScope();

		return result;
	}

	@Override
	public List<Node> codeGeneration(EnvironmentVariablesWithOffset ev, EnvironmentFunctionsWithLabel ef, OperationCodeGeneration oCgen) {
		List<Node> codeBlock = new ArrayList<>();

		List<String> variablesDeclared;

		ev.openScope();
		ef.openScope();

		oCgen.increaseNestingLevel();

		variablesDeclared = visitBlockAndGetDeclaration(children, ev);

		for(int i = variablesDeclared.size()-1; i >= 0; i--){
			codeBlock.add(oCgen.addi("sp", "sp", "-1"));
		}

		codeBlock.addAll(oCgen.push("fp"));
		codeBlock.add(oCgen.move("fp", "sp"));

		//list for saving children statements
		List<Node> statementCode = new ArrayList<>();

		//visit each children
		if(children!=null)
			for(SimpleStmt el:children)
				statementCode.addAll(el.codeGeneration(ev, ef, oCgen));

		codeBlock.addAll(statementCode);

		ef.closeScope();
		ev.closeScope();
		oCgen.decreaseNestingLevel();

		codeBlock.add(oCgen.top("fp"));
		codeBlock.add(oCgen.pop());

		for (String ignored : variablesDeclared) {
			codeBlock.add(oCgen.pop());
		}

		return codeBlock;
	}


	List<Node> codeGenerationDeclaration(EnvironmentVariablesWithOffset ev, EnvironmentFunctionsWithLabel ef, OperationCodeGeneration oCgen) {
		List<Node> codeBlock = new ArrayList<>();

		//list for saving children statements
		List<Node> statementCode = new ArrayList<>();

		//visit each children
		if(children!=null)
			for(SimpleStmt el:children)
				statementCode.addAll(el.codeGeneration(ev, ef, oCgen));

		codeBlock.addAll(statementCode);

		return codeBlock;
	}

	private List<String> visitBlockAndGetDeclaration(List<SimpleStmt> children, EnvironmentVariablesWithOffset ev) {
		List<String> variablesDeclared = new ArrayList<>();

		for (SimpleStmt child : children) {
			if (child.getClass() == SimpleStmtDeclaration.class){
				SimpleStmtDeclaration declaration = (SimpleStmtDeclaration) child;
				if (declaration.getType() != null){
					variablesDeclared.add(declaration.getID());
					ev.varDeclaration(declaration.getID());
				}
			}
		}

		return variablesDeclared;
	}

	public void setFunction(String id){ this.function = id; }

	public String getFunctionID() { return function; }

}
