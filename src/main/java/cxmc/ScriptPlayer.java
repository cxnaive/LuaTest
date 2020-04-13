package cxmc;

public class ScriptPlayer {
    public String name;
    ScriptPlayer(String name){
        this.name = name;
    }
    public void runCMD(String cmd){
        System.out.println("#Java runCMD:"+cmd);
    }
}