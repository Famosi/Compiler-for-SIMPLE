package models;
import util.Node;
import util.OperationCodeGeneration;
import util.Params;
import util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleStmtFunctionDeclaration extends SimpleStmt {

    List<SimpleParameter> parameters;
    String id;
    SimpleStmtBlock block;

    public SimpleStmtFunctionDeclaration(String id, List<SimpleParameter> parameters, SimpleStmtBlock block){
        this.id = id;
        this.parameters = parameters;
        this.block = block;

    }

    @Override
    public List<SemanticError> checkSemantics(EnvironmentVariables ev, EnvironmentFunctions ef) {

        List<SemanticError> semanticErrors = new LinkedList<>();

        List<Params> params = new LinkedList<>();

        if(ef.containsFunction(id) == false){
            ef.addFunction(id, new Params(parameters));
        } else {
            semanticErrors.add(new SemanticError(Strings.FunctionAlreadyDeclared + id));
        }

        ev.openScope();

        for (SimpleParameter parameter: parameters) {
            if(ev.containsVariable(parameter.getID()) == false){
                ev.addVariable(parameter.getID(), parameter.getType());
            } else {
                semanticErrors.add(new SemanticError(Strings.VariablesAlreadyDeclared + parameter.getID()));
            }
        }

        semanticErrors.addAll(block.checkSemanticsFunction(ev, ef));


        return semanticErrors;
    }

    @Override
    public List<Node> codeGeneration(EnvironmentVariablesWithOffset ev, EnvironmentFunctionsWithLabel ef, OperationCodeGeneration oCgen) {
        List<Node> codeDeclaration = new LinkedList<>();

        List<String> variablesDeclared = visitBlockAndGetDeclaration(block, ev);

        String endFunctionLabel = oCgen.getFreshLabel();
        codeDeclaration.add(oCgen.b(endFunctionLabel));

        ev.openScope();
        oCgen.increaseNestingLevel();

        List<String> paramsList = new LinkedList<>();

        for (SimpleParameter param: parameters){
            paramsList.add(param.getID());
        }

        paramsList.addAll(variablesDeclared);

        for (String param: paramsList){
            ev.varDeclaration(param);
        }

        String freshLabel = ef.newFunctionDeclaration(id, variablesDeclared);
        codeDeclaration.add(oCgen.label(freshLabel));
        codeDeclaration.add(oCgen.move("fp", "sp"));

        codeDeclaration.addAll(oCgen.push("ra"));

        ef.openScope();

        codeDeclaration.addAll(block.codeGenerationDeclaration(ev, ef, oCgen));

        ef.closeScope();

        codeDeclaration.add(oCgen.top("ra"));
        codeDeclaration.add(oCgen.pop());

        codeDeclaration.add(oCgen.top("fp"));
        codeDeclaration.add(oCgen.pop());

        for (int i = 0; i < paramsList.size(); i++) {
            codeDeclaration.add(oCgen.addi("sp", "sp", "1"));
        }

        codeDeclaration.add(oCgen.jr("ra"));

        codeDeclaration.add(oCgen.label(endFunctionLabel));


        oCgen.decreaseNestingLevel();
        ev.closeScope();

        return codeDeclaration;
    }

    private List<String> visitBlockAndGetDeclaration(SimpleStmtBlock block, EnvironmentVariablesWithOffset ev) {
        List<String> variablesDeclared = new ArrayList<>();

        for (SimpleStmt children : block.children) {
            if (children.getClass() == SimpleStmtDeclaration.class){
                SimpleStmtDeclaration declaration = (SimpleStmtDeclaration) children;
                if (declaration.getType() != null){
                    variablesDeclared.add(declaration.getID());
                    ev.varDeclaration(declaration.getID());
                }
            }
        }

        return variablesDeclared;
    }
}
