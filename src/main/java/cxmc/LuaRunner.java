package cxmc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

public class LuaRunner {
    ExecutorService Pool;
    private Map<String,DaemonScript> RunningScripts;
    public synchronized void PutRunningScripts(String key,DaemonScript value){
        RunningScripts.put(key, value);
    }
    public synchronized void RemoveRunningScripts(String key){
        RunningScripts.remove(key);
    }
    class DaemonScript implements Runnable{
        String funcName,runID;
        Globals script;
        LuaValue[] args;
        public DaemonScript(Globals script,String runID,String funcName,LuaValue[] args){
            this.funcName = funcName;
            this.runID = runID;
            this.script = script;
            this.args = args;
        }
        @Override
        public void run() {
            if (this.script.debuglib instanceof LuaDebugLib){
                LuaDebugLib nowDebugLib = (LuaDebugLib)this.script.debuglib;
                nowDebugLib.interrupted = false;
                LuaValue runFunc = this.script.get(LuaValue.valueOf(this.funcName));
                PutRunningScripts(runID, this);
                try{
                    runFunc.invoke(args);
                } catch(Exception ex){
                    System.out.println("killed "+runID+"!");
                }
                RemoveRunningScripts(runID);
            }
            else{
                System.out.println("Error loaded script!.");
            }
        }
        public void forcestop(){
            if (this.script.debuglib instanceof LuaDebugLib){
                LuaDebugLib nowDebugLib = (LuaDebugLib)this.script.debuglib;
                nowDebugLib.interrupted = true;
            }
            else{
                System.out.println("Error loaded script!.");
            }
        }
    }

    public LuaRunner(){
        this.Pool = Executors.newCachedThreadPool();
        this.RunningScripts = new HashMap<>();
    }
    
    public synchronized void runFunc(Globals script,String runID,String funcName,LuaValue[] args){
        DaemonScript now = new DaemonScript(script,runID,funcName, args);
        Pool.execute(now);
    }
    public synchronized void kill(String runID){
        DaemonScript now = RunningScripts.get(runID);
        if(now != null){
            now.forcestop();
        }
    }
    public synchronized void close(){
        Pool.shutdown();
    }
    public synchronized Set<String> RunningIDs(){
        return RunningScripts.keySet();
    }
}