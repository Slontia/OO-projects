package ooProject07;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Runner {
	/*@Overview:
	 * Runner是程序的入口。
	 */
	final static public int TRACEABLE_TAXI_NUM = 30;
	final static public int TAXI_NUM = 100;
	final static Random random = new Random();
	final static public TaxiGUI gui = new TaxiGUI();
	final static public boolean SHOW_CR_GUI = false;
	
	public static void main(String[] args) {
		//@REQUIRES: 
		//@MODIFIES: None;
		//@EFFECTS:	gui set;
		//@			threads started;
		
		// ===== Set GUI =====
		mapInfo mi=new mapInfo();
		mi.readmap("map.txt");
		gui.LoadMap(mi.map, 80);
		
		// ===== Initial =====
		OutputHandle outputHandle = new OutputHandle();
		TaxiMap taxiMap = new TaxiMap();
		InputHandle inputHandle = new InputHandle(taxiMap);
		inputHandle.readMapFileData();
		inputHandle.readLightFileData();
		long timeMill = System.currentTimeMillis();
		Share share = new Share(timeMill, taxiMap, outputHandle);
		Taxi[] taxis = initTaxis(taxiMap, timeMill, share, outputHandle);
		share.setTaxis(taxis);
		RequestInputHandle requestInputHandle = new RequestInputHandle(taxiMap, share, outputHandle, gui);
		
		// ===== Run Threads =====
		new Thread(share).start();
		requestInputHandle.start();
		for (int i=0; i<TAXI_NUM; i++){
			taxis[i].start();
		}
		
		// ===== Test =====
		new TestThread(share, taxiMap, taxis, outputHandle, requestInputHandle).start();
	}
	
	static private Taxi[] initTaxis(TaxiMap taxiMap, long timeMill, Share share, OutputHandle outputHandle){
		//@REQUIRES: 所有参数不为null;
		//@MODIFIES: None;
		//@EFFECTS: \result中含有30个可追踪出租车和70个普通出租车;
		Taxi[] taxis = new Taxi[TAXI_NUM];
		for (int i=0; i<TRACEABLE_TAXI_NUM; i++){
			taxis[i] = new TraceableTaxi(i, taxiMap, timeMill, share, random, gui, outputHandle);
			gui.SetTaxiType(i, 1);
		}
		for (int i=TRACEABLE_TAXI_NUM; i<TAXI_NUM; i++){
			taxis[i] = new Taxi(i, taxiMap, timeMill, share, random, gui, outputHandle);
			gui.SetTaxiType(i, 0);
		}
		return taxis;
	}
}
