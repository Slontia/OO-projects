package ooProject06;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// One Thread for One File
public class MonitorThread extends Thread{
	final private int SLEEP_TIME_MILL = 1000;
	final private SnapshotShare snapshotShare;
	final private FileShare fileShare;
	final private File workspace;
	
	public MonitorThread(SnapshotShare snapshotShare, FileShare fileShare, File workspace) {
		this.snapshotShare = snapshotShare;
		this.fileShare = fileShare;
		this.workspace = workspace;
	}
	
	private void putIntoDirShot(DirectorySnapshot dirShot, File dirFile){
		for (File file : dirFile.listFiles()){
			// is document
			if (!file.isDirectory()){
				DocumentSnapshot subdocShot = new DocumentSnapshot(file, dirShot);
				dirShot.putIntoMap(subdocShot);
				
			// is directory
			}else {
				DirectorySnapshot subdirShot = new DirectorySnapshot(file);
				putIntoDirShot(subdirShot, file);
				dirShot.putIntoMap(subdirShot);
			}
		}
	}
	
	private DirectorySnapshot getDirShot(File dirFile) {
		synchronized (this.fileShare) {
			if (!dirFile.isDirectory()){
				InfoHandle.error("Not a directory");
			}
			DirectorySnapshot dirShot = new DirectorySnapshot(dirFile);
			putIntoDirShot(dirShot, dirFile);
			return dirShot;
		}
	}
	
	@Override
	public void run() {
		while (true){
			DirectorySnapshot workspaceShot;
			workspaceShot = getDirShot(workspace);
			synchronized (this.snapshotShare) {
				this.snapshotShare.setShot(workspaceShot);
			}
		}
	}
}
