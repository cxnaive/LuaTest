package cxmc;

public class ScriptPos {
    public int x,y,z;
    ScriptPos(int x,int y,int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public String toString(){
        return "P:"+this.x+","+this.y+","+this.z;
    }
    @Override
    public final int hashCode(){
        int hashcode = 17;
        hashcode = hashcode * 31 + x;
        hashcode = hashcode * 31 + y;
        hashcode = hashcode * 31 + z;
        return hashcode;
    }
    @Override
    public boolean equals(Object object){
        if(this == object) return true;
        if(object instanceof ScriptPos){
            ScriptPos other = (ScriptPos)object;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
        else return false;
    }
    public static ScriptPos BuildFromStr(String str){
        String[] vars = str.substring(2).split(",");
        return new ScriptPos(Integer.parseInt(vars[0]),Integer.parseInt(vars[1]),Integer.parseInt(vars[2]));
    }
}