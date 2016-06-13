package com.emc.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.opencsv.CSVReader;

public class ParseCSV {
	/**
	 * Creates a JSON to be sent back to client of following form { tableHeaders:[],tableData:[],tableName:}
	 * @param filePath
	 * @param table
	 * @param size
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject formResponse(String filePath,String table,int size) throws IOException {
		File file = new File(filePath);
		CSVReader csvReader = null;
		//Create new CSV Reader
		try {
			csvReader = new CSVReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		}
		ArrayList<String> list=new ArrayList<String>();
		list=this.readCSVHeader(csvReader,file);
		JSONObject obj=new JSONObject();//Final JSON Object
		obj.put("tableHeaders",list);
		int count=0;
		JSONArray arr1=new JSONArray();
		while(count<size){
			ArrayList<String> list1=new ArrayList<String>();
			try {
				list1=readCSVHeader(csvReader,file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			if(list1.size()==0){
				break;
			}
			arr1.add(list1);
			obj.put("tableData", arr1);
			System.out.println(list1);
			count++;
		}
		obj.put("tableName",table);
		return obj;
	}
	/**
	 * readCSVHeader [ Provided and reader and file this method return current pointer row of reader and form a ArrayList<String> of those values
	 * @param reader
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> readCSVHeader(CSVReader reader,File file) throws IOException{
		String[] columnList=null;
		ArrayList<String> list=new ArrayList<String>();
		try{
			columnList=reader.readNext();
			if(columnList==null){
				return list ;
			}
			
			for(int i=0;i<columnList.length;i++)
			{
				list.add(columnList[i]);
			}
		}
		catch(IOException e){
			e.printStackTrace();
			throw e;
		}
		return list;
	}
	/**
	 * Saves a file into specific location on host machine
	 * @param uploadedInputStream
	 * @param serverLocation
	 * @throws IOException
	 */
	public void saveFile(InputStream uploadedInputStream,
			String serverLocation) throws IOException {

		try {
			OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}
}
