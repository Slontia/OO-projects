package ooProject07;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.xml.soap.Node;

public class TestThread extends Thread{
	/*@Overview:
	 * TestThread是一个测试线程，由于此部分由测试者书写，所以不存在不变式。
	 */
	final private Share share;
	final private TaxiMap taxiMap;
	final private Taxi[] taxis;
	final private OutputHandle outputHandle;
	final private RequestInputHandle requestInputHandle;
	
	public TestThread(Share share, TaxiMap taxiMap, Taxi[] taxis, OutputHandle outputHandle, RequestInputHandle requestInputHandle) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.timeLock; this.share; this.taxiMap; this.taxis; this.outputHandle; this.requestInputHandle;
		//@EFFECTS: 创建了一个新的TestThread对象并初始化;
		//this.timeLock = timeLock;
		this.share = share;
		this.taxiMap = taxiMap;
		this.taxis = taxis;
		this.outputHandle = outputHandle;
		this.requestInputHandle = requestInputHandle;
	}
	
	@Override
	public void run() {
		/* Your code here */

	}
	
	private void putCRrequest(int startingI, int startingJ, int terminalI, int terminalJ){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: a new CRrequest is made;
		requestInputHandle.makeCRrequest(startingI, startingJ, terminalI, terminalJ);
	}
	
	private void showTaxiInfo(int no){
		//@REQUIRES: 0 <= no < taxis.length;
		//@MODIFIES: None;
		//@EFFECTS: 输出系统时间，输出对应出租车的信息;
		showSysMill();
		InfoHandle.test("Taxi " + no + " Information:\n" + taxis[no].toString());
	}
	
	private void showStateTaxis(TaxiState taxiState){
		//@REQUIRES: taxiState != null;
		//@MODIFIES: None;
		//@EFFECTS: 输出系统时间，并输出所有处于taxiState状态的出租车编号;
		showSysMill();
		for (int i=0; i<taxis.length; i++){
			if (taxiState == taxis[i].getTaxiState()){
				InfoHandle.test(taxiState + ": " + i);
			}
		}
	}
	
	private ArrayList<Taxi> getStateTaxis(TaxiState taxiState){
		//@REQUIRES: taxiState != null;
		//@MODIFIES: None;
		//@EFFECTS: (\all int i; 0<=i<taxis.length && taxis[i].state==taxiState; \result.contains(t));
		ArrayList<Taxi> stateTaxis = new ArrayList<Taxi>();
		for (int i=0; i<taxis.length; i++){
			if (taxiState == taxis[i].getTaxiState()){
				stateTaxis.add(taxis[i]);
			}
		}
		return stateTaxis;
	}
	
	private void showSysMill(){
		//@REQUIRES: 
		//@MODIFIES: None;
		//@EFFECTS: 输出系统时间;
		InfoHandle.test("System Time: " + System.currentTimeMillis());
	}
}
