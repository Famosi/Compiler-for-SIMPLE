package models;

import com.sun.org.apache.xpath.internal.operations.Bool;
import parser.SimpleParser;

import java.util.LinkedList;
import java.util.List;

public class SimpleParameter extends SimpleElementBase {

    Boolean var;
    String id;
    String type;

    public SimpleParameter(String id, String type, Boolean var){
        this.var = var;
        this.id = id;
        this.type = type;
    }

    @Override
    public List<SemanticError> checkSemantics(EnvironmentVariables ev, EnvironmentFunctions ef) {

        List<SemanticError> semanticErrors = new LinkedList<>();

        return semanticErrors;
    }

    public String getType(){ return type;}

    public String getID(){ return id; }

}
