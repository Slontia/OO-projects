package ooProject06;

import java.io.File;
import java.nio.channels.NetworkChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputHandle {
	static final private Pattern PTN = Pattern.compile("^IF \\<(?<fileAbpath>.*)> (?<monitorType>renamed|modified|path-changed|size-changed) THEN (?<task>record-summary|record-detail|recover)$");
	static final private int MIN_FILE_NUM = 5;
	static final private int MAX_FILE_NUM = 8;
	final Map<String, String> fileToTopFile = new HashMap<String, String>();
	final Map<String, MonitorThread> monitorThreads = new HashMap<String, MonitorThread>();
	final Map<String, SnapshotShare> snapshotShares = new HashMap<String, SnapshotShare>();
	final Set<TriggerThread> triggerThreads = new HashSet<TriggerThread>();
	final FileShare fileShare;
	final SummaryTaskThread summaryTaskThread = new SummaryTaskThread();
	final DetailTaskThread detailTaskThread = new DetailTaskThread();
	
	public InputHandle(FileShare fileShare) {
		this.fileShare = fileShare;
	}
	
	private void startThreads() {
		InfoHandle.printInfo("ready to start.");
		summaryTaskThread.start();
		detailTaskThread.start();
		Set<MonitorThread> monitorThreadTempList = new HashSet<MonitorThread>();
		for (MonitorThread thread : monitorThreads.values()){
			if (!monitorThreadTempList.contains(thread)){
				monitorThreadTempList.add(thread);
				thread.start();
			}
		}
		InfoHandle.printInfo(monitorThreadTempList.size() + " threads started.");
		for (TriggerThread thread : triggerThreads){
			thread.start();
		}
		InfoHandle.printInfo(triggerThreads.size() + " threads started.");
		InfoHandle.printInfo("all started already.");
	}
	
	private TaskType toTaskType(String str){
		if (str.equals("record-summary")){
			return TaskType.SUMMARY;
		}else if (str.equals("record-detail")){
			return TaskType.DETAIL;
		}else if (str.equals("recover")){
			return TaskType.RECOVER;
		}
		return null;
	}
	
	private TriggerType toTriggerType(String str){
		if (str.equals("renamed")){
			return TriggerType.RENAMED;
		}else if (str.equals("modified")){
			return TriggerType.MODIFIED;
		}else if (str.equals("path-changed")){
			return TriggerType.PATH_CHANGED;
		}else if (str.equals("size-changed")){
			return TriggerType.SIZE_CHANGED;
		}
		return null;
	}
	
	private boolean repeatedRequestJudge(String abpath, TriggerType triggerType, TaskType taskType) {
		for (TriggerThread thread : triggerThreads){
			if (
				abpath.equals(thread.getAbpath()) &&
				triggerType == thread.getTriggerType()
			){
				if (!thread.hasTask(taskType)){
					thread.addTask(taskType);
				}
				return true;
			}
		}
		return false;
	}
	
	private void bindTopFile(File file){
		File dirFile;
		if (file.isDirectory()){
			dirFile = file;
		}else {
			dirFile = file.getParentFile();
		}
		String dirFileAbpath = dirFile.getAbsolutePath();
		for (String topFileAbpath : fileToTopFile.values()){
			if (dirFileAbpath.startsWith(topFileAbpath)){
				fileToTopFile.put(file.getAbsolutePath(), topFileAbpath);
				return;
			}
		}
		fileToTopFile.put(file.getAbsolutePath(), dirFileAbpath);
		for (String fileAbpath : fileToTopFile.keySet()){
			if (fileToTopFile.get(fileAbpath).startsWith(dirFileAbpath)){
				fileToTopFile.put(fileAbpath, dirFileAbpath);
			}
		}
	}
	
	public void inputTask() {
		String fileStr, monitorStr, taskStr;
		while(true) {
			Scanner scanner = new Scanner(System.in);
			String str = scanner.nextLine();
			if (str.equals("over")){
				if (fileToTopFile.size() < MIN_FILE_NUM){
					System.out.println("Too few files!");
					continue;
				}
				break;
			}
			Matcher matcher = this.PTN.matcher(str);
			if (!matcher.matches()){
				System.out.println("Invalid input.");
				continue;
			}
			
			File file = new File(matcher.group("fileAbpath"));
			TaskType taskType = toTaskType(matcher.group("task"));
			TriggerType triggerType = toTriggerType(matcher.group("monitorType"));
			
			if (!file.exists()) {
				System.out.println("File not found.");
				continue;
			}
			if (
				taskType == TaskType.RECOVER &&
				(triggerType == TriggerType.MODIFIED || 
				triggerType == TriggerType.SIZE_CHANGED || 
				file.isDirectory())
			){
				System.out.println("Invalid task.");
				continue;
			}
			if (!fileToTopFile.containsKey(file)){
				if (fileToTopFile.size() >= MAX_FILE_NUM){
					System.out.println("Too many files!");
					continue;
				}
				bindTopFile(file);
			}
			
			if (repeatedRequestJudge(file.getAbsolutePath(), triggerType, taskType)){
				continue;
			}
			if (file.isDirectory()){
				DirectoryTriggerThread thread = new DirectoryTriggerThread(triggerType, file, summaryTaskThread, detailTaskThread, fileShare);
				thread.addTask(taskType);
				triggerThreads.add(thread);
			}else {
				DocumentTriggerThread thread = new DocumentTriggerThread(triggerType, file, summaryTaskThread, detailTaskThread, fileShare);
				thread.addTask(taskType);
				triggerThreads.add(thread);
			}
		}
		
		for (String abpath : fileToTopFile.keySet()){
			String topAbpath = fileToTopFile.get(abpath);
			if (!monitorThreads.containsKey(topAbpath)){
				snapshotShares.put(topAbpath, new SnapshotShare());
				monitorThreads.put(topAbpath, new MonitorThread(snapshotShares.get(topAbpath), fileShare, new File(topAbpath)));
			}
			SnapshotShare share = snapshotShares.get(topAbpath);
			
			for (TriggerThread thread : triggerThreads){
				if (abpath.equals(thread.getAbpath())){
					thread.setSnapshotShare(share);
					share.addGotThreadCount(); // --- count threads
				}
			}
		}
		startThreads();
	}
}
