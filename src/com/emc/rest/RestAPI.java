package com.emc.rest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Mango People
 *
 */
@SuppressWarnings("unused")
@Path("/table")
public class RestAPI {
	/*Create variable which will be constant and persist across*/
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/csvparser";
	private static final int BATCH_SIZE=1000;
	private static final int ROW_LENGTH=2;

	private static final String USER = "root";
	private static final String PASS = "admin";
	private Connection connection=null;
	private DataSource dataSource;
	private String getPathOfCSV_Folder(){
		File here = new File(".");
		return here.getAbsolutePath()+File.separator+"CSV_Temp";
	}
	/**
	 * [createConnection Create a JDBC Connection and return a connection string]
	 * @return [description]
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private Connection createConnection() throws NamingException, SQLException{
		Connection conn=null;
		Context initContext;
		try {
			initContext = new InitialContext();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		Context envContext;
		try {
			envContext = (Context)initContext.lookup("java:/comp/env");
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		try {
			dataSource = (DataSource)envContext.lookup("jdbc/db");
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		try {
			conn=dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return conn;
	}
	@GET
	@Path("/structure")
	@Produces("application/json")
	public String getStructure(@QueryParam("tableName") String tableName) throws NamingException, SQLException {
		System.out.println("Getting table for ..."+tableName);
		connection=createConnection();
		DatabaseOp dbOp=new DatabaseOp();
		return dbOp.tableStructure(connection,tableName);

	}
	/**
	 * Upload a File in users CSV_Temp Folder and propose table schema to client
	 * Also checks if the tableName provided by user/by default already exist or not
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws NamingException 
	 */

	@POST
	@Path("/schema")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String createSchema(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
			@FormDataParam("tableName") String tableName) throws SQLException, IOException, NamingException {
		connection=createConnection();
		/**
		 * fileName=Name of file uploaded
		 * table=Finalized tableName to be used to create table
		 */
		System.out.println("=========================Team Mango People=======================");
		String csvFolderPath=getPathOfCSV_Folder();
		String fileName=contentDispositionHeader.getFileName();
		String table;
		boolean isEmpty = (tableName.equals(null)|| tableName.equals("null") ||tableName.trim().length() == 0);
		if(isEmpty){
			table=fileName.substring(0, fileName.length()-4);
			System.out.println(table);
		}
		else{
			System.out.println("NOT NULL");
			table=tableName;
		}
		DatabaseMetaData dbm = null;
		try {
			dbm = connection.getMetaData();
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		// check if  table is there
		ResultSet tables = null;
		try {
			tables = dbm.getTables(null, null, table, null);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			if (tables.next()) {
				System.out.println("\nTable Exist");
				return null;
			  // Table exists
			}
			System.out.println("\nTable Does not exist");
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			throw e2;
		}
		//Create directory if not present
		try{      
	         // returns pathnames for files and directory
	         File f = new File(csvFolderPath);
	         
	         // create
	         Boolean bool = f.mkdir();
	         
	      }catch(Exception e){
	         // if any error occurs
	         e.printStackTrace();
	         throw e;
	      }
		String filePath = csvFolderPath+ File.separator+table+".csv";
		ParseCSV parseCSV=new ParseCSV();//Our csv parser
		// save the file to the server
		parseCSV.saveFile(fileInputStream, filePath);//Save file
		String output = "File saved to server location : " + filePath;
		System.out.println("\n*****"+output+"*****");
		JSONObject finalJSON=parseCSV.formResponse(filePath,table,ROW_LENGTH);
		return finalJSON.toJSONString();
	}
	
	/**
	 * Create a table
	 * @param str is JSON Coming from client in following form  {
                    'tableHeaders':[],
                    'dataTypes': [],
                    'tableName': '',
                    deleteCols:[]
                };
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public String createTable(String str) throws Exception
	{
		connection=createConnection();
		JSONParser parser=new JSONParser();
		JSONObject obj=new JSONObject();
		ArrayList<String> tableHeader=new ArrayList<String>();
		ArrayList<Boolean> deleteCols=new ArrayList<Boolean>();
		JSONArray dataTypes=new JSONArray();
		String tableName = null;
		
		
		System.out.println("Parsing JSON...");
		//Parse JSON
		try {
			obj=(JSONObject) parser.parse(str);
			for (Object key : obj.keySet()) {
		        String keyStr = (String)key;
		        Object keyvalue = obj.get(keyStr);
		        if(keyStr.equals("tableHeaders"))
		        {
		        	tableHeader=(ArrayList<String>) keyvalue;
		        }
		        if(keyStr.equals("dataTypes"))
		        {
		        	dataTypes=(JSONArray) keyvalue;
		        }
		        if(keyStr.equals("tableName")) {
		        	tableName=(String )keyvalue;
		        }
		        if(keyStr.equals("deletedCols")) {
		        	deleteCols=(ArrayList<Boolean>) keyvalue;
		        }
		    }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		DatabaseOp dbOp=new DatabaseOp();
		dbOp.createTable(connection,getPathOfCSV_Folder()+File.separator+tableName+".csv",tableName,tableHeader,dataTypes,deleteCols,BATCH_SIZE);
		return null;
		
	}
	@DELETE
	@Path("/drop/{tableName}")
	public void dropTable (@PathParam("tableName") String tableName) throws NamingException, SQLException {
		connection=createConnection();
		System.out.println("Deleting table "+tableName);
		DatabaseOp dbOp=new DatabaseOp();
		dbOp.dropTable(connection,tableName);
	}
	public static void main(String[] args) {
		
	}
}