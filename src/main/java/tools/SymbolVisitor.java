package tools;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.util.HashMap;
import java.util.Map;

public class SymbolVisitor extends ModifierVisitor<Void> {

    private Map<String, Integer> numberMap = new HashMap<>();
    private Map<String, String> nameMap = new HashMap<>();

    public VariableDeclarator visit(VariableDeclarator declarator, Void arg) {
        String type = declarator.getType().asString();
        int num = 1;
        if (numberMap.containsKey(type)) {
            num = numberMap.get(type);
            num ++;
            numberMap.put(type, num);
        } else {
            numberMap.put(type, 1);
        }
        String newName = type.toLowerCase() + num;
        nameMap.put(declarator.getName().asString(), newName);
        super.visit(declarator, arg);
        return declarator;
    }

    public SimpleName visit(SimpleName name, Void arg) {
        if (nameMap.containsKey(name.asString())) {
            name.setIdentifier(nameMap.get(name.asString()));
        }
        super.visit(name, arg);
        return name;
    }
}
