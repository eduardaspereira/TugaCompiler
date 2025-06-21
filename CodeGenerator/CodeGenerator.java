package CodeGenerator;

import Tuga.TugaBaseVisitor;
import Tuga.TugaParser;
import TypeChecker.TypeChecker;
import VM.OpCode;
import VM.Instruction.Instruction;
import VM.Instruction.Instruction1Arg;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
import java.util.*;

public class CodeGenerator extends TugaBaseVisitor<Void> {
    private TypeChecker typeChecker = new TypeChecker();
    private List<Instruction> code = new ArrayList<>();
    private List<Object> constantPool = new ArrayList<>();
    private Map<Object, Integer> constantIndices = new HashMap<>();
    private int labelCounter = 0;
    private Map<Integer, Integer> labelToInstruction = new HashMap<>();
    private List<Integer> jumpFixups = new ArrayList<>();

    public void initialize(ParseTree tree) {
        typeChecker.visit(tree);
    }

    @Override
    public Void visitProgram(TugaParser.ProgramContext ctx) {
        if (typeChecker.hasErrors()) {
        }
        for (var varDecl : ctx.varDecl()) {
            visit(varDecl);
        }
        for (var stmt : ctx.stmt()) {
            visit(stmt);
        }
        emit(OpCode.halt);
        fixJumps();
        return null;
    }

    @Override
    public Void visitVarDecl(TugaParser.VarDeclContext ctx) {
        int varCount = ctx.ID().size();
        if (varCount > 0) {
            emit(OpCode.galloc, varCount);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(TugaParser.PrintStmtContext ctx) {
        visit(ctx.expr());
        String type = typeChecker.visit(ctx.expr());
        switch (type) {
            case "inteiro":
                emit(OpCode.iprint);
                break;
            case "real":
                emit(OpCode.dprint);
                break;
            case "string":
                emit(OpCode.sprint);
                break;
            case "booleano":
                emit(OpCode.bprint);
                break;
        }
        return null;
    }

    @Override
    public Void visitAssignStmt(TugaParser.AssignStmtContext ctx) {
        visit(ctx.expr());
        String varName = ctx.ID().getText();
        int globalIndex = typeChecker.getGlobalIndex(varName);
        emit(OpCode.gstore, globalIndex);
        return null;
    }

    @Override
    public Void visitBlockStmt(TugaParser.BlockStmtContext ctx) {
        for (var stmt : ctx.stmt()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(TugaParser.WhileStmtContext ctx) {
        int startLabel = labelCounter++;
        int endLabel = labelCounter++;
        emitLabel(startLabel);
        visit(ctx.expr());
        emit(OpCode.jumpf, endLabel);
        jumpFixups.add(code.size() - 1);
        visit(ctx.stmt());
        emit(OpCode.jump, startLabel);
        jumpFixups.add(code.size() - 1);
        emitLabel(endLabel);
        return null;
    }

    @Override
    public Void visitIfStmt(TugaParser.IfStmtContext ctx) {
        int elseLabel = labelCounter++;
        int endLabel = ctx.stmt(1) != null ? labelCounter++ : elseLabel;
        visit(ctx.expr());
        emit(OpCode.jumpf, elseLabel);
        jumpFixups.add(code.size() - 1);
        visit(ctx.stmt(0));
        if (ctx.stmt(1) != null) {
            emit(OpCode.jump, endLabel);
            jumpFixups.add(code.size() - 1);
            emitLabel(elseLabel);
            visit(ctx.stmt(1));
            emitLabel(endLabel);
        } else {
            emitLabel(elseLabel);
        }
        return null;
    }

    @Override
    public Void visitNegExpr(TugaParser.NegExprContext ctx) {
        visit(ctx.expr());
        String exprType = typeChecker.visit(ctx.expr());
        emit(OpCode.iuminus);
        return null;
    }

    @Override
    public Void visitNumberExpr(TugaParser.NumberExprContext ctx) {
        String text = ctx.getText();
        if (text.startsWith("-")) {
            if (text.contains(".")) {
                double value = Double.parseDouble(text);
                if (!constantIndices.containsKey(value)) {
                    constantIndices.put(value, constantPool.size());
                    constantPool.add(value);
                }
                emit(OpCode.dconst, constantIndices.get(value));
            } else {
                emit(OpCode.iconst, Math.abs(Integer.parseInt(text)));
                emit(OpCode.iuminus);
            }
        } else {
            if (text.contains(".")) {
                double value = Double.parseDouble(text);
                if (!constantIndices.containsKey(value)) {
                    constantIndices.put(value, constantPool.size());
                    constantPool.add(value);
                }
                emit(OpCode.dconst, constantIndices.get(value));
            } else {
                emit(OpCode.iconst, Integer.parseInt(text));
            }
        }
        return null;
    }

    @Override
    public Void visitStringExpr(TugaParser.StringExprContext ctx) {
        String text = ctx.getText();
        String value = text.substring(1, text.length() - 1);
        if (!constantIndices.containsKey(value)) {
            constantIndices.put(value, constantPool.size());
            constantPool.add(value);
        }
        emit(OpCode.sconst, constantIndices.get(value));
        return null;
    }

    @Override
    public Void visitTrueExpr(TugaParser.TrueExprContext ctx) {
        emit(OpCode.tconst);
        return null;
    }

    @Override
    public Void visitFalseExpr(TugaParser.FalseExprContext ctx) {
        emit(OpCode.fconst);
        return null;
    }

    @Override
    public Void visitVarExpr(TugaParser.VarExprContext ctx) {
        String varName = ctx.ID().getText();
        int globalIndex = typeChecker.getGlobalIndex(varName);
        emit(OpCode.gload, globalIndex);
        return null;
    }

    @Override
    public Void visitAddExpr(TugaParser.AddExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        if (leftType.equals("inteiro") && typeChecker.visit(ctx.expr(1)).equals("string")) {
            emit(OpCode.itos);
        }
        visit(ctx.expr(1));
        if (leftType.equals("string") && typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
            emit(OpCode.itos);
        }
        if (leftType.equals("string") || typeChecker.visit(ctx.expr(1)).equals("string")) {
            emit(OpCode.sconcat);
        } else if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.dadd);
        } else {
            emit(OpCode.iadd);
        }
        return null;
    }

    @Override
    public Void visitSubExpr(TugaParser.SubExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.dsub);
        } else {
            emit(OpCode.isub);
        }
        return null;
    }

