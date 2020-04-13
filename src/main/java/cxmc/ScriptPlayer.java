package cxmc;

import java.util.HashMap;

public class ScriptPlayer {
    public String name;
    HashMap<String,Object> map;
    ScriptPlayer(String name){
        this.name = name;
        this.map = new HashMap<>();
    }
    public void runCMD(String cmd){
        System.out.println("#Java runCMD:"+cmd);
    }
    public void set(String key,Object value){
        map.put(key,value);
    }
    public Object get(String key){
        return map.get(key);
    }
}