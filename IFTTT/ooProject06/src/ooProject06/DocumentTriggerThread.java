package ooProject06;

import java.io.File;

public class DocumentTriggerThread extends TriggerThread{
	public DocumentTriggerThread(TriggerType triggerType, File file, SummaryTaskThread summaryTaskThread, DetailTaskThread detailTaskThread, FileShare fileShare) {
		super(triggerType, file, summaryTaskThread, detailTaskThread, fileShare);
	}
	
	@Override
	protected void recordDocShot(DocumentSnapshot docShot) {
		if (docShot != null){
			this.abpath = docShot.getAbpath();
		}
	}
	
	private boolean sizeChangedJudge(String abpath) {
		DocumentSnapshot oldDocShot = oldWorkspaceShot.pathSeekDocShot(abpath);
		DocumentSnapshot newDocShot = newWorkspaceShot.pathSeekDocShot(abpath);
		if (
			(oldDocShot == null && newDocShot != null) ||
			(oldDocShot != null && newDocShot == null) ||
			(oldDocShot != null && newDocShot != null && oldDocShot.getLenth() != newDocShot.getLenth())
		){
			System.out.println("size changed");
			taskRecord(oldDocShot, newDocShot);
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public void run() {
		while (true){
			receiveShot();			
			if (oldWorkspaceShot != null){
				switch (this.triggerType) {
				case RENAMED:
					renamedJudge(oldWorkspaceShot.pathSeekDocShot(this.abpath));
					break;
				case MODIFIED:
					modifiedJudge(oldWorkspaceShot.pathSeekDocShot(this.abpath));
					break;
				case PATH_CHANGED:
					pathChangedJudge(oldWorkspaceShot.pathSeekDocShot(this.abpath));
					break;
				case SIZE_CHANGED:
					sizeChangedJudge(this.abpath);
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
