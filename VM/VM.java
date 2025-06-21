package VM;

import VM.Instruction.Instruction;
import VM.Instruction.Instruction1Arg;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VM {
    private List<Object> constantPool;
    private List<Object> globals;
    private Stack<Object> stack;
    private List<Instruction> code;
    private int ip;

    public VM(List<Object> constantPool, List<Instruction> code) {
        this.constantPool = constantPool;
        this.code = code;
        this.globals = new ArrayList<>();
        this.stack = new Stack<>();
        this.ip = 0;
    }

    public void execute() {
        while (ip < code.size()) {
            Instruction instr = code.get(ip);
            OpCode opc = instr.getOpCode();
            int arg = instr instanceof Instruction1Arg ? ((Instruction1Arg) instr).getArg() : 0;

            try {
                switch (opc) {
                    case galloc:
                        for (int i = 0; i < arg; i++) {
                            globals.add(null);
                        }
                        break;
                    case gstore:
                        globals.set(arg, stack.pop());
                        break;
                    case gload:
                        Object value = globals.get(arg);
                        if (value == null) {
                            throw new RuntimeException("erro de runtime: tentativa de acesso a valor NULO");
                        }
                        stack.push(value);
                        break;
                    case iconst:
                        stack.push(arg);
                        break;
                    case dconst:
                        stack.push(constantPool.get(arg));
                        break;
                    case sconst:
                        stack.push(constantPool.get(arg));
                        break;
                    case tconst:
                        stack.push(true);
                        break;
                    case fconst:
                        stack.push(false);
                        break;
                    case iprint:
                        System.out.println((Integer) stack.pop());
                        break;
                    case dprint:
                        System.out.println((Double) stack.pop());
                        break;
                    case sprint:
                        System.out.println((String) stack.pop());
                        break;
                    case bprint:
                        boolean b = (Boolean) stack.pop();
                        System.out.println(b ? "verdadeiro" : "falso");
                        break;
                    case iuminus:
                        int val = (Integer) stack.pop();
                        stack.push(-val);
                        break;
                    case iadd:
                        int ib = (Integer) stack.pop();
                        int ia = (Integer) stack.pop();
                        stack.push(ia + ib);
                        break;
                    case isub:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia - ib);
                        break;
                    case imult:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia * ib);
                        break;
                    case idiv:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia / ib);
                        break;
                    case imod:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia % ib);
                        break;
                    case ieq:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia == ib);
                        break;
                    case ilt:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia < ib);
                        break;
                    case ileq:
                        ib = (Integer) stack.pop();
                        ia = (Integer) stack.pop();
                        stack.push(ia <= ib);
                        break;
                    case itod:
                        int i = (Integer) stack.pop();
                        stack.push((double) i);
                        break;
                    case itos:
                        i = (Integer) stack.pop();
                        stack.push(String.valueOf(i));
                        break;
                    case dadd:
                        double db = (Double) stack.pop();
                        double da = (Double) stack.pop();
                        stack.push(da + db);
                        break;
                    case dsub:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da - db);
                        break;
                    case dmult:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da * db);
                        break;
                    case ddiv:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da / db);
                        break;
                    case deq:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da == db);
                        break;
                    case dlt:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da < db);
                        break;
                    case dleq:
                        db = (Double) stack.pop();
                        da = (Double) stack.pop();
                        stack.push(da <= db);
                        break;
                    case dtos:
                        double d = (Double) stack.pop();
                        stack.push(String.valueOf(d));
                        break;
                    case sconcat:
                        String sb = (String) stack.pop();
                        String sa = (String) stack.pop();
                        stack.push(sa + sb);
                        break;
                    case seq:
                        sb = (String) stack.pop();
                        sa = (String) stack.pop();
                        stack.push(sa.equals(sb));
                        break;
                    case sneq:
                        sb = (String) stack.pop();
                        sa = (String) stack.pop();
                        stack.push(!sa.equals(sb));
                        break;
                    case beq:
                        boolean bb = (Boolean) stack.pop();
                        boolean ba = (Boolean) stack.pop();
                        stack.push(ba == bb);
                        break;
                    case bneq:
                        bb = (Boolean) stack.pop();
                        ba = (Boolean) stack.pop();
                        stack.push(ba != bb);
                        break;
                    case and:
                        bb = (Boolean) stack.pop();
                        ba = (Boolean) stack.pop();
                        stack.push(ba && bb);
                        break;
                    case or:
                        bb = (Boolean) stack.pop();
                        ba = (Boolean) stack.pop();
                        stack.push(ba || bb);
                        break;
                    case not:
                        b = (Boolean) stack.pop();
                        stack.push(!b);
                        break;
                    case btos:
                        b = (Boolean) stack.pop();
                        stack.push(String.valueOf(b));
                        break;
                    case jump:
                        ip = arg;
                        continue;
                    case jumpf:
                        if (!(Boolean) stack.pop()) {
                            ip = arg;
                            continue;
                        }
                        break;
                    case halt:
                        return;
                }
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }
            ip++;
        }
    }
}