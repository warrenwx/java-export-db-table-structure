import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Util
{
     
  public static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  public static String DB_URL = "jdbc:mysql://127.0.0.1:3306/db_xxx";
  
  public static String USER = "xxx";
  public static String PASS = "xxx";
  
  public static int tbcnt = 0;
  
  public static void exportstructure(String userName, String path, String db) throws IOException, SQLException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    String ss = "";
    List<Map<String, String>> list = new ArrayList();
    boolean creatingField = false;
    String currTable = "";
    
    while ((ss = br.readLine()) != null)
    {
      if (ss.contains("CREATE TABLE")) {
        tbcnt += 1;
        creatingField = true;
        String[] tbsplit = ss.split("`");
        currTable = tbsplit[1];

      }
      else if ((creatingField) && (ss.startsWith(")"))) {
        creatingField = false;
        currTable = "";

      }
      else if ((creatingField) && (!ss.contains("KEY"))) {
        Map<String, String> map = new HashMap();
        String[] fieldsplit = ss.split("`");
        map.put("db", db);
        map.put("tb", currTable);
        map.put("field", fieldsplit[1]);
        map.put("desc", fieldsplit[2].substring(0, fieldsplit[2].length() - 1));
        list.add(map);
      }
    }
    
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userName + "_" + db + "_fixed" + ".txt")));
    for (Map<String, String> map_item : list) {
      map_item.replace("desc", ((String)map_item.get("desc")).replace("'", ""));
      String tmpTbName = (String)map_item.get("tb");
      if (!tmpTbName.matches(".*_[0-9]{0,1}[0-4]{0,1}$"))
      {
        if (tmpTbName.matches(".*_15"))
        {
          map_item.replace("tb", tmpTbName.substring(0, tmpTbName.lastIndexOf('_')) + "_" + tbSplitNum);
        }
        
        bw.write((String)map_item.get("db") + "\t" + (String)map_item.get("tb") + "\t" + (String)map_item.get("field") + "\t" + (String)map_item.get("desc") + "\n");
        String sql = "insert into tb_xxx set username = '" + userName + "', dbname = '" + (String)map_item.get("db") + "', tbname = '" + 
          (String)map_item.get("tb") + "', fieldname = '" + (String)map_item.get("field") + "', fielddesc = '" + (String)map_item.get("desc") + "'";
        System.out.println(sql);
        executesql(sql);
      }
    }
    br.close();
    bw.close();
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
    
    if (args.length != 2) {
      System.out.println("error argus. only accept one param(directory path of *.sql)");
      return;
    }
    
    String directoryPath = args[0];
    String userName = args[1];
    
    LinkedList<File> linkedList = GetDirectory(directoryPath);
    System.out.println("fixing...");
    initconn();
    for (File file : linkedList) {
      String fPath = file.getPath();
      System.out.println("parsing " + fPath + "...");
      String dbName = fPath.substring(fPath.lastIndexOf(File.separatorChar) + 1, fPath.lastIndexOf('.'));
      if (dbName.endsWith("_0"))
      {

        exportstructure(userName, fPath, dbName.replaceAll("_0", "_" + dbSplitNum));
      }
      else
      {
        exportstructure(userName, fPath, dbName);
      }
    }
    
    conn.close();
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
