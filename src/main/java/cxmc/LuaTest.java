package cxmc;


import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
class LuaScript{
    public LuaLoader lualoader;
    public LuaRunner luarunner;
    public FileLoader fileLoader;
    public H2Manager h2Manager;
    public void init(){
        lualoader = new LuaLoader(this);
        fileLoader = new FileLoader("script");
        luarunner = new LuaRunner();
        h2Manager = new H2Manager("./H2/H2", "luascript", "luascript");
        h2Manager.TryConnect();
    }
    public void test(){
        Scanner sc = new Scanner(System.in);
        while(true){
            String op = sc.next().toLowerCase();
            if(op.equals("load")){
                String ScriptID = sc.next();
                String filename = sc.next();
                String Content = fileLoader.ReadScript(filename);
                boolean result = lualoader.SetScript(ScriptID, Content);
                if(!result) System.out.println("loadscript failed!");
            }
            else if(op.equals("bind")){
                String op1 = sc.next();
                if(op1.equals("pos")){
                    int x = sc.nextInt(),y = sc.nextInt(),z = sc.nextInt();
                    ScriptPos nowpos = new ScriptPos(x, y, z);
                    String ScriptID = sc.next();
                    boolean result =  lualoader.SetPos(nowpos, ScriptID, new HashMap<>());
                    if(!result) System.out.println("bind pos failed!");
                }
                else if(op1.equals("area")){
                    String AreaID = sc.next();
                    String ScriptID = sc.next();
                    boolean result =  lualoader.SetArea(new ScriptPos(0, 0, 0),new ScriptPos(0, 0, 0),AreaID , ScriptID, new HashMap<>());
                    if(!result) System.out.println("bind area failed!");
                }
            }
            else if(op.equals("run")){
                String op1 = sc.next();
                Globals script;
                if(op1.equals("pos")){
                    int x = sc.nextInt(),y = sc.nextInt(),z = sc.nextInt();
                    String funcID = sc.next();
                    ScriptPos nowpos = new ScriptPos(x, y, z);
                    script = lualoader.GetScript(nowpos);
                    if(script == null){
                        System.out.println("reading pos script failed!");
                        continue;
                    }
                    HashMap<String,Object> now = lualoader.GetVars(nowpos);
                    if(now == null){
                        System.out.println("reading pos vars failed!");
                        continue;
                    }
                    LuaValue[] args = {CoerceJavaToLua.coerce(now)};
                    luarunner.runFunc(script, nowpos.toString(), funcID, args);
                }
                else if(op1.equals("area")){
                    String AreaID = sc.next();
                    String funcID = sc.next();
                    script = lualoader.GetScript(AreaID);
                    if(script == null){
                        System.out.println("reading area script failed!");
                        continue;
                    }
                    HashMap<String,Object> now = lualoader.GetVars(AreaID);
                    if(now == null){
                        System.out.println("reading area vars failed!");
                        continue;
                    }
                    LuaValue[] args = {CoerceJavaToLua.coerce(now)};
                    luarunner.runFunc(script, AreaID, funcID, args);
                }
            }
            else if(op.equals("show")){
                String op1 = sc.next().toLowerCase();
                if(op1.equals("allpos")){
                    List<Pair<ScriptPos,String>> now = lualoader.GetPosALL();
                    for(Pair<ScriptPos,String> posv:now){
                        System.out.println(posv.getKey().toString()+":"+posv.getValue());
                    }
                }
                else if(op1.equals("pos")){
                    String SID = sc.next();
                    List<ScriptPos> now = lualoader.GetPosBySID(SID);
                    for(ScriptPos posv:now){
                        System.out.println(posv.toString());
                    }
                }
                else if(op1.equals("allscript")){
                    List<String> now = lualoader.GetScriptIDALL();
                    for(String sid:now){
                        System.out.println(sid);
                    }
                }
                else if(op1.equals("allarea")){
                    List<Pair<String,String>> now = lualoader.GetAreaALL();
                    for(Pair<String,String> areav:now){
                        System.out.println(areav.getKey().toString()+":"+areav.getValue());
                    }
                }
                else if(op1.equals("area")){
                    String SID = sc.next();
                    List<String> now = lualoader.GetAreaBySID(SID);
                    for(String v:now){
                        System.out.println(v);
                    }
                }
                else if(op1.equals("content")){
                    String ScriptID = sc.next();
                    String content = lualoader.GetScriptContent(ScriptID);
                    System.out.println(content);
                }
            }
            else if(op.equals("showrun")){
                Set<String> ids = luarunner.RunningIDs();
                for(String id:ids){
                    System.out.println(id);
                }
            }
            else if(op.equals("kill")){
                String id = sc.next();
                luarunner.kill(id);
            }
            else if(op.equals("close")){
                break;
            }
        }
        sc.close();
    }
    public void end(){
        lualoader.close();
        luarunner.close();
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