package ooProject07;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

public class InputHandle {
	/*@Overview:
	 * InputHandle是一个负责读取文件的类，可读取的文件有地图文件和信号灯文件。
	 */
	final private File mapFile = new File("map.txt");
	final private File lightFile = new File("light.txt");
	final private TaxiMap taxiMap;
	
	//不变式	c.taxiMap!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (taxiMap != null);
	}
	
	public InputHandle(TaxiMap taxiMap) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.taxiMap;
		//@EFFECTS: 创建了一个新的InputHandle对象并初始化;
		this.taxiMap = taxiMap;
	}
	
	public boolean readLightFileData(){
		//@REQUIRES:
		//@MODIFIES: taxiMap
		//@EFFECTS: (lightFile.exists && lightFile is valid) ==> lightFile is read in taxiMap && \result <==> true;
		//@			(!flightFileile.exists || lightFile is invalid) ==> \result <==> false;
		int charJ = 0, charI = 0;
		
		if(lightFile.exists()==false){
			System.out.println("Non-existent light file. The process will be closed.");
			System.exit(1);
			return false;
		}
		Scanner scanner = null;
		try {
			scanner = new Scanner(lightFile);
		} catch (FileNotFoundException e) {
			System.out.println("We've got a problem in the light file. The process will be closed.");
			System.exit(1);
		}
		for(int i=0; i<TaxiMap.Y; i++){
			charJ = 0;
			String stringLine = null;
			try{
				stringLine = scanner.nextLine();
			}catch(Exception e){
				System.out.println("We've got a problem in the light file. The process will be closed.");
				System.exit(1);
			}
			char[] tempchars = stringLine.toCharArray();
			for (char c : tempchars){
				switch (c) {
				case ' ':
				case '\t':
					break;
				case '0':
				case '1':
					if (charJ == 0) { // --- a new line
						if (charI >= 80) {
							InfoHandle.error("The line number out of range.");
							return false;
						}
						charI ++;
					}else if (charJ >= 80) {
						InfoHandle.error(charJ + "characters found in line " + charI + ".");
						return false;
					}
					charJ ++;
					if (c == '1'){
						taxiMap.setLight(charI, charJ);
					}
					break;

				default:
					InfoHandle.error("Unexpected character " + c + ".");
					return false;
				}
			}
		}
		scanner.close();
		if (!(charJ == TaxiMap.X && charI == TaxiMap.Y)){
			InfoHandle.error("Unexpected end.");
			return false;
		}
		return true;
	}
	
	
	public boolean readMapFileData(){
		//@REQUIRES:
		//@MODIFIES: taxiMap
		//@EFFECTS: 
		//@			(mapFile.exists && mapFile is valid) ==> mapFile is read in taxiMap && \result <==> true;
		//@			(!mapFile.exists || mapFile is invalid) ==> \result <==> false;
		int charJ = 0, charI = 0;
		
		if(mapFile.exists()==false){
			System.out.println("Non-existent map file. The process will be closed.");
			System.exit(1);
			return false;
		}
		Scanner scanner = null;
		try {
			scanner = new Scanner(mapFile);
		} catch (FileNotFoundException e) {
			System.out.println("We've got a problem in the map file. The process will be closed.");
			System.exit(1);
		}
		for(int i=0; i<TaxiMap.Y; i++){
			charJ = 0;
			String stringLine = null;
			try{
				stringLine = scanner.nextLine();
			}catch(Exception e){
				System.out.println("We've got a problem in the map file. The process will be closed.");
				System.exit(1);
			}
			char[] tempchars = stringLine.toCharArray();
			for (char c : tempchars){
				switch (c) {
				case ' ':
				case '\t':
					break;
				case '0':
				case '1':
				case '2':
				case '3':
					if (charJ == 0) { // --- a new line
						if (charI >= 80) {
							InfoHandle.error("The line number out of range.");
							return false;
						}
						charI ++;
					}else if (charJ >= 80) {
						InfoHandle.error(charJ + "characters found in line " + charI + ".");
						return false;
					}
					charJ ++;
					taxiMap.setEdge(charI, charJ, c - '0');
					break;

				default:
					InfoHandle.error("Unexpected character " + c + ".");
					return false;
				}
			}
		}
		scanner.close();
		if (!(charJ == TaxiMap.X && charI == TaxiMap.Y)){
			InfoHandle.error("Unexpected end.");
			return false;
		}
		return true;
	}
}
