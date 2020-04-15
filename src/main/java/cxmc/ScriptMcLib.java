package cxmc;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ScriptMcLib extends TwoArgFunction{
    public static String libname = "scriptmc";
    @Override
    public LuaValue call(LuaValue modname,LuaValue env){
        LuaValue library = tableOf();
        library.set("time", new TimeFunc());
        library.set("sleep",new SleepFunc());
        library.set("servermsg",new ServerMsgFunc());
        env.set(libname,library);
        env.get("package").get("loaded").set(libname,library);
        return library;
    }
    
    public class TimeFunc extends ZeroArgFunction{
        @Override
        public LuaValue call(){
            return LuaValue.valueOf(System.currentTimeMillis());
        }
    }

    public class SleepFunc extends OneArgFunction{
        @Override
        public LuaValue call(LuaValue arg){
            try{
                Thread.sleep(arg.checklong());
                return LuaValue.valueOf("success");
            } catch (Exception ex){
                return LuaValue.valueOf(ex.getMessage());                                  
            }
        }
    }

    public class ServerMsgFunc extends OneArgFunction{
        @Override
        public LuaValue call(LuaValue arg){
            System.out.println("Server:"+arg.checkstring());
            return null;
        }
    }
}