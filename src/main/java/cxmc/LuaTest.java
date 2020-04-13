package cxmc;


import java.util.Scanner;

import org.luaj.vm2.LuaValue;
//import org.luaj.vm2.lib.jse.CoerceJavaToLua;
class LuaScript{
    public LuaLoader lualoader;
    public LuaRunner runner;
    public FileLoader fileLoader;
    public H2Manager h2Manager;
    public void init(){
        lualoader = new LuaLoader(this);
        fileLoader = new FileLoader("script");
        runner = new LuaRunner();
        h2Manager = new H2Manager("./H2", "luascript", "luascript");
        h2Manager.TryConnect();
    }
    public void test(){
        LuaValue[] fargs = {};
        Scanner sc = new Scanner(System.in);
        sc.close();
    }
    public void end(){
        h2Manager.CloseConnect();
        runner.close();
    }
}
public class LuaTest{
    public static void main( String[] args ){
        LuaScript test = new LuaScript();
        test.init();
        test.test();
        test.end();
    }
}