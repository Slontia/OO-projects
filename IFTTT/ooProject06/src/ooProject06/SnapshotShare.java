package ooProject06;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;

public class SnapshotShare {
	private int threadNum = 0;
	private ArrayList<DocumentSnapshot> renamedDocList = new ArrayList<DocumentSnapshot>();
	private DirectorySnapshot workspaceShot = null;
	private int gotThreadCount = 0;
	private int overThreadCount = 0;
	
	public SnapshotShare() {
	}
	
	public void addThreadNum(){
		this.threadNum ++;
	}
	
	public void setShot(DirectorySnapshot workspaceShot){
		while (this.gotThreadCount < this.threadNum || this.overThreadCount < this.threadNum){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.workspaceShot = workspaceShot;
		this.gotThreadCount = 0;
		this.overThreadCount = 0;
		this.renamedDocList.clear();
		notifyAll();
	}
	
	public void addGotThreadCount(){
		this.gotThreadCount ++;
	}
	
	public void addOverThreadCount(){
		this.overThreadCount ++;
	}
	
	public DirectorySnapshot getShot(String workspacePath, DirectorySnapshot oldDirShot) {
		while (this.workspaceShot == null || this.workspaceShot.pathSeekDirShot(workspacePath) == oldDirShot) {
			try {
				System.out.println(132);
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		notifyAll();
		return this.workspaceShot.pathSeekDirShot(workspacePath);
	}
	
	public boolean canBeRenamedDirShot(DocumentSnapshot renamedDocShot){
		return !this.renamedDocList.contains(renamedDocShot);
	}
	
	public void addToRenamedDocShotList(DocumentSnapshot renamedDocShot){
		this.renamedDocList.add(renamedDocShot);
	}
}
