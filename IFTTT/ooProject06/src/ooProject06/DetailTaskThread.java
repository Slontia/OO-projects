package ooProject06;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DetailTaskThread extends Thread{
	static final int SLEEP_MILL = 5000;
	final String FILENAME = "detail.txt";
	final File file;
	ArrayList<String> renamedDetail = new ArrayList<String>();
	ArrayList<String> modifiedDetail = new ArrayList<String>();
	ArrayList<String> sizeChangedDetail = new ArrayList<String>();
	ArrayList<String> pathChangedDetail = new ArrayList<String>();
	
	public DetailTaskThread() {
		this.file = new File(FILENAME);
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void writeToFile() {
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("renamed:\n");
			for (int i=0; i<renamedDetail.size(); i++){
				fileWriter.write(renamedDetail.get(i) + "\n");
			}
			fileWriter.write("\nmodified:\n");
			for (int i=0; i<modifiedDetail.size(); i++){
				fileWriter.write(modifiedDetail.get(i) + "\n");
			}
			fileWriter.write("\npath_changed:\n");
			for (int i=0; i<pathChangedDetail.size(); i++){
				fileWriter.write(pathChangedDetail.get(i) + "\n");
			}
			fileWriter.write("\nsize_changed:\n");
			for (int i=0; i<sizeChangedDetail.size(); i++){
				fileWriter.write(sizeChangedDetail.get(i) + "\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void recordDirSizeChanged(long oldLenthSum, long newLenthSum, String abpath){
		sizeChangedDetail.add(oldLenthSum + " -> " + newLenthSum + " at " + abpath);
	}
	
	public synchronized void record(TriggerType triggerType, DocumentSnapshot oldShot, DocumentSnapshot newShot) {
		switch (triggerType) {
		case RENAMED:
			renamedDetail.add(oldShot.getAbpath() + " -> " + newShot.getAbpath());
			break;
		case MODIFIED:
			Date oldDate = new Date(oldShot.getLastModifiedTimeMill());
			Date newDate = new Date(newShot.getLastModifiedTimeMill());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			modifiedDetail.add(oldShot.getAbpath() + " : " + formatter.format(oldDate) + " -> " + formatter.format(newDate));
			break;	
		case PATH_CHANGED:
			pathChangedDetail.add(oldShot.getAbpath() + " ->" + newShot.getAbpath());
			break;
		case SIZE_CHANGED:
			if (oldShot == null) {
				sizeChangedDetail.add("new: " + newShot.getLenth() + " at " + newShot.getAbpath());
			}else if (newShot == null) {
				sizeChangedDetail.add("del: " + oldShot.getLenth() + " at " + oldShot.getAbpath());
			}else {
				sizeChangedDetail.add(oldShot.getLenth() + " -> " + newShot.getLenth() + " at " + oldShot.getAbpath());
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(SLEEP_MILL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			writeToFile();
		}
	}
}
