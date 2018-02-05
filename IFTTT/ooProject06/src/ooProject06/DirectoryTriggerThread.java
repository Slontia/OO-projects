package ooProject06;

import java.io.File;

public class DirectoryTriggerThread extends TriggerThread{
	public DirectoryTriggerThread(TriggerType triggerType, File file, SummaryTaskThread summaryTaskThread, DetailTaskThread detailTaskThread, FileShare fileShare) {
		super(triggerType, file, summaryTaskThread, detailTaskThread, fileShare);
	}
	
	protected void renamedJudge(DirectorySnapshot dirShot) {
		for (DocumentSnapshot shot : dirShot.docMap.values()){
			renamedJudge(shot);
		}
		for (DirectorySnapshot shot : dirShot.dirMap.values()){
			renamedJudge(shot);
		}
	}
	
	protected void modifiedJudge(DirectorySnapshot dirShot){
		for (DocumentSnapshot shot : dirShot.docMap.values()){
			modifiedJudge(shot);
		}
		for (DirectorySnapshot shot : dirShot.dirMap.values()){
			modifiedJudge(shot);
		}
	}
	
	protected void pathChangedJudge(DirectorySnapshot dirShot){
		for (DocumentSnapshot shot : dirShot.docMap.values()){
			pathChangedJudge(shot);
		}
		for (DirectorySnapshot shot : dirShot.dirMap.values()){
			pathChangedJudge(shot);
		}
	}
	
	protected void taskRecordDirSizeChanged(long oldLenthSum, long newLenthSum){
		if (this.taskSet.contains(TaskType.SUMMARY)){
			summaryTaskThread.record(TriggerType.SIZE_CHANGED);
		}else if (this.taskSet.contains(TaskType.DETAIL)){
			detailTaskThread.recordDirSizeChanged(oldLenthSum, newLenthSum, this.abpath);
		}
	}
	
	protected void sizeChangedJudge(DirectorySnapshot oldDirShot, DirectorySnapshot newDirShot){
		long oldLenthSum = 0, newLenthSum = 0;
		for (DocumentSnapshot shot : oldDirShot.docMap.values()){
			oldLenthSum += shot.getLenth();
		}
		for (DocumentSnapshot shot : newDirShot.docMap.values()){
			newLenthSum += shot.getLenth();
		}
		if (oldLenthSum != newLenthSum){
			taskRecordDirSizeChanged(oldLenthSum, newLenthSum);
		}
	}
	
	@Override
	public void run() {
		while (true){
			receiveShot();
			
			if (oldWorkspaceShot != null){
				switch (this.triggerType) {
				case RENAMED:
					renamedJudge(oldWorkspaceShot.pathSeekDirShot(this.abpath));
					break;
				case MODIFIED:
					modifiedJudge(oldWorkspaceShot.pathSeekDirShot(this.abpath));
					break;
				case PATH_CHANGED:
					pathChangedJudge(oldWorkspaceShot.pathSeekDirShot(this.abpath));
					break;
				case SIZE_CHANGED:
					sizeChangedJudge(oldWorkspaceShot, newWorkspaceShot);
					break;
				default:
					break;
				}
			}
			oldWorkspaceShot = newWorkspaceShot;
			newWorkspaceShot = null;
			synchronized (snapshotShare) {
				snapshotShare.addOverThreadCount();
			}
			try {
				Thread.sleep(SLEEP_MILL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}