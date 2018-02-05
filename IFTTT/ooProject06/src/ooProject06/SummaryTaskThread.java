package ooProject06;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SummaryTaskThread extends Thread{
	static final int SLEEP_MILL = 5000;
	final String FILENAME = "summary.txt";
	final File file;
	long renamedCount = 0;
	long modifiedCount = 0;
	long sizeChangedCount = 0;
	long pathChangedCount = 0;
	
	public SummaryTaskThread() {
		this.file = new File(FILENAME);
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void writeToFile(){
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("renamed: " + renamedCount + "\n");
			fileWriter.write("modified: " + modifiedCount + "\n");
			fileWriter.write("path_changed: " + pathChangedCount + "\n");
			fileWriter.write("size_changed: " + sizeChangedCount + "\n");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void record(TriggerType triggerType){
		switch (triggerType) {
		case RENAMED:
			//System.out.println("now" + renamedCount);
			renamedCount ++;
			break;
		case MODIFIED:
			modifiedCount ++;
			break;
		case SIZE_CHANGED:
			sizeChangedCount ++;
			break;
		case PATH_CHANGED:
			pathChangedCount ++;
			break;
		default:
			break;
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(SLEEP_MILL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			writeToFile();
		}
	}
}
