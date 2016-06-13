package com.emc.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.csvreader.CsvReader;
import com.opencsv.CSVReader;
/**
 * 
 * @author Mango People
 *
 */
@SuppressWarnings("unused")
public class DatabaseOp {
	/**
	 * Create a table in Database provided
	 * @param conn = connection
	 * @param filePath = path from where CSV is read
	 * @param tableName = tableName proposed by client
	 * @param tableHeaders = tableHeader name
	 * @param dataTypes = Data type of each of table headers
	 * @param BATCH_SIZE = Batch size
	 * @throws Exception
	 */
	public void createTable(Connection conn,String filePath,String tableName,ArrayList<String> tableHeaders,JSONArray dataTypes,ArrayList<Boolean> deleteCols,int BATCH_SIZE) throws Exception
	{		
		File file=new File(filePath);
		String sql;
		conn.setAutoCommit(false);
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		ParseCSV csvParser=new ParseCSV();
		ArrayList<String> ignoreList=csvParser.readCSVHeader(csvReader,file);//ignore this because it is first row of CSV & corresponds to tableHeader
		int noOfCol_CSV=tableHeaders.size();
		String createTabletoken="",seperator=null;
		int noOfCol_table=0;
		System.out.println("Creating token string for create table "+deleteCols.size());
		for(int i=0;i<noOfCol_CSV;i++)
		{
			String ColumnName=tableHeaders.get(i);
			System.out.println("Delete "+ColumnName+" "+deleteCols.get(i));
			if(deleteCols.get(i)==true);
			else {
				JSONObject dataType = (JSONObject) dataTypes.get(i); 
				if(ColumnName.length()>=64){
					ColumnName=ColumnName.substring(0, 62);
				}
				if(noOfCol_table==0){
					seperator=" ";	
				}
				else {
					seperator=", ";
				}
				createTabletoken+=seperator+"`"+ColumnName+"` "+dataType.get("type");
				noOfCol_table++;	
			}
		}
		System.out.println("\n Token string for creating table is :- "+createTabletoken);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			//create table
			sql = "Create table "+tableName+" ("+createTabletoken+")";
			System.out.println("\n****Create Query is :- "+sql);
			int rs = stmt.executeUpdate(sql);
			//insert records
			int noOfRecords=0;
			FileReader fileReader=new FileReader(file);
			CsvReader csvReader1=new CsvReader(fileReader);
			Boolean b=csvReader1.readHeaders();
			while(csvReader1.readRecord())
			{	
				noOfRecords++;
			}
			System.out.println("\nNo of records: "+noOfRecords);
			String queryToken2= new String(new char[noOfCol_table-1]).replace("\0", "?,");
			queryToken2+="?";
			System.out.println(queryToken2);
			sql="insert into "+tableName+" values ("+queryToken2+")";
			PreparedStatement ps=conn.prepareStatement(sql);
			FileReader fileReader1=new FileReader(file);
			CsvReader csvReader11=new CsvReader(fileReader1);
			Boolean readheader=csvReader11.readHeaders();
			int batch=0,i=1;
			while(csvReader11.readRecord())
			{
				i=1;
				for(int j=1;j<=noOfCol_CSV;j++)
				{	
					if(deleteCols.get(j-1)==true){
						;
					}
					else {
						JSONObject dataTypeObj = (JSONObject) dataTypes.get(j-1);
						String dataType1=(String) dataTypeObj.get("type");
						switch(dataType1){
						case "TEXT":
							ps.setString(i,csvReader11.get(tableHeaders.get(j-1)));
							break;
						case "INT":
							ps.setInt(i, Integer.parseInt(csvReader11.get(tableHeaders.get(j-1))));
							break;
						case "FLOAT":
							ps.setFloat(i, Float.parseFloat(csvReader11.get(tableHeaders.get(j-1))));
							break;
						case "TIME":
							java.sql.Time myTime = java.sql.Time.valueOf(csvReader11.get(tableHeaders.get(j-1)));
							ps.setTime(i, myTime);
							break;
						case "DATE":
							SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
							java.util.Date myDate=formatter1.parse(csvReader11.get(tableHeaders.get(j-1)));
							java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
							ps.setDate(i, sqlDate);
							break;
						case "YEAR":
							ps.setString(i, csvReader11.get(tableHeaders.get(j-1)));
							break;
						case "DATETIME":
							SimpleDateFormat formatter111=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							java.util.Date myDate11=formatter111.parse(csvReader11.get(tableHeaders.get(j-1)));
							java.sql.Date sqlDate11 = new java.sql.Date(myDate11.getTime());
							ps.setDate(i, sqlDate11);
							break;
						}
						i++;
					}
				}
				ps.addBatch();
				batch++;
				if(batch%BATCH_SIZE==0){
					ps.executeBatch();
					batch=0;
				}
			}
			ps.executeBatch();
			conn.commit();
			System.out.println("Inserted into table");
			stmt.close();
			boolean temp=file.delete();
			System.out.println("File deleted! "+temp);
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
			sql="drop table "+tableName;
			stmt.execute(sql);
			conn.commit();
			throw se;
		} catch (Exception e) {
			e.printStackTrace();
			sql="drop table "+tableName;
			stmt.execute(sql);
			conn.commit();
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	public void dropTable(Connection conn,String tableName) throws SQLException {
		Statement stmt=null;
		String sql="drop table "+tableName;
		System.out.println(sql);
		stmt = conn.createStatement();
		stmt.execute(sql);
		stmt.close();
		conn.close();
	}
	@SuppressWarnings("unchecked")
	public String tableStructure(Connection conn,String tableName) throws SQLException {
		Statement stmt=null;
		String sql="SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_NAME`="+"\""+tableName+"\"";
		stmt = conn.createStatement();
		ResultSet cols=stmt.executeQuery(sql);
		ArrayList<String> colList=new ArrayList<String>();
		while (cols.next()) {
	        colList.add(cols.getString(1));
	    } 
		JSONObject obj=new JSONObject();
		obj.put("tableHeaders",colList);
		sql="Select * from "+tableName;
		ResultSet rows=stmt.executeQuery(sql);
		JSONArray tableDataArr=new JSONArray();
		int noOfCols=colList.size(),curr=0;
		while(rows.next()){
			ArrayList<String> list1=new ArrayList<String>();
			for(int i=0;i<noOfCols;i++){
				list1.add(rows.getObject(i+1).toString());
			}
			tableDataArr.add(list1);
		}
		obj.put("tableData", tableDataArr);
		System.out.println("Table sent!");
		stmt.close();
		conn.close();
		return obj.toJSONString();
		
	}
}
