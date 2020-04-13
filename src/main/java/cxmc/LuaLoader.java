package cxmc;

import java.util.HashMap;
import java.util.Map;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaLoader {
    private LuaScript instance;
    Map<String,Globals> StoredScripts;
    public LuaLoader(LuaScript instance){
        this.StoredScripts = new HashMap<>();
    }
    public Globals LoadFromH2(ScriptPos pos){
        Globals script = JsePlatform.standardGlobals();
        try{
            String luastr = instance.h2Manager.GetPosScript(pos);
            script.load(new LuaDebugLib());
            script.load(luastr,"@"+pos.toString(),script).call();
            StoredScripts.put(pos.toString(), script);
            return script;
        } catch(LuaError e){
            e.printStackTrace();
            return null;
        }
    }
    public Globals LoadFromH2(String AreaID){
        Globals script = JsePlatform.standardGlobals();
        try{
            String luastr = instance.h2Manager.GetAreaScript(AreaID);
            script.load(new LuaDebugLib());
            script.load(luastr,"@"+AreaID,script).call();
            StoredScripts.put(AreaID, script);
            return script;
        } catch(LuaError e){
            e.printStackTrace();
            return null;
        }
    }
    public Globals LoadScript(ScriptPos pos){
        Globals stored = StoredScripts.get(pos.toString());
        if(stored == null) stored = LoadFromH2(pos);
        return stored;
    }
    public Globals LoadScript(String AreaID){
        Globals stored = StoredScripts.get(AreaID);
        if(stored == null) stored = LoadFromH2(AreaID);
        return stored;
    }
    public void refresh(){
        StoredScripts.clear();
    }
}