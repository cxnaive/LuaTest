package cxmc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileLoader {
    public String BaseDir;
    public FileLoader(String BaseDir){
        this.BaseDir = BaseDir;
    }
    String ReadScript(String file){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(BaseDir+"/"+file)));
            String tmp = null;
            StringBuffer buffer = new StringBuffer();
            while((tmp = reader.readLine()) != null){
                buffer.append(tmp);
                buffer.append('\n');
            }
            reader.close();
            return buffer.toString();
        } catch(Exception ex){
            return null;
        }
    }
}