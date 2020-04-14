package cxmc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*
SID---script ID
ID---area ID
VARS---variables
*/
public class H2Manager {
    private static final String DRIVER_CLASS = "org.h2.Driver";
    
    private static final String POS_STRING = "POS";
    private static final String POS_STRUCT = "(X INT,Y INT,Z INT,SID VARCHAR(255),VARS BLOB)";
    private static final String POS_COLUMNS = "(X,Y,Z,SID,VARS)";

    private static final String AREA_STRING = "AREA";
    private static final String AREA_STRUCT = "(X1 INT,Y1 INT,Z1 INT,X2 INT,Y2 INT,Z2 INT,ID VARCHAR(255),SID VARCHAR(255),VARS BLOB)";
    private static final String AREA_COLUMNS = "(X1,Y1,Z1,X2,Y2,Z2,ID,SID,VARS)";

    private static final String SCRIPT_STRING = "SCRIPT";
    private static final String SCRIPT_STRUCT = "(SID VARCHAR(255),CONTENT VARCHAR(65535))";
    private static final String SCRIPT_COLUMNS = "(SID,CONTENT)";
    
    private PreparedStatement PUT_SCRIPT,PUT_POS,PUT_AREA;
    private PreparedStatement HAS_POS,HAS_AREA,HAS_SID;
    private PreparedStatement GET_POS_SCRIPT,GET_POS_SID,GET_AREA_SID,GET_AREA_SCRIPT,
                            GET_POS_VARS,GET_AREA_VARS,GET_AREA_AABB,GET_POS_BY_SID,GET_AREA_BY_SID,
                            GET_POS_ALL,GET_AREA_ALL,GET_SID_ALL,GET_SCRIPT_BY_SID;
    private PreparedStatement UPD_SCRIPT,UPD_POS_VAR,UPD_AREA_VAR,UPD_POS,UPD_AREA;
    private PreparedStatement DEL_SCRIPT,DEL_POS,DEL_AREA;
    private final String USER;
    private final String PASSWORD;
    private final String PATH;
    Connection conn;

    public H2Manager(final String path, final String username, final String password) {
        this.PATH = path;
        this.PASSWORD = password;
        this.USER = username;
    }

    public boolean TryConnect() {
        final String JDBC_URL = "jdbc:h2:" + this.PATH + ";AUTO_RECONNECT=TRUE";
        try {
            Class.forName(DRIVER_CLASS);
            conn = DriverManager.getConnection(JDBC_URL, this.USER, this.PASSWORD);
            final Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS " + POS_STRING + POS_STRUCT);
            statement.execute("CREATE TABLE IF NOT EXISTS " + AREA_STRING + AREA_STRUCT);
            statement.execute("CREATE TABLE IF NOT EXISTS " + SCRIPT_STRING + SCRIPT_STRUCT);
            statement.close();
            PUT_SCRIPT = conn.prepareStatement("INSERT INTO " + SCRIPT_STRING + SCRIPT_COLUMNS + " VALUES(?,?)");
            PUT_POS = conn.prepareStatement("INSERT INTO " + POS_STRING + POS_COLUMNS + " VALUES(?,?,?,?,?)");
            PUT_AREA = conn
                    .prepareStatement("INSERT INTO " + AREA_STRING + AREA_COLUMNS + " VALUES(?,?,?,?,?,?,?,?,?)");

            HAS_POS = conn.prepareStatement("SELECT COUNT(*) FROM " + POS_STRING + " WHERE X = ? AND Y = ? AND Z = ? ");
            HAS_AREA = conn.prepareStatement("SELECT COUNT(*) FROM " + AREA_STRING + " WHERE ID = ?");
            HAS_SID = conn.prepareStatement("SELECT COUNT(*) FROM " + SCRIPT_STRING + " WHERE SID = ?");
            
            GET_POS_SCRIPT = conn.prepareStatement("SELECT CONTENT FROM " + SCRIPT_STRING 
                    + " WHERE SID IN (SELECT SID FROM " + POS_STRING + " WHERE X = ? AND Y = ? AND Z = ?)");
            GET_AREA_SCRIPT = conn.prepareStatement("SELECT CONTENT FROM " + SCRIPT_STRING
                    + " WHERE SID IN (SELECT SID FROM " + AREA_STRING + " WHERE ID = ?)");
            GET_POS_SID = conn.prepareStatement("SELECT SID FROM "+POS_STRING+" WHERE X = ? AND Y = ? AND Z = ?");
            GET_AREA_SID = conn.prepareStatement("SELECT SID FROM "+AREA_STRING+" WHERE ID = ?");
            GET_POS_VARS = conn.prepareStatement("SELECT VARS FROM "+POS_STRING+" WHERE X = ? AND Y = ? AND Z = ?");
            GET_AREA_VARS = conn.prepareStatement("SELECT VARS FROM "+AREA_STRING+" WHERE ID = ?");
            GET_AREA_AABB = conn.prepareStatement("SELECT X1,Y1,Z1,X2,Y2,Z2 FROM "+AREA_STRING+" WHERE ID = ?");
            GET_POS_BY_SID = conn.prepareStatement("SELECT X,Y,Z FROM "+POS_STRING+" WHERE SID = ?");
            GET_AREA_BY_SID = conn.prepareStatement("SELECT ID FROM "+AREA_STRING+" WHERE SID = ?");
            GET_POS_ALL = conn.prepareStatement("SELECT X,Y,Z,SID FROM "+POS_STRING);
            GET_AREA_ALL = conn.prepareStatement("SELECT ID,SID FROM "+AREA_STRING);
            GET_SID_ALL = conn.prepareStatement("SELECT SID FROM "+SCRIPT_STRING);
            GET_SCRIPT_BY_SID = conn.prepareStatement("SELECT CONTENT FROM "+SCRIPT_STRING+" WHERE SID = ?");

            UPD_SCRIPT = conn.prepareStatement("UPDATE " + SCRIPT_STRING + " SET CONTENT = ? WHERE SID = ?");
            UPD_POS_VAR = conn.prepareStatement(
                    "UPDATE " + POS_STRING + " SET VARS = ? WHERE X = ? AND Y = ? AND Z = ?");
            UPD_AREA_VAR = conn
                    .prepareStatement("UPDATE " + AREA_STRING + " SET VARS = ? WHERE ID = ?");
            UPD_POS = conn.prepareStatement("UPDATE "+ POS_STRING + " SET SID = ?,VARS = ? WHERE X = ? AND Y = ? AND Z = ?");
            UPD_AREA = conn.prepareStatement("UPDATE " + AREA_STRING + " SET SID = ?,VARS = ? WHERE ID = ?");
            
            DEL_SCRIPT = conn.prepareStatement("DELETE FROM "+POS_STRING+" WHERE SID = ?;"+"DELETE FROM "+AREA_STRING+" WHERE SID = ?;"+"DELETE FROM "+SCRIPT_STRING+" WHERE SID = ?");
            DEL_POS = conn.prepareStatement("DELETE FROM "+POS_STRING+" WHERE X = ? AND Y = ? AND Z = ?");
            DEL_AREA = conn.prepareStatement("DELETE FROM "+AREA_STRING+" WHERE ID = ?");
            
            return true;
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public void CloseConnect() {
        try {
            PUT_SCRIPT.close();
            PUT_POS.close();
            PUT_AREA.close();

            HAS_SID.close();
            HAS_POS.close();
            HAS_AREA.close();

            GET_POS_SCRIPT.close();
            GET_AREA_SCRIPT.close();
            GET_SCRIPT_BY_SID.close();
            GET_POS_SID.close();
            GET_AREA_SID.close();
            GET_POS_VARS.close();
            GET_AREA_VARS.close();
            GET_AREA_AABB.close();
            GET_POS_BY_SID.close();
            GET_AREA_BY_SID.close();
            GET_POS_ALL.close();
            GET_AREA_ALL.close();
            GET_SID_ALL.close();
            
            UPD_SCRIPT.close();
            UPD_POS_VAR.close();
            UPD_AREA_VAR.close();
            UPD_POS.close();
            UPD_AREA.close();

            DEL_SCRIPT.close();
            DEL_POS.close();
            DEL_AREA.close();
            
            conn.close();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean DeleteScript(final String ScriptID){
        try{
            DEL_SCRIPT.setString(1, ScriptID);
            DEL_SCRIPT.setString(2, ScriptID);
            DEL_SCRIPT.setString(3, ScriptID);
            DEL_SCRIPT.executeUpdate();
            return true;
        } catch (final Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean DeletePos(final ScriptPos pos){
        try {
            DEL_POS.setInt(1, pos.x);
            DEL_POS.setInt(2, pos.y);
            DEL_POS.setInt(3, pos.z);
            DEL_POS.executeUpdate();
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean DeleteArea(final String AreaID){
        try {
            DEL_AREA.setString(1, AreaID);
            DEL_AREA.executeUpdate();
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean HasScript(final String ScriptID) {
        try {
            HAS_SID.setString(1, ScriptID);
            final ResultSet result = HAS_SID.executeQuery();
            result.next();
            final int cnt = result.getInt(1);
            return cnt == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean HasPos(final ScriptPos pos) {
        try {
            HAS_POS.setInt(1, pos.x);
            HAS_POS.setInt(2, pos.y);
            HAS_POS.setInt(3, pos.z);
            final ResultSet result = HAS_POS.executeQuery();
            result.next();
            final int cnt = result.getInt(1);
            return cnt == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean HasArea(final String AreaID) {
        try {
            HAS_AREA.setString(1, AreaID);
            final ResultSet result = HAS_AREA.executeQuery();
            result.next();
            final int cnt = result.getInt(1);
            return cnt == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String GetPosScript(final ScriptPos pos) {
        try {
            GET_POS_SCRIPT.setInt(1, pos.x);
            GET_POS_SCRIPT.setInt(2, pos.y);
            GET_POS_SCRIPT.setInt(3, pos.z);
            final ResultSet result = GET_POS_SCRIPT.executeQuery();
            result.next();
            final String now = result.getString(1);
            return now;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String GetAreaScript(final String AreaID) {
        try {
            GET_AREA_SCRIPT.setString(1, AreaID);
            final ResultSet result = GET_AREA_SCRIPT.executeQuery();
            result.next();
            final String now = result.getString(1);
            return now;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private HashMap<String,Object> Blob2Map(Blob inBlob) throws Exception{
        InputStream is = inBlob.getBinaryStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buff = new byte[(int) inBlob.length()];
        bis.read(buff, 0, buff.length);
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buff));
        return (HashMap<String,Object>)in.readObject();
    }
    
    public HashMap<String,Object> GetPosVars(final ScriptPos pos) {
        try {
            GET_POS_VARS.setInt(1, pos.x);
            GET_POS_VARS.setInt(2, pos.y);
            GET_POS_VARS.setInt(3, pos.z);
            final ResultSet result = GET_POS_VARS.executeQuery();
            result.next();
            return Blob2Map(result.getBlob(1));
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public HashMap<String,Object> GetAreaVars(final String AreaID) {
        try {
            GET_AREA_VARS.setString(1, AreaID);
            final ResultSet result = GET_AREA_VARS.executeQuery();
            result.next();
            return Blob2Map(result.getBlob(1));
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Pair<ScriptPos,ScriptPos> GetAreaAABB(final String AreaID){
        try{
            GET_AREA_AABB.setString(1, AreaID);
            ResultSet result = GET_AREA_AABB.executeQuery();
            result.next();
            return new Pair<ScriptPos,ScriptPos>(new ScriptPos(result.getInt(1), result.getInt(2), result.getInt(3)),new ScriptPos(result.getInt(4), result.getInt(5), result.getInt(6)));
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    //public 

    public String GetPosSID(final ScriptPos pos){
        try{
            GET_POS_SID.setInt(1, pos.x);
            GET_POS_SID.setInt(2, pos.y);
            GET_POS_SID.setInt(3, pos.z);
            ResultSet result =  GET_POS_SID.executeQuery();
            result.next();
            return result.getString(1);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public String GetAreaSID(final String AreaID){
        try{
            GET_AREA_SID.setString(1, AreaID);
            ResultSet result =  GET_AREA_SID.executeQuery();
            result.next();
            return result.getString(1);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public List<ScriptPos> GetPosBySID(final String SID){
        try{
            GET_POS_BY_SID.setString(1, SID);
            ResultSet result = GET_POS_BY_SID.executeQuery();
            List<ScriptPos> now = new ArrayList<>();
            while(result.next()){
                now.add(new ScriptPos(result.getInt(1), result.getInt(2), result.getInt(3)));
            }
            return now;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> GetAreaBySID(final String SID){
        try{
            GET_AREA_BY_SID.setString(1, SID);
            ResultSet result = GET_AREA_BY_SID.executeQuery();
            List<String> now = new ArrayList<>();
            while(result.next()){
                now.add(result.getString(1));
            }
            return now;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public String GetScriptBySID(final String SID){
        try{
            GET_SCRIPT_BY_SID.setString(1, SID);
            ResultSet result = GET_SCRIPT_BY_SID.executeQuery();
            result.next();
            return result.getString(1);
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public List<ScriptPos> GetPosALL(){
        try{
            ResultSet result = GET_POS_ALL.executeQuery();
            List<ScriptPos> now = new ArrayList<>();
            while(result.next()){
                now.add(new ScriptPos(result.getInt(1), result.getInt(2), result.getInt(3)));
            }
            return now;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> GetAreaALL(){
        try{
            ResultSet result = GET_AREA_ALL.executeQuery();
            List<String> now = new ArrayList<>();
            while(result.next()){
                now.add(result.getString(1));
            }
            return now;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public List<String> GetSIDALL(){
        try{
            ResultSet result = GET_SID_ALL.executeQuery();
            List<String> now = new ArrayList<>();
            while(result.next()){
                now.add(result.getString(1));
            }
            return now;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public boolean UpdatePos(final ScriptPos pos,final String ScriptID, final HashMap<String,Object> values){
        try{
            UPD_POS.setString(1, ScriptID);
            UPD_POS.setObject(2, values);
            UPD_POS.setInt(3, pos.x);
            UPD_POS.setInt(4, pos.y);
            UPD_POS.setInt(5, pos.z);
            UPD_POS.executeUpdate();
            return true;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean UpdateArea(final String AreaID,final String ScriptID, final HashMap<String,Object> values){
        try{
            UPD_POS.setString(1, ScriptID);
            UPD_POS.setObject(2, values);
            UPD_POS.setString(3, AreaID);
            int af = UPD_POS.executeUpdate();
            return af == 1;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean UpdateScript(final String ScriptID, final String Content) {
        try {
            UPD_SCRIPT.setString(1, Content);
            UPD_SCRIPT.setString(2, ScriptID);
            UPD_SCRIPT.executeUpdate();
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean UpdatePosVars(final ScriptPos pos, final HashMap<String,Object> values) {
        try {
            UPD_POS_VAR.setObject(1, values);
            UPD_POS_VAR.setInt(2, pos.x);
            UPD_POS_VAR.setInt(3, pos.y);
            UPD_POS_VAR.setInt(4, pos.z);
            int af = UPD_POS_VAR.executeUpdate();
            return af == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean UpdateAreaVars(final String AreaID, final HashMap<String,Object> values) {
        try {
            UPD_AREA_VAR.setObject(1, values);
            UPD_AREA_VAR.setString(2, AreaID);
            int af = UPD_AREA_VAR.executeUpdate();
            return af == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean PutScript(final String ScriptID, final String Content) {
        try {
            PUT_SCRIPT.setString(1, ScriptID);
            PUT_SCRIPT.setString(2, Content);
            PUT_SCRIPT.executeUpdate();
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean PutPos(final ScriptPos pos, final String ScriptID, final HashMap<String,Object> values) {
        try {
            PUT_POS.setInt(1, pos.x);
            PUT_POS.setInt(2, pos.y);
            PUT_POS.setInt(3, pos.z);
            PUT_POS.setString(4, ScriptID);
            PUT_POS.setObject(5, values);
            PUT_POS.executeUpdate();
            return true;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean PutArea(final ScriptPos pos1, final ScriptPos pos2, final String AreaID, final String ScriptID,
            final HashMap<String,Object> values) {
        try {
            PUT_AREA.setInt(1, pos1.x);
            PUT_AREA.setInt(2, pos1.y);
            PUT_AREA.setInt(3, pos1.z);
            PUT_AREA.setInt(4, pos2.x);
            PUT_AREA.setInt(5, pos2.y);
            PUT_AREA.setInt(6, pos2.z);
            PUT_AREA.setString(7, AreaID);
            PUT_AREA.setString(8, ScriptID);
            PUT_AREA.setObject(9, values);
            int af = PUT_AREA.executeUpdate();
            return af == 1;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}