package tools;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class SymbolTools {

    public static void nameSubstitute(MethodDeclaration declaration) {
        ModifierVisitor<Void> visitor = new SymbolVisitor();
        visitor.visit(declaration, null);
    }

}
