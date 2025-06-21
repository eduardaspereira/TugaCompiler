package TypeChecker;

import Tuga.TugaBaseVisitor;
import Tuga.TugaParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeChecker extends TugaBaseVisitor<String> {
    private Map<String, String> variables = new HashMap<>();
    private Map<String, Integer> globalIndices = new HashMap<>();
    private int nextGlobalIndex = 0;
    private List<String> errors = new ArrayList<>();

    public void declareVariable(String name, String type, int line) {
        if (variables.containsKey(name)) {
            errors.add("erro na linha " + line + ": variavel '" + name + "' ja foi declarada");
        } else {
            variables.put(name, type);
            globalIndices.put(name, nextGlobalIndex++);
        }
    }

    public String getType(String name, int line) {
        if (!variables.containsKey(name)) {
            errors.add("erro na linha " + line + ": variavel '" + name + "' nao declarada");
        }
        return variables.get(name);
    }

    public int getGlobalIndex(String name) {
        return globalIndices.getOrDefault(name, -1);
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String visitVarDecl(TugaParser.VarDeclContext ctx) {
        String type = ctx.type().getText();
        int line = ctx.getStart().getLine();
        for (var id : ctx.ID()) {
            declareVariable(id.getText(), type, line);
        }
        return null;
    }

    @Override
    public String visitAssignStmt(TugaParser.AssignStmtContext ctx) {
        String varName = ctx.ID().getText();
        int line = ctx.getStart().getLine();
        String varType = getType(varName, line);
        String exprType = visit(ctx.expr());
        if (!varType.equals("unknown") && !isCompatible(varType, exprType)) {
            errors.add("erro na linha " + line +
                    ": operador '<-' eh invalido entre " + varType + " e " + exprType);
        }
        return null;
    }

    @Override
    public String visitIfStmt(TugaParser.IfStmtContext ctx) {
        String exprType = visit(ctx.expr());
        int line = ctx.getStart().getLine();
        if (!exprType.equals("booleano")) {
            errors.add("erro na linha " + line +
                    ": expressao de 'se' nao eh do tipo booleano");
        }
        visit(ctx.stmt(0));
        if (ctx.stmt(1) != null) {
            visit(ctx.stmt(1));
        }
        return null;
    }

    @Override
    public String visitWhileStmt(TugaParser.WhileStmtContext ctx) {
        String exprType = visit(ctx.expr());
        int line = ctx.getStart().getLine();
        if (!exprType.equals("booleano")) {
            errors.add("erro na linha " + line +
                    ": expressao de 'enquanto' nao eh do tipo booleano");
        }
        visit(ctx.stmt());
        return null;
    }

    @Override
    public String visitVarExpr(TugaParser.VarExprContext ctx) {
        return getType(ctx.ID().getText(), ctx.getStart().getLine());
    }

    @Override
    public String visitNumberExpr(TugaParser.NumberExprContext ctx) {
        return ctx.getText().contains(".") ? "real" : "inteiro";
    }

    @Override
    public String visitStringExpr(TugaParser.StringExprContext ctx) {
        return "string";
    }

    @Override
    public String visitTrueExpr(TugaParser.TrueExprContext ctx) {
        return "booleano";
    }

    @Override
    public String visitFalseExpr(TugaParser.FalseExprContext ctx) {
        return "booleano";
    }

    @Override
    public String visitAddExpr(TugaParser.AddExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if (left.equals("string") || right.equals("string")) {
            return "string";
        }
        if (left.equals("real") || right.equals("real")) {
            return "real";
        }
        if (left.equals("inteiro") && right.equals("inteiro")) {
            return "inteiro";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitSubExpr(TugaParser.SubExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if (left.equals("real") || right.equals("real")) {
            return "real";
        }
        if (left.equals("inteiro") && right.equals("inteiro")) {
            return "inteiro";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitMulExpr(TugaParser.MulExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if (left.equals("real") || right.equals("real")) {
            return "real";
        }
        if (left.equals("inteiro") && right.equals("inteiro")) {
            return "inteiro";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitDivExpr(TugaParser.DivExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if (left.equals("real") || right.equals("real")) {
            return "real";
        }
        if (left.equals("inteiro") && right.equals("inteiro")) {
            return "inteiro";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitModExpr(TugaParser.ModExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if (left.equals("inteiro") && right.equals("inteiro")) {
            return "inteiro";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitEqExpr(TugaParser.EqExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();

        if (isCompatible(left, right) || isCompatible(right, left)) {
            return "booleano";
        }
        errors.add("erro na linha " + line +
                ": tipos incompativeis em comparacao de igualdade");
        return "unknown";
    }
    @Override
    public String visitAndExpr(TugaParser.AndExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();

        if (!left.equals("booleano") || !right.equals("booleano")) {
            errors.add("erro na linha " + line +
                    ": operador 'e' requer operandos booleanos");
            return "unknown";
        }
        return "booleano";
    }

    @Override
    public String visitOrExpr(TugaParser.OrExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();

        if (!left.equals("booleano") || !right.equals("booleano")) {
            errors.add("erro na linha " + line +
                    ": operador 'ou' requer operandos booleanos");
            return "unknown";
        }
        return "booleano";
    }

    @Override
    public String visitLtExpr(TugaParser.LtExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if ((left.equals("inteiro") || left.equals("real")) && (right.equals("inteiro") || right.equals("real"))) {
            return "booleano";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    @Override
    public String visitLeqExpr(TugaParser.LeqExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        int line = ctx.getStart().getLine();
        if ((left.equals("inteiro") || left.equals("real")) && (right.equals("inteiro") || right.equals("real"))) {
            return "booleano";
        }
        errors.add("erro na linha " + line + ": tipos incompativeis em expressao");
        return "unknown";
    }

    private boolean isCompatible(String varType, String exprType) {
        if (varType.equals(exprType)) {
            return true;
        }
        if (varType.equals("real") && exprType.equals("inteiro")) {
            return true;
        }
        return false;
    }

    @Override
    public String visitNegExpr(TugaParser.NegExprContext ctx) {
        String exprType = visit(ctx.expr());
        int line = ctx.getStart().getLine();
        if (exprType.equals("inteiro") || exprType.equals("real")) {
            return exprType;
        }
        errors.add("erro na linha " + line + ": operador unario '-' nao pode ser aplicado a tipo " + exprType);
        return "unknown";
    }


}