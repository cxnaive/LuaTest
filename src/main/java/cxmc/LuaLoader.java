package cxmc;

import java.util.HashMap;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaLoader {
    private LuaScript instance;
    HashMap<String,Globals> StoredScripts;
    HashMap<String,HashMap<String,Object>> StoredVars;
    public LuaLoader(LuaScript instance){
        this.StoredScripts = new HashMap<>();
    }
    public HashMap<String,Object> LoadVarsFromH2(ScriptPos pos){
        HashMap<String,Object> now = instance.h2Manager.GetPosVars(pos);
        if(now != null){
            StoredVars.put(pos.toString(), now);
        }
        return null;
    }
    public HashMap<String,Object> LoadVarsFromH2(String AreaID){
        HashMap<String,Object> now = instance.h2Manager.GetAreaVars(AreaID);
        if(now != null){
            StoredVars.put(AreaID, now);
        }
        return null;
    }
    public Globals LoadScriptFromH2(ScriptPos pos){
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
    public Globals LoadScriptFromH2(String AreaID){
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
    public Globals GetScript(ScriptPos pos){
        Globals stored = StoredScripts.get(pos.toString());
        if(stored == null) stored = LoadScriptFromH2(pos);
        return stored;
    }
    public Globals GetScript(String AreaID){
        Globals stored = StoredScripts.get(AreaID);
        if(stored == null) stored = LoadScriptFromH2(AreaID);
        return stored;
    }
    public HashMap<String,Object> GetVars(String AreaID){
        HashMap<String,Object> now = StoredVars.get(AreaID);
        if(now == null) now = LoadVarsFromH2(AreaID);
        return now;
    }
    public HashMap<String,Object> GetVars(ScriptPos pos){
        HashMap<String,Object> now = StoredVars.get(pos.toString());
        if(now == null) now = LoadVarsFromH2(pos);
        return now;
    }
    public Pair<ScriptPos,ScriptPos> GetAABB(String AreaID){
        return instance.h2Manager.GetAreaAABB(AreaID);
    }
    public void SetScript(String ScriptID,String Content){
        if(instance.h2Manager.HasScript(ScriptID)){
            instance.h2Manager.UpdateScript(ScriptID, Content);
        }
        else instance.h2Manager.PutScript(ScriptID, Content);
    }
    public void SetPos(ScriptPos pos,String ScriptID,HashMap<String,Object> values){
        if(instance.h2Manager.HasPos(pos)){
            instance.h2Manager.UpdatePos(pos, ScriptID, values);
        }
        else instance.h2Manager.PutPos(pos, ScriptID, values);
    }
    public void SetArea(String AreaID,String ScriptID,HashMap<String,Object> values){
        if(instance.h2Manager.HasArea(AreaID)){
            instance.h2Manager.UpdateArea(AreaID, ScriptID, values);
        }
    }
    public void SetArea(ScriptPos pos1,ScriptPos pos2,String AreaID,String ScriptID,HashMap<String,Object> values){
        if(instance.h2Manager.HasArea(AreaID)){
            instance.h2Manager.DeleteArea(AreaID);
        }
        instance.h2Manager.PutArea(pos1, pos2, AreaID, ScriptID, values);
    }
    public void refresh(){
        StoredScripts.clear();
    }
}