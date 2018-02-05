package ooProject06;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.print.Doc;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

enum TriggerType {
	RENAMED, MODIFIED, PATH_CHANGED, SIZE_CHANGED
}

public class TriggerThread extends Thread{
	static protected int SLEEP_MILL = 1000;
	protected String abpath;
	final protected String workspaceAbpath;
	protected TriggerType triggerType;
	protected SnapshotShare snapshotShare = null;
	protected DirectorySnapshot oldWorkspaceShot = null;
	protected DirectorySnapshot newWorkspaceShot = null;
	final protected Set<TaskType> taskSet = new HashSet<TaskType>();
	final protected SummaryTaskThread summaryTaskThread;
	final protected DetailTaskThread detailTaskThread;
	final protected FileShare fileShare;
	
	public TriggerThread(TriggerType triggerType, File file, SummaryTaskThread summaryTaskThread, DetailTaskThread detailTaskThread, FileShare fileShare) {
		
		this.triggerType = triggerType;
		this.abpath = file.getAbsolutePath();
		if (file.isDirectory()){
			this.workspaceAbpath = this.abpath;
		}else {
			this.workspaceAbpath = file.getParentFile().getAbsolutePath();
		}
		this.summaryTaskThread = summaryTaskThread;
		this.detailTaskThread = detailTaskThread;
		this.fileShare = fileShare;
	}
	
	protected void taskRecord(DocumentSnapshot oldShot, DocumentSnapshot newShot){
		if (taskSet.contains(TaskType.SUMMARY)){
			synchronized (summaryTaskThread) {
				summaryTaskThread.record(this.triggerType);
			}
		}
		if (taskSet.contains(TaskType.DETAIL)){
			synchronized (detailTaskThread) {
				detailTaskThread.record(this.triggerType, oldShot, newShot);
			}
		}
		if (taskSet.contains(TaskType.RECOVER)){
			synchronized (fileShare) {
				if (triggerType == TriggerType.RENAMED){
					String oldName = oldShot.getName();
					String newName = newShot.getName();
					System.out.println(oldName + " " + newName);
					fileShare.rename(new File(newShot.getAbpath()), oldName);
					File returnedFile = new File(oldShot.getAbpath()); 
					DirectorySnapshot parentDirShot = newShot.getParentDirShot();
					DocumentSnapshot returnedShot = new DocumentSnapshot(returnedFile, parentDirShot);
					parentDirShot.putIntoMap(returnedShot);
					newShot.cutFromParentShot();
					recordDocShot(returnedShot);
					
				}else if (triggerType == TriggerType.PATH_CHANGED){
					File returnedFile = new File(oldShot.getAbpath());
					File currentFile = new File(newShot.getAbpath());
					currentFile.renameTo(returnedFile);
					DirectorySnapshot returnedParentShot = newWorkspaceShot.pathSeekDirShot(oldShot.getParentDirShot().getAbpath());
					if (returnedParentShot == null){
						System.out.println("Recover failed! Direction not found!");
						recordDocShot(newShot);
					}else {
						DocumentSnapshot returnedShot = new DocumentSnapshot(returnedFile, returnedParentShot);
						returnedParentShot.putIntoMap(returnedShot);
						recordDocShot(returnedShot);
					}
				}
			}
		}else {
			recordDocShot(newShot);
		}
	}
	
	protected void receiveShot() {
		synchronized (this.snapshotShare) {
			this.newWorkspaceShot = this.snapshotShare.getShot(this.workspaceAbpath, this.oldWorkspaceShot);
			this.snapshotShare.addGotThreadCount();
			
		}
	}
	
	protected void recordDocShot(DocumentSnapshot docShot){
		/* It is a EMPTY function if not in DocumentTriggerThread */
		/* I'm really TM clever */
	}
	
	/* Must recordDocShot before return! */
	protected boolean renamedJudge(DocumentSnapshot docShot) {
		if (docShot == null) {
			return false; // not exist
		}
		
		DirectorySnapshot oldParentShot = docShot.getParentDirShot();
		DirectorySnapshot newParentShot = newWorkspaceShot.pathSeekDirShot(oldParentShot.getAbpath());
		
		// parent file missed
		if (newParentShot == null){
			recordDocShot(null);
			return false;
		}
		// old document found
		DocumentSnapshot newDocShot = newParentShot.getSubdocShot(docShot.getName());
		if (newDocShot != null){
			recordDocShot(newDocShot);
			return false;
		}
		// get renamed docShot
		for (DocumentSnapshot shot : newParentShot.getDocMap().values()){
			if (
				shot.getLenth() == docShot.getLenth() &&
				shot.getLastModifiedTimeMill() == docShot.getLastModifiedTimeMill() &&
				!shot.getName().equals(docShot.getName()) && // --- has a different name
				oldParentShot.getSubdocShot(shot.getName()) == null // --- cannot be found in last snapshot
			){
				synchronized (snapshotShare) {
					if (snapshotShare.canBeRenamedDirShot(shot)){
						snapshotShare.addToRenamedDocShotList(shot);
						taskRecord(docShot, shot);
						System.out.println("Renamed");
						return true;
					}
				}
			}
		}
		// not found renamed shot
		recordDocShot(null);
		return false;
	}
	
	/* Must recordDocShot before return! */
	protected boolean modifiedJudge(DocumentSnapshot docShot){
		if (docShot == null) {
			return false; // --- not exist
		}
		
		DirectorySnapshot oldParentShot = docShot.getParentDirShot();
		DirectorySnapshot newParentShot = newWorkspaceShot.pathSeekDirShot(oldParentShot.getAbpath());
		
		// parent file missed
		if (newParentShot == null){
			recordDocShot(null);
			return false;
		}
		
		DocumentSnapshot newDocShot = newParentShot.getSubdocShot(docShot.getName());
		recordDocShot(newDocShot); // --- record
		// not found
		if (newDocShot == null){
			return false;
		}
		// not be modified
		if (newDocShot.getLastModifiedTimeMill() != docShot.getLastModifiedTimeMill()){
			taskRecord(docShot, newDocShot);
			System.out.println("Modified");
			return true;
		}else {
			return false;
		}
	}
	
	/* Must recordDocShot before return! */
	protected boolean pathChangedJudge(DocumentSnapshot docShot){
		if (docShot == null) {
			return false; // --- not exist
		}
		
		DirectorySnapshot oldParentShot = docShot.getParentDirShot();
		DirectorySnapshot newParentShot = newWorkspaceShot.pathSeekDirShot(oldParentShot.getAbpath());
		
		if (newParentShot != null){
			DocumentSnapshot newDocShot = newParentShot.getSubdocShot(docShot.getName());
			if (newDocShot != null){
				recordDocShot(newDocShot);
				return false; // --- still exist
			}
		}
		
		DocumentSnapshot pathChangedNewDocShot = newWorkspaceShot.seekSameNewDocShot(docShot, oldWorkspaceShot);
		if (pathChangedNewDocShot != null){
			taskRecord(docShot, pathChangedNewDocShot);
			System.out.println("Path Changed.");
			return true;
		}else {
			recordDocShot(null);
			return false;
		}
	}
	
	public TriggerType getTriggerType() {
		return triggerType;
	}
	
	public void addTask(TaskType taskType) {
		this.taskSet.add(taskType);
	}
	
	public boolean hasTask(TaskType taskType) {
		return taskSet.contains(taskType);
	}
	
	public String getAbpath() {
		return abpath;
	}
	
	public void setSnapshotShare(SnapshotShare snapshotShare) {
		this.snapshotShare = snapshotShare;
	}
}
