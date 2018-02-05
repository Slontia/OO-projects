package ooProject06;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

enum FileType{
	DOCUMENT, DIRECTORY
}

abstract class FileSnapshot{
	final private FileType fileType;
	
	public FileSnapshot(FileType fileType) {
		this.fileType = fileType;
	}

	public FileType getFileType() {
		return fileType;
	}
	
	abstract String getName();
	
	abstract String getAbpath();
}

class DocumentSnapshot extends FileSnapshot{
	private String abpath;
	private String name;
	private long lenth;
	private long lastModifiedTimeMill;
	private DirectorySnapshot parentDirShot;
	
	public DocumentSnapshot(File docFile, DirectorySnapshot parentDirShot) {
		super(FileType.DOCUMENT);
		if (docFile.isDirectory()) {
			InfoHandle.error("Not a document");
		}
		this.abpath = docFile.getAbsolutePath();
		this.name = docFile.getName();
		this.lenth = docFile.length();
		this.lastModifiedTimeMill = docFile.lastModified();
		this.parentDirShot = parentDirShot;
	}
	
	public void cutFromParentShot(){
		parentDirShot.getDocMap().remove(this.name);
	}
	
	@Override
	public String getAbpath(){
		return this.abpath;
	}
	
	@Override
	public String getName(){
		return this.name;
	}
	
	public long getLenth(){
		return this.lenth;
	}
	
	public long getLastModifiedTimeMill() {
		return lastModifiedTimeMill;
	}
	
	public DirectorySnapshot getParentDirShot(){
		return this.parentDirShot;
	}
	
	public void setParentDirShot(DirectorySnapshot dirShot){
		this.parentDirShot = dirShot;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}

class DirectorySnapshot extends FileSnapshot{
	final private String abpath;
	final private String name;
	final Map<String, DocumentSnapshot> docMap = new HashMap<String, DocumentSnapshot>();
	final Map<String, DirectorySnapshot> dirMap = new HashMap<String, DirectorySnapshot>();
	
	public DirectorySnapshot(File dirFile) {
		super(FileType.DIRECTORY);
		if (!dirFile.isDirectory()){
			InfoHandle.error("Not a directory");
		}
		this.abpath = dirFile.getAbsolutePath();
		this.name = dirFile.getName();
	}
	
	public void putIntoMap(FileSnapshot fileShot) {
		if (fileShot.getFileType() == FileType.DOCUMENT){
			if (docMap.put(fileShot.getName(), (DocumentSnapshot)fileShot) != null) {
				InfoHandle.error("Rewrite key: " + fileShot.getName());
			}
		}else if (fileShot.getFileType() == FileType.DIRECTORY){
			if (dirMap.put(fileShot.getName(), (DirectorySnapshot)fileShot) != null) {
				InfoHandle.error("Rewrite key: " + fileShot.getName());
			}
		}
	}
	
	public DocumentSnapshot getSubdocShot (String name) {
		if (!this.docMap.containsKey(name)){
			return null; // --- not found
		}
		return this.docMap.get(name);
	}
	
	public DirectorySnapshot getSubdirShot (String name) {
		if (!this.dirMap.containsKey(name)){
			return null;
		}
		return this.dirMap.get(name);
	}
	
	private FileSnapshot pathSeekFileShot (String seekAbpath) {
		if (this.abpath.equals(seekAbpath)){
			return this;
		}
		String currentPath = this.abpath + File.separator;
		//System.out.println(currentPath);
		//System.out.println(seekAbpath);
		if (!seekAbpath.startsWith(currentPath)){
			InfoHandle.error("Not proper abpath");
		}
		String[] nextFileNames = seekAbpath.substring(currentPath.length()).split("\\" + File.separator);
		String nextFileName = nextFileNames[0];
		if (dirMap.containsKey(nextFileName) || docMap.containsKey(nextFileName)){
			if (nextFileNames.length == 1){
				if (dirMap.containsKey(nextFileName)){
					return dirMap.get(nextFileName); // --- found a directory
				}else if (docMap.containsKey(nextFileName)){
					return docMap.get(nextFileName); // --- found a document
				}
			}else {
				DirectorySnapshot nextDirShot = getSubdirShot(nextFileName);
				return nextDirShot.pathSeekFileShot(seekAbpath);
			}
		}
		return null;
	}
	
	public DocumentSnapshot pathSeekDocShot (String seekAbpath){
		FileSnapshot foundShot = pathSeekFileShot(seekAbpath);
		if (foundShot == null){
			return null;
		}else if (foundShot.getFileType() == FileType.DOCUMENT){
			return (DocumentSnapshot)foundShot;
		}
		return null;
	}
	
	public DirectorySnapshot pathSeekDirShot (String seekAbpath){
		FileSnapshot foundShot = pathSeekFileShot(seekAbpath);
		if (foundShot == null){
			return null;
		}else if (foundShot.getFileType() == FileType.DIRECTORY){
			return (DirectorySnapshot)foundShot;
		}
		return null;
	}
	
	public DocumentSnapshot seekSameNewDocShot (DocumentSnapshot templetDocShot, DirectorySnapshot oldWorkspace){
		for (DocumentSnapshot shot : this.docMap.values()) {
			if (
				shot.getLenth() == templetDocShot.getLenth() &&
				shot.getName().equals(templetDocShot.getName()) &&
				shot.getLastModifiedTimeMill() == templetDocShot.getLastModifiedTimeMill() &&
				oldWorkspace.pathSeekDocShot(shot.getAbpath()) == null // --- cannot be found in last snapshot
			){
				return shot;
			}
		}
		for (DirectorySnapshot shot : this.dirMap.values()) {
			DocumentSnapshot foundDocShot = shot.seekSameNewDocShot(templetDocShot, oldWorkspace);
			if (foundDocShot != null){
				return foundDocShot;
			}
		}
		return null;
	}

	@Override
	public String getAbpath() {
		return abpath;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Map<String, DirectorySnapshot> getDirMap() {
		return dirMap;
	}
	
	public Map<String, DocumentSnapshot> getDocMap() {
		return docMap;
	}
}
