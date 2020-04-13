package cxmc;

import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

public class LuaDebugLib extends DebugLib{
    public volatile boolean interrupted = false;

    @Override
    public void onInstruction(int pc, Varargs v, int top) {
        if (interrupted) {
            throw new ScriptInterruptException();
        }
        super.onInstruction(pc, v, top);
    }

    public static class ScriptInterruptException extends RuntimeException {
        private static final long serialVersionUID = -1L;
    }
}