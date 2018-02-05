package ooProject07;

import java.util.ArrayList;
import java.util.LinkedList;

public class Share implements Runnable{
	/*@Overview:
	 * Share类是一个负责接收、储存和处理请求的类，它分别有序存储着“未激活CR请求”、“已激活CR请求”和“未处理ER请求”。
	 */
	final static long INTERVAL_TIMEMILL = 100;
	final static long CR_LIFE_TIMEMILL = 3000;
	final static int ER_MAX = 5;
	final private ArrayList<CRrequest> unactivatedCRList = new ArrayList<CRrequest>();
	final private LinkedList<CRrequest> activatedCRList = new LinkedList<CRrequest>();
	final private ArrayList<ERrequest> erList = new ArrayList<ERrequest>();
	final private TaxiMap taxiMap;
	private Taxi[] taxis = null;
	final private OutputHandle outputHandle;
	final private long startingTimeMill;
	private long timeMill;
	
	//不变式	c.unactivatedCRList!=null && c.activatedCRList!=null && c.erList!=null && c.taxiMap!=null &&
	//		c.outputHandle!=null && c.startingTimeMill>=0 && c.timeMill>=0;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (unactivatedCRList!=null && activatedCRList!=null && erList!=null && taxiMap!=null &&
				outputHandle!=null && startingTimeMill>=0 && timeMill>=0);
	}
	
	public Share(long timeMill, TaxiMap taxiMap, OutputHandle outputHandle) {
		//@REQUIRES: 所有参数一律不为null; timeMill >= 0;
		//@MODIFIES: this.timeMill; this.taxiMap; this.taxis; this.outputHandle;
		//@EFFECTS: 创建了一个新的Share对象并初始化;
		this.startingTimeMill = timeMill;
		this.taxiMap = taxiMap;
		this.outputHandle = outputHandle;
		this.timeMill = 0;
	}
	
	public void setTaxis(Taxi[] taxis){
		//@REQUIRES: taxis != null;
		//@MODIFIES: this.taxis;
		//@EFFECTS: this.taxis == taxis;
		this.taxis = taxis;
	}
	
	synchronized public void storeER (ERrequest er) {
		//@REQUIRES: er != null;
		//@MODIFIES: erList
		//@EFFECTS: (size < ER_MAX && (\all ERrequest r; \old(contains(r)); !equals(er)))
		//@			==> contains(er) && size == \old(size) + 1; 
		//@THREAD_EFFECTS: \locked();
		if (erList.size() >= ER_MAX){
			InfoHandle.info("The number of ERrequest should be less than or equal to 5 in the same moment!");
			return;
		}
		for (ERrequest r : erList) {
			if (r.equals(er)){
				InfoHandle.info("Same ERrequest!");
				return;
			}
		}
		erList.add(er);
	}
	
	synchronized public void storeCR (CRrequest cr) {
		//@REQUIRES: cr != null;
		//@MODIFIES: unactivatedCRList;
		//@EFFECTS: (\all CRrequest r; \old(contains(r); !equals(cr)))
		//@			==> contains(cr) && size == \old(size) + 1; 
		//@THREAD_EFFECTS: \locked();
		for (CRrequest r : unactivatedCRList){
			if (r.equals(cr)){
				InfoHandle.info("Same CRrequest!");
				return;
			}
		}
		unactivatedCRList.add(cr);
	}
	
	synchronized public void activateCRs () {
		//@REQUIRES:
		//@MODIFIES: unactivatedCRList; activatedCRList; outputHandle;
		//@EFFECTS: (\all CRrequest r; \old(unactivatedCRList.contains(r)); 
		//@			information about r is recorded into outputHandle &&
		//@			r is activated &&
		//@			!unactivatedCRList.contains(r) &&
		//@			activatedCRList.contains(r));
		//@			unactivatedCRList.size == 0;
		//@THREAD_EFFECTS: \locked();
		for (CRrequest cr : unactivatedCRList){
			outputHandle.createData(cr);
			outputHandle.writeLine(cr, "<< Taxis' Information >>");
			ArrayList<MapNode> nodes = taxiMap.getAreaNodes(cr.getStartingNode(), 2);
			for (int i=0; i<taxis.length; i++){
				for (MapNode node : nodes){
					if (taxis[i].getPositionNode() == node){
						outputHandle.writeLine(cr, taxis[i].toString()); // --- record taxi information
						break;
					}
				}
			}
			cr.activate();
			activatedCRList.addLast(cr);
		}
		unactivatedCRList.clear();
	}
	
	synchronized public void executeERs(){
		//@REQUIRES: 
		//@MODIFIES: taxis; erList; taxiMap;
		//@EFFECTS:	(\all int i; 0<=i<min(ER_MAX, \old(erList.size)); the edge in taxiMap is reversed);
		//@			!\old(erList.empty) ==> (\all int i; 0<=i<\old(taxis.length); taxis[i].toChangeWay);
		//@			(\all ERrequest r; \old(erList.contains(r)); !erList.contains(r));
		//@			erList.size == 0;
		//@THREAD_EFFECTS: \locked();
		for (int i=0; i<Math.min(ER_MAX, erList.size()); i++){
			ERrequest eRrequest = erList.get(i);
			int nodeI = eRrequest.getNodeI();
			int nodeJ = eRrequest.getNodeJ();
			EdgeDirection edgeDirection = eRrequest.getEdgeDirection();
			MapEdge mapEdge = taxiMap.getEdge(nodeI, nodeJ, edgeDirection);
			mapEdge.reverse();
		}
		if (!erList.isEmpty()){
			for (Taxi taxi : taxis){
				taxi.changeWay();
			}
		}
		erList.clear();
	}
	
	synchronized public void killCRs(){
		//@REQUIRES: sysTime >= 0;
		//@MODIFIES: activatedCRList;
		//@EFFECTS:	(\all CRrequest r; \old(contains(r)) && r.activatingTimeMill <= sysTime - CR_LIFE_TIMEMILL; 
		//@			r is killed && !contains(r))
		//@THREAD_EFFECTS: \locked();
		while (true){
			if (activatedCRList.isEmpty()){
				break;
			}
			CRrequest cr = activatedCRList.getFirst();
			if (cr.getActivatingTimeMill() <= this.timeMill + this.startingTimeMill - CR_LIFE_TIMEMILL){
				cr.kill();
				activatedCRList.removeFirst();
			}else {
				break; // --- all killed
			}
		}
	}
	
	@Override
	public void run() {
		//@REQUIRES:
		//@MODIFIES: timeMill; taxis; erList; taxiMap; unactivatedCRList; activatedCRList; outputHandle;
		//@EFFECTS:	every INTERVAL_TIMEMILL:
		//			it cannot stop and it keeps dealing with requests
		while(true){
			if(!repOK()){
				System.out.println(110);
				System.exit(0);
			}
			long sleepMill = this.timeMill + INTERVAL_TIMEMILL - (System.currentTimeMillis() - this.startingTimeMill);
			try {
				Thread.sleep(sleepMill);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				//InfoHandle.error("Share timeout: " + sleepMill);
			}
			this.timeMill += INTERVAL_TIMEMILL;
			activateCRs();
			killCRs();
			executeERs();
		}
	}
}
