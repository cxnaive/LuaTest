package cxmc;


import java.util.HashMap;
import java.util.Scanner;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
class LuaScript{
    public LuaLoader lualoader;
    public LuaRunner runner;
    public FileLoader fileLoader;
    public H2Manager h2Manager;
    public void init(){
        lualoader = new LuaLoader(this);
        fileLoader = new FileLoader("script");
        runner = new LuaRunner();
        h2Manager = new H2Manager("./H2/H2", "luascript", "luascript");
        h2Manager.TryConnect();
    }
    public void test(){
        Scanner sc = new Scanner(System.in);
        while(true){
            String op = sc.next();
            op = op.toLowerCase();
            if(op.equals("load")){
                String ScriptID = sc.next();
                String filename = sc.next();
                String Content = fileLoader.ReadScript(filename);
                lualoader.SetScript(ScriptID, Content);
            }
            if(op.equals("bindpos")){
                int x = sc.nextInt(),y = sc.nextInt(),z = sc.nextInt();
                ScriptPos nowpos = new ScriptPos(x, y, z);
                String ScriptID = sc.next();
                lualoader.SetPos(nowpos, ScriptID, new HashMap<>());
            }
            if(op.equals("runpos")){
                int x = sc.nextInt(),y = sc.nextInt(),z = sc.nextInt();
                String funcID = sc.next();
                ScriptPos nowpos = new ScriptPos(x, y, z);
                Globals script = lualoader.GetScript(nowpos);
                HashMap<String,Object> now = lualoader.GetVars(nowpos);
                LuaValue[] args = {CoerceJavaToLua.coerce(now)};
                runner.runFunc(script, nowpos.toString(), funcID, args);
            }
            if(op.equals("close")){
                break;
            }
        }
        sc.close();
    }
    public void end(){
        lualoader.close();
        runner.close();
        h2Manager.CloseConnect();
    }
}

public class LuaTest{
    public static void main( String[] args ){
        LuaScript test = new LuaScript();
        test.init();
        test.test();
        test.end();
        // Globals globals = JsePlatform.standardGlobals();
        // globals.loadfile("script/test.lua").call();
        // LuaValue func = globals.get(LuaValue.valueOf("Run"));
        // func.call(CoerceJavaToLua.coerce(new Hashtable<String,Object>()));
    }
}