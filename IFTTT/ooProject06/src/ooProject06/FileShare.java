package ooProject06;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public class FileShare {
	public synchronized boolean create(File file){
		if (file.exists()) {
			return false;
		}
		try {
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}
	
	public synchronized boolean createDir(File file){
		if (file.exists()) {
			return false;
		}
		return file.mkdirs();
	}
	
	public synchronized boolean delete(File file){
		if (!file.exists()){
			return false;
		}
		return file.delete();
	}
	
	public synchronized boolean deleteDir(File file){
		if (!file.exists()){
			return false;
		}
		if (file.isDirectory()){
			File[] files = file.listFiles();
			boolean success;
			for (File f : files){
				success = deleteDir(f);
				if (!success){
					return false;
				}
			}
		}
		return file.delete();
	}
	
	public synchronized boolean rename(File file, String newName){
		if (!file.exists()){
			return false;
		}
		String renamedFileAbpath = file.getParentFile().getAbsolutePath() + File.separator + newName;
		File renamedFile = new File(renamedFileAbpath);
		if (renamedFile.exists()){
			return false;
		}else {
			file.renameTo(renamedFile);
			return true;
		}
	}
	
	public synchronized boolean move(File file, String abpath){
		if (!file.exists()){
			return false;
		}
		File movedFile = new File(abpath);
		System.out.println(abpath);
		if (movedFile.exists() || !movedFile.getParentFile().exists()){
			return false;
		}else {
			return file.renameTo(movedFile);
		}
	}
	
	public synchronized boolean write(File file, String text){
		if (!file.exists()){
			return false;
		}
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(file, true);
			fileWriter.write(text);
			fileWriter.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public synchronized String getName(File file){
		if (!file.exists()){
			return null;
		}
		return file.getName();
	}
	
	public synchronized long getLenth(File file){
		if (!file.exists()){
			return -1;
		}
		return file.length();
	}
	
	public synchronized long getLastModified(File file){
		if (!file.exists()){
			return -1;
		}
		return file.lastModified();
	}
}