    @Override
    public Void visitMulExpr(TugaParser.MulExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.dmult);
        } else {
            emit(OpCode.imult);
        }
        return null;
    }

    @Override
    public Void visitDivExpr(TugaParser.DivExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.ddiv);
        } else {
            emit(OpCode.idiv);
        }
        return null;
    }

    @Override
    public Void visitModExpr(TugaParser.ModExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCode.imod);
        return null;
    }

    @Override
    public Void visitEqExpr(TugaParser.EqExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        String rightType = typeChecker.visit(ctx.expr(1));

        if (leftType.equals("real") || rightType.equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (rightType.equals("inteiro")) emit(OpCode.itod);
            emit(OpCode.deq);
        } else if (leftType.equals("string")) {
            emit(OpCode.seq);
        } else if (leftType.equals("booleano")) {
            emit(OpCode.beq);
        } else {
            emit(OpCode.ieq);
        }
        return null;
    }
    @Override
    public Void visitAndExpr(TugaParser.AndExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCode.and);
        return null;
    }

    @Override
    public Void visitOrExpr(TugaParser.OrExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCode.or);
        return null;
    }

    @Override
    public Void visitLtExpr(TugaParser.LtExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.dlt);
        } else {
            emit(OpCode.ilt);
        }
        return null;
    }

    @Override
    public Void visitLeqExpr(TugaParser.LeqExprContext ctx) {
        visit(ctx.expr(0));
        String leftType = typeChecker.visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (leftType.equals("real") || typeChecker.visit(ctx.expr(1)).equals("real")) {
            if (leftType.equals("inteiro")) emit(OpCode.itod);
            if (typeChecker.visit(ctx.expr(1)).equals("inteiro")) {
                emit(OpCode.itod);
            }
            emit(OpCode.dleq);
        } else {
            emit(OpCode.ileq);
        }
        return null;
    }


    private void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    private void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    private void emitLabel(int label) {
        labelToInstruction.put(label, code.size());
    }

    private void fixJumps() {
        for (int i : jumpFixups) {
            Instruction1Arg instr = (Instruction1Arg) code.get(i);
            int label = instr.getArg();
            int target = labelToInstruction.getOrDefault(label, code.size());
            instr.setArg(target);
        }
    }

    public void dumpCode() {
        System.out.println("*** Instructions ***");
        for (int i = 0; i < code.size(); i++) {
            System.out.println(i + ": " + code.get(i));
        }
    }

    public void saveBytecodes(String filename) throws IOException {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            dout.writeInt(constantPool.size());
            for (Object constant : constantPool) {
                if (constant instanceof Double) {
                    dout.writeByte(0x01);
                    dout.writeDouble((Double) constant);
                } else if (constant instanceof String) {
                    dout.writeByte(0x03);
                    String s = (String) constant;
                    dout.writeInt(s.length());
                    for (char c : s.toCharArray()) {
                        dout.writeChar(c);
                    }
                }
            }
            for (Instruction inst : code) {
                inst.writeTo(dout);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Object> getConstantPool() {
        return constantPool;
    }

    public List<Instruction> getCode() {
        return code;
    }
}