package cxmc;

public class ScriptValues {
    public int amount,cooldown,delay;
    public long last_executed_time;
    public ScriptValues(int amount,int cooldown,int delay){
        this.amount = amount;
        this.cooldown = cooldown;
        this.delay = delay;
    }
}