import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sun.org.apache.xpath.internal.operations.And;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import javafx.util.Pair;
// 用于小库产生用户权限配置
public class GenerateDbConfigSmallDB
{
  public static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  public static String DB_URL = "jdbc:mysql://127.0.0.1:3306/db_config";
  
  public static String USER = "test";
  public static String PASS = "test";
  
  public static int tbcnt = 0;
  
  public static void exportstructure(String path, String db, boolean splitdb, Set<String> tables) throws IOException, SQLException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    String ss = "";
    boolean creatingField = false;
    String currTable = "";
    String fString = null;
    Map<String, Map<String, String>> map = new HashMap();
    while ((ss = br.readLine()) != null)
    {    	
      if (ss.contains("CREATE TABLE")) {
        creatingField = true;
        String[] tbsplit = ss.split("`");
        currTable = tbsplit[1];        
        boolean bsplittb = currTable.matches(".*_[0-9]{1,2}$");
        if(bsplittb) {
        	currTable = currTable.substring(0, currTable.lastIndexOf('_'));        	
        }
        Map<String, String> tbdbArea = new HashMap<>();
        tbdbArea.put("tb", currTable);
        tbdbArea.put("db", db);
        map.put(db+currTable, tbdbArea);
        if (bsplittb) {
			map.get(db+currTable).put("is_split_tb", "true");
		}else {
			map.get(db+currTable).put("is_split_tb", "false");
		}
        map.get(db+currTable).put("is_split_db", splitdb == true ? "true" : "false");
        fString = "";
      }
      else if ((creatingField) && (ss.startsWith(")"))) {
    	  map.get(db+currTable).put("fields", fString);
        creatingField = false;
        currTable = "";
        fString = null;
      }
      else if ((creatingField) && (ss.contains("PRIMARY KEY"))) {        
        String[] fieldsplit = ss.split("`");
        int slen = fieldsplit.length;
        String keys = "";
        for(int i = 1; i < slen - 1; i += 2) {        	
        	keys += ("\"" + fieldsplit[i] + "\",");        	
        }
        map.get(db+currTable).put("keys", keys);
      }else if ((creatingField) && (!ss.contains("KEY"))) {  
    	  String[] fieldsplit = ss.split("`");
    	  if (!fieldsplit[1].contains("t_.*")) {
    		  fString += ("\"" + fieldsplit[1] + "\",");
		}          
      }
    }
    
    final String prefix = "t_";
    for (String str : map.keySet()) {
    	if(tables.size() != 0 && !tables.contains(map.get(str).get("tb"))) {
    		continue;
    	}
    	tbcnt++;
    	String strout = prefix + tbcnt + " = { ";
    	strout += ("[\"TB\"] = \"" + map.get(str).get("tb") + "\", ");
    	strout += ("[\"DB\"] = \"" + map.get(str).get("db") + "\", ");
    	if(map.get(str).get("is_split_db").equals("true")) {
    		strout += ("[\"is_split_db\"] = \"" + map.get(str).get("is_split_db") + "\", ");
    	}
    	if(map.get(str).get("is_split_tb").equals("true")) {
    		strout += ("[\"is_split_tb\"] = \"" + map.get(str).get("is_split_tb") + "\", ");
    	}
    	strout += ("[\"fields\"] = {" + map.get(str).get("fields") + "}, ");
    	strout += ("[\"src_2_dst_map\"] = {db = \"db_samples\", tb = \"" + map.get(str).get("tb") + "\",},");
    	if(map.get(str).get("keys") != null && map.get(str).get("keys").equals("\"id\",")) {
    		strout += ("[\"use_id_limit\"] = true, ");
    	}
    	strout += ("[\"keys\"] = {" + map.get(str).get("keys") + "}, ");
    	System.out.println(strout + " },");
    }
    br.close();
  }
  
  public static Connection conn = null;
  public static Statement stmt = null;
  
  public static void initconn() throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver");
    
    System.out.println("连接数据库...");
    conn = DriverManager.getConnection(DB_URL, USER, PASS);
    
    System.out.println(" 实例化Statement对象...");
    stmt = conn.createStatement();
  }
  
  public static void executesql(String sql) throws SQLException {
    stmt.executeUpdate(sql);
  }
  
  public static String dbSplitNum = "16";
  public static String tbSplitNum = "16";
  

  public static void main(String[] args)
    throws Exception
  {
        
    String directoryPath = args[0];
    
    Set<String> hSet = new HashSet<String>();
    if (args.length == 2) {
    	String tablesFile = args[1]; 
    	BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(tablesFile)));
    	String line = "";
        while((line = bReader.readLine()) != null) {
        	hSet.add(line);
        }
	}
    
    LinkedList<File> linkedList = GetDirectory(directoryPath);
    
    System.out.println("start fixing...");
    for (File file : linkedList) {
      String fPath = file.getPath();
      // System.out.println("parsing " + fPath + "...");
      String dbName = fPath.substring(fPath.lastIndexOf(File.separatorChar) + 1, fPath.lastIndexOf('.'));
      if (dbName.endsWith("_0"))
      {

        exportstructure(fPath, dbName.replaceAll("_0", ""), true, hSet);
      }
      else
      {
        exportstructure(fPath, dbName, false, hSet);
      }
    }
    
    System.out.println("tbcnt:" + tbcnt);
    System.out.println("finish...");
  }
  
  public static LinkedList<File> GetDirectory(String path) { File file = new File(path);
    LinkedList<File> Dirlist = new LinkedList();
    LinkedList<File> fileList = new LinkedList();
    GetOneDir(file, Dirlist, fileList);
    
    while (!Dirlist.isEmpty()) {
      File tmp = (File)Dirlist.removeFirst();
      GetOneDir(tmp, Dirlist, fileList);
    }
    
    return fileList;
  }
  
  private static void GetOneDir(File file, LinkedList<File> Dirlist, LinkedList<File> fileList)
  {
    File[] files = file.listFiles();
    
    if ((files == null) || (files.length == 0)) {
      return;
    }
    for (File f : files) {
      if (f.isDirectory()) {
        Dirlist.add(f);
      } else {
        fileList.add(f);
      }
    }
  }
}
