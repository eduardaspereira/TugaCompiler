import Tuga.TugaLexer;
import Tuga.TugaParser;
import CodeGenerator.CodeGenerator;
import TypeChecker.TypeChecker;
import VM.VM;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.*;
import java.util.List;

public class TugaCompileAndRun {
    private static boolean showLexerErrors = false;
    private static boolean showParserErrors = false;

    public static void main(String[] args) throws Exception {
        InputStream input = System.in;
        if (args.length > 0) {
            input = new FileInputStream(args[0]);
            for (String arg : args) {
                if (arg.equals("--show-lexer-errors")) showLexerErrors = true;
                if (arg.equals("--show-parser-errors")) showParserErrors = true;
            }
        }

        CharStream charStream = CharStreams.fromStream(input);
        TugaLexer lexer = new TugaLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                if (showLexerErrors) {
                    System.err.println("Erro léxico na linha " + line + ": " + msg);
                } else {
                    System.out.println("Input tem erros lexicais");
                    System.exit(0);
                }
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TugaParser parser = new TugaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                if (showParserErrors) {
                    System.err.println("Erro de parsing na linha " + line + ": " + msg);
                } else {
                    System.out.println("Input tem erros de parsing");
                    System.exit(0);
                }
            }
        });

        ParseTree tree = parser.program();

        TypeChecker typeChecker = new TypeChecker();
        typeChecker.visit(tree);
        if (typeChecker.hasErrors()) {
            for (String error : typeChecker.getErrors()) {
                System.out.println(error);
            }
            System.exit(0);
        }

        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.initialize(tree);
        codeGenerator.visit(tree);
        System.out.println("*** Constant pool ***");
        List<Object> constantPool = codeGenerator.getConstantPool();
        for (int i = 0; i < constantPool.size(); i++) {
            Object constant = constantPool.get(i);
            if (constant instanceof String) {
                System.out.println(i + ": \"" + constant + "\"");
            } else {
                System.out.println(i + ": " + constant);
            }
        }
        codeGenerator.dumpCode();

        // Save bytecodes
        codeGenerator.saveBytecodes("bytecodes.bc");

        // Execute virtual machine
        System.out.println("*** VM output ***");
        VM vm = new VM(codeGenerator.getConstantPool(), codeGenerator.getCode());
        vm.execute();
    }
}