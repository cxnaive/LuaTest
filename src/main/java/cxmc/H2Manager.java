package cxmc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
/*
ID---script ID
AMT---amount
CLD---cooldown
LET---last executed time
DLY---delay execute
CONTENT---real lua script content
SID---script id 
*/
public class H2Manager {
    private static final String DRIVER_CLASS = "org.h2.Driver";
    
    private static final String POS_STRING = "POS";
    private static final String POS_STRUCT = "(X INT,Y INT,Z INT,SID varchar(255),AMT INT,CLD INT,LET BIGINT,DLY INT)";
    private static final String POS_COLUMNS = "(X1,Y1,Z1,SID,AMT,CLD,LET,DLY)";

    private static final String AREA_STRING = "AREA";
    private static final String AREA_STRUCT = "(X1 INT,Y1 INT,Z1 INT,X2 INT,Y2 INT,Z2 INT,ID varchar(255),SID varchar(255),AMT INT,CLD INT,LET BIGINT,DLY INT)";
    private static final String AREA_COLUMNS = "(X1,Y1,Z1,X2,Y2,Z2,ID,SID,AMT,CLD,LET,DLY)";

    private static final String SCRIPT_STRING = "SCRIPT";
    private static final String SCRIPT_STRUCT = "(SID varchar(255),CONTENT MEDIUMTEXT)";
    private static final String SCRIPT_COLUMNS = "(SID,CONTENT)";
    
    private PreparedStatement PUT_SCRIPT,PUT_POS,PUT_AREA;
    private PreparedStatement HAS_POS,HAS_AREA,HAS_SCRIPT;
    private PreparedStatement GET_POS_SCRIPT,GET_AREA_SCRIPT;
    private String USER;
    private String PASSWORD;
    private String PATH;
    Connection conn;
    public H2Manager(String path,String username,String password){
        this.PATH = path;
        this.PASSWORD = password;
        this.USER = username;
    }
    public boolean TryConnect(){
        String JDBC_URL = "jdbc:h2:"+this.PATH+";AUTO_RECONNECT=TRUE";
        try{
            Class.forName(DRIVER_CLASS);
            conn = DriverManager.getConnection(JDBC_URL, this.USER, this.PASSWORD);
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS "+POS_STRING+POS_STRUCT);
            statement.execute("CREATE TABLE IF NOT EXISTS "+AREA_STRING+AREA_STRUCT);
            statement.execute("CREATE TABLE IF NOT EXISTS "+SCRIPT_STRING+SCRIPT_STRUCT);
            statement.close();
            PUT_SCRIPT = conn.prepareStatement("INSERT INTO "+SCRIPT_STRING+SCRIPT_COLUMNS+" VALUES(?,?)");
            PUT_POS = conn.prepareStatement("INSERT INTO "+POS_STRING+POS_COLUMNS+" VALUES(?,?,?,?,?,?,?,?)");
            PUT_AREA = conn.prepareStatement("INSERT INTO "+AREA_STRING+AREA_COLUMNS+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            HAS_POS = conn.prepareStatement("SELECT COUNT(*) FROM "+POS_STRING+" WHERE X = ? and Y = ? and Z = ? ");
            HAS_AREA = conn.prepareStatement("SELECT COUNT(*) FROM "+AREA_STRING+" WHERE ID = ?");
            HAS_SCRIPT = conn.prepareStatement("SELECT COUNT(*) FROM "+SCRIPT_STRING+" WHERE SID = ?");
            GET_POS_SCRIPT = conn.prepareStatement("SELECT CONTENT FROM "+SCRIPT_STRING+" WHERE SID IN (SELECT SID FROM "+POS_STRING+" WHERE X = ? and Y = ? and Z = ?)");
            GET_AREA_SCRIPT = conn.prepareStatement("SELECT CONTENT FROM "+SCRIPT_STRING+" WHERE SID IN (SELECT SID FROM "+AREA_STRING+" WHERE ID = ?)");
            return true;
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
    public void CloseConnect(){
        try{
            PUT_SCRIPT.close();
            PUT_POS.close();
            PUT_AREA.close();
            HAS_POS.close();
            HAS_AREA.close();
            GET_POS_SCRIPT.close();
            GET_AREA_SCRIPT.close();
            conn.close();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    public boolean HasPos(ScriptPos pos){
        try{
            HAS_POS.setInt(1, pos.x);
            HAS_POS.setInt(2, pos.y);
            HAS_POS.setInt(3, pos.z);
            ResultSet result = HAS_POS.executeQuery();
            result.next();
            int cnt = result.getInt(0);
            return cnt == 1;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public boolean HasArea(String AreaID){
        try{
            HAS_AREA.setString(1, AreaID);
            ResultSet result = HAS_AREA.executeQuery();
            result.next();
            int cnt = result.getInt(0);
            return cnt == 1;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public String GetPosScript(ScriptPos pos){
        try{
            GET_POS_SCRIPT.setInt(1, pos.x);
            GET_POS_SCRIPT.setInt(2, pos.y);
            GET_POS_SCRIPT.setInt(3, pos.z);
            ResultSet result = GET_POS_SCRIPT.executeQuery();
            result.next();
            String now = result.getString(0);
            return now;
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public String GetAreaScript(String AreaID){
        try{
            GET_AREA_SCRIPT.setString(1, AreaID);
            ResultSet result = GET_AREA_SCRIPT.executeQuery();
            result.next();
            String now = result.getString(0);
            return now;
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public boolean HasScript(String ScriptID){
        try{
            HAS_AREA.setString(1, ScriptID);
            ResultSet result = HAS_SCRIPT.executeQuery();
            result.next();
            int cnt = result.getInt(0);
            return cnt == 1;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public boolean PutScript(String ScriptID,String Content){
        try{
            PUT_SCRIPT.setString(1, ScriptID);
            PUT_SCRIPT.setString(2, Content);
            PUT_SCRIPT.executeUpdate();
            return true;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public boolean PutPos(ScriptPos pos,String ScriptID,ScriptValues values){
        try{
            PUT_POS.setInt(1, pos.x);
            PUT_POS.setInt(2, pos.y);
            PUT_POS.setInt(3, pos.z);
            PUT_POS.setString(4, ScriptID);
            PUT_POS.setInt(5, values.amount);
            PUT_POS.setInt(6, values.cooldown);
            PUT_POS.setLong(7, values.last_executed_time);
            PUT_POS.setInt(8, values.delay);
            PUT_POS.executeUpdate();
            return true;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public boolean PutArea(ScriptPos pos1,ScriptPos pos2,String AreaID,String ScriptID,ScriptValues values){
        try{
            PUT_AREA.setInt(1, pos1.x);
            PUT_AREA.setInt(2, pos1.y);
            PUT_AREA.setInt(3, pos1.z);
            PUT_AREA.setInt(4, pos2.x);
            PUT_AREA.setInt(5, pos2.y);
            PUT_AREA.setInt(6, pos2.z);
            PUT_AREA.setString(7, AreaID);
            PUT_POS.setString(8, ScriptID);
            PUT_POS.setInt(9, values.amount);
            PUT_POS.setInt(10, values.cooldown);
            PUT_POS.setLong(11, values.last_executed_time);
            PUT_POS.setInt(12, values.delay);
            PUT_POS.executeUpdate();
            return true;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}