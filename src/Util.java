import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util{
	public static void exportStructure(String file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    	System.out.println("fixing...");
    	String ss = "";
    	String db = "db_enterprise_0";
    	List<Map<String, String>> list = new ArrayList<>();
    	boolean creatingField = false;
    	String currTable = "";
    	
    	while((ss = br.readLine()) != null) {    		    	
    		// Start create table 
    		if (ss.contains("CREATE TABLE")) {
    			creatingField = true;
    			String[] tbsplit = ss.split("`");
    			currTable = tbsplit[1];
    			continue;
			}
    		
    		// Finish current table
    		if (creatingField == true && ss.startsWith(")")) {
    			creatingField = false;
    			currTable = "";
    			continue;
    		}
    		
    		if (creatingField == true && !ss.contains("KEY")) {
    			Map<String, String> map = new HashMap<>();
    			String[] fieldsplit = ss.split("`");    			
    			map.put("db", db);
    			map.put("tb", currTable);
    			map.put("field", fieldsplit[1]);
    			map.put("desc", fieldsplit[2].substring(0, fieldsplit[2].length() - 1));
    			list.add(map);
    		}  		
    	}
    	
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("fixed.txt")));
    	for (Map<String, String> map_item : list) {
			bw.write(map_item.get("db") + "\t" + map_item.get("tb") + "\t" +  map_item.get("field") + "\t" + map_item.get("desc") + "\n");
		}
    	br.close();
    	bw.close();
    	System.out.println("finished.");
	}
	
	public static void main(String[] args) throws IOException {
		String fileName = "db.sql";
		Util.exportStructure(fileName);
	}
}