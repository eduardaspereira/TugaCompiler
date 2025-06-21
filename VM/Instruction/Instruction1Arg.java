package VM.Instruction;

import VM.OpCode;
import java.io.DataOutputStream;
import java.io.IOException;

public class Instruction1Arg extends Instruction {
    private int arg;

    public Instruction1Arg(OpCode opc, int arg) {
        super(opc);
        setArg(arg);
    }

    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }

    @Override
    public String toString() {
        return opc.toString() + " " + arg;
    }

    @Override
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(opc.ordinal());
        out.writeInt(arg);
    }
}