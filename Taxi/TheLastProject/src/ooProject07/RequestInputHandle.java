package ooProject07;

import java.awt.Point;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestInputHandle extends Thread{
	/*@Overview:
	 * RequestInputHandle是一个捕捉控制台输入，并将其根据标识符制作为相应请求的类。
	 */
	final static Pattern CR_PTN = Pattern.compile("^\\[CR,\\([+]?(?<startingI>\\d+),[+]?(?<startingJ>\\d+)\\),\\([+]?(?<terminalI>\\d+),[+]?(?<terminalJ>\\d+)\\)\\]$");
	final static Pattern ER_PTN = Pattern.compile("^\\[ER,\\([+]?(?<nodeI>\\d+),[+]?(?<nodeJ>\\d+)\\),(?<dir>RIGHT|DOWN)\\]");
	final static int X = TaxiMap.X;
	final static int Y = TaxiMap.Y;
	final private TaxiMap taxiMap;
	final private Share share;
	final private OutputHandle outputHandle;
	final private TaxiGUI taxiGUI;
	
	//不变式	c.taxiMap!=null && c.share!=null && c.outputHandle!=null && c.taxiGUI!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (taxiMap!=null && share!=null && outputHandle!=null && taxiGUI!=null);
	}
	
	public RequestInputHandle(TaxiMap taxiMap, Share share, OutputHandle outputHandle, TaxiGUI taxiGUI) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.taxiMap; this.share; this.outputHandle; this.taxiGUI; this.timeLock;
		//@EFFECTS: 创建了一个新的RequestInputHandle对象并初始化;
		this.taxiMap = taxiMap;
		this.share = share;
		this.outputHandle = outputHandle;
		this.taxiGUI = taxiGUI;
	}
	
	public void makeERrequest(int nodeI, int nodeJ, EdgeDirection edgeDirection){
		//@REQUIRES:
		//@MODIFIES: share;
		//@EFFECTS: (nodeI > 0 && nodeJ > 0 &&
		//@			(edgeDirection == EdgeDirection.HORIZONAL && nodeI <= TaxiMap.Y && nodeJ < TaxiMap.X ||
		//@			edgeDirection == EdgeDirection.VERTICAL && nodeI < TaxiMap.Y && nodeJ <= TaxiMap.X)	)
		//@			==> store <new ER>;
		if (
			nodeI > 0 && nodeJ > 0 &&
			(edgeDirection == EdgeDirection.HORIZONAL && nodeI <= TaxiMap.Y && nodeJ < TaxiMap.X ||
			edgeDirection == EdgeDirection.VERTICAL && nodeI < TaxiMap.Y && nodeJ <= TaxiMap.X)	
		){
			share.storeER(new ERrequest(nodeI, nodeJ, edgeDirection));
		}else {
			InfoHandle.error("Invalid ERrequest!");
		}
	}
	
	public void makeCRrequest(int startingI, int startingJ, int terminalI, int terminalJ){
		//@REQUIRES:
		//@MODIFIES: share;
		//@EFFECTS: (startingI > 0 && startingI <= TaxiMap.Y &&
		//@			startingJ > 0 && startingJ <= TaxiMap.X &&
		//@			terminalI > 0 && terminalI <= TaxiMap.Y &&
		//@			terminalJ > 0 && terminalJ <= TaxiMap.X &&
		//@			startingI == terminalI && startingJ == terminalJ)
		//@			==> store <new CR>;
		if (
			startingI > 0 && startingI <= TaxiMap.Y &&
			startingJ > 0 && startingJ <= TaxiMap.X &&
			terminalI > 0 && terminalI <= TaxiMap.Y &&
			terminalJ > 0 && terminalJ <= TaxiMap.X
		){
			if (startingI == terminalI && startingJ == terminalJ){
				InfoHandle.info("It's here!");
				return;
			}
			MapNode startingNode = taxiMap.getNode(startingI, startingJ);
			MapNode terminalNode = taxiMap.getNode(terminalI, terminalJ);
			share.storeCR(new CRrequest((System.currentTimeMillis()/100)*100, startingNode, terminalNode, taxiMap, outputHandle));
			if (Runner.SHOW_CR_GUI){
				taxiGUI.RequestTaxi(new Point(startingI-1, startingJ-1), new Point(terminalI-1, terminalJ-1));
			}
		}else {
			InfoHandle.error("Invalid position.");
		}
	}
	
	@Override
	public void run() {
		//@REQUIRES:
		//@MODIFIES: share;
		//@EFFECTS:	(CR_PTN.matcher) ==> CRrequest made;
		//@			(ER_PTN.matcher) ==> ERrequest made;
		Scanner scanner = new Scanner(System.in);
		while(true){
			String string = scanner.nextLine().replaceAll(" ", "");
			Matcher mCR = CR_PTN.matcher(string);
			Matcher mER = ER_PTN.matcher(string);
			if (mCR.matches()){
				int startingI = Integer.parseInt(mCR.group("startingI"));
				int startingJ = Integer.parseInt(mCR.group("startingJ"));
				int terminalI = Integer.parseInt(mCR.group("terminalI"));
				int terminalJ = Integer.parseInt(mCR.group("terminalJ"));
				makeCRrequest(startingI, startingJ, terminalI, terminalJ);
			}else if (mER.matches()){
				int nodeI = Integer.parseInt(mER.group("nodeI"));
				int nodeJ = Integer.parseInt(mER.group("nodeJ"));
				String dir = mER.group("dir");
				if (dir.equals("RIGHT")){
					makeERrequest(nodeI, nodeJ, EdgeDirection.HORIZONAL);
				}else if (dir.equals("DOWN")){
					makeERrequest(nodeI, nodeJ, EdgeDirection.VERTICAL);
				}
			}else {
				InfoHandle.error("Invalid request!");
			}
		}
	}
}
