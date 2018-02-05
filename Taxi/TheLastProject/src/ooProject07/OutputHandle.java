package ooProject07;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OutputHandle {
	/*@Overview:
	 * OutputHandle是一个向数据文件写入数据的类。
	 */
	final private String fileName = "data.txt";
	final private File file;
	final Map<Integer, String> crData = new HashMap<Integer, String>();
	private int count = 0;
	
	//不变式	c.file!=null && c.crData!=null && c.count>=0;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (file!=null && crData!=null && count>=0);
	}
	
	public OutputHandle() {
		//@REQUIRES: 
		//@MODIFIES: file;
		//@EFFECTS: 创建了一个新的OutputHandle对象并初始化;
		//@			!exists ==> be created;
		//@			data in the file is cleared;
		this.file = new File(fileName);
		createIfNotExists();
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createIfNotExists(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: !file.exists ==> file.createNewFile;
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized public void createData(CRrequest cr){
		//@REQUIRES: !keySet.contains(cr);
		//@MODIFIES: cr; crData;
		//@EFFECTS: cr.no == \old(count);
		//@			count == \old(count) + 1;
		//@			get(\old(count)).equals("===== " + cr.toString + " =====\n");
		//@THREAD_EFFECTS: \locked();
		cr.setNo(count);
		crData.put(count++, "===== " + cr.getActivatingTimeMill() + ":" + cr.toString() + " =====\n");
	}
	
	synchronized public void writeLine(CRrequest cr, String msg){
		//@REQUIRES: keySet.contains(cr);
		//@MODIFIES: crData;
		//@EFFECTS: get(cr.no).equals(\old(get(cr.no)) + msg + "\n");
		//@THREAD_EFFECTS: \locked();
		String string = crData.get(cr.getNo());
		crData.put(cr.getNo(), string + msg + "\n");
	}
	
	synchronized public String getData(CRrequest cr){
		//@REQUIRES: keySet.contains(cr);
		//@MODIFIES: None;
		//@EFFECTS: \result == get(cr.no);
		return crData.get(cr.getNo());
	}
	
	synchronized public void saveToFile(CRrequest cr){
		//@REQUIRES: keySet.contains(cr);
		//@MODIFIES: crData;
		//@EFFECTS: get(cr.no) is written to file;
		//@			!keySet.contains(cr);
		//@			size == \old(size) - 1;
		//@THREAD_EFFECTS: \locked();
		createIfNotExists();
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(crData.get(cr.getNo()) + "\n\n");
			fileWriter.close();
		} catch (IOException e) {
			InfoHandle.error("Please close 'data.txt' or we cannot write data in!");
		}
		crData.remove(cr.getNo());
	}
}
