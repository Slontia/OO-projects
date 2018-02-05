package ooProject02;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputHandler {
	final static String FILENAME = "result.txt";
	static File file;
	
	static public void init(){
		file = new File(FILENAME);
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public void output(String text){
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(text + "\n");
			fileWriter.close();
			//System.out.println(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public void close(){
	}
}
