package ooProject07;

import java.util.ArrayList;

public class TaxiMap{
	/*@Overview:
	 * TaxiMap是地图，它以二维数组的形式存储了其节点、水平边和垂直边。
	 */
	final static public int X = 80;
	final static public int Y = 80;
	final static private long REFRESH_FLOWRATE_MILL = 200;
	final private MapNode[][] mapNodes = new MapNode[Y+1][X+1];
	final private MapEdge[][] horMapEdges = new MapEdge[Y+1][X];
	final private MapEdge[][] verMapEdges = new MapEdge[Y][X+1];
	
	//不变式	c.mapNodes!=null && c.horMapEdges!=null && c.verMapEdges!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (mapNodes!=null && horMapEdges!=null && verMapEdges!=null);
	}
	
	public TaxiMap() {
		//@REQUIRES: 
		//@MODIFIES: mapNodes; horMapEdges; verMapEdges;
		//@EFFECTS: 创建了一个新的TaxiMap对象并初始化;
		//@			建立地图上的各个节点和各个边;
		for (int i=1; i<=Y; i++){
			for (int j=1; j<=X; j++){
				this.mapNodes[i][j] = new MapNode(i, j);
			}
		}
		for (int i=1; i<=Y; i++){
			for (int j=1; j<X; j++){
				this.horMapEdges[i][j] = new MapEdge(this.mapNodes[i][j], this.mapNodes[i][j+1], EdgeDirection.HORIZONAL);
			}
		}
		for (int i=1; i<Y; i++){
			for (int j=1; j<=X; j++){
				this.verMapEdges[i][j] = new MapEdge(this.mapNodes[i][j], this.mapNodes[i+1][j], EdgeDirection.VERTICAL);
			}
		}
		for (int i=1; i<=Y; i++){
			for (int j=1; j<=X; j++){
				this.mapNodes[i][j].setEdge(Direction.LEFT, (j > 1) ? this.horMapEdges[i][j-1] : null);
				this.mapNodes[i][j].setEdge(Direction.RIGHT, (j < X) ? this.horMapEdges[i][j] : null);
				this.mapNodes[i][j].setEdge(Direction.UP, (i > 1) ? this.verMapEdges[i-1][j] : null);
				this.mapNodes[i][j].setEdge(Direction.DOWN, (i < Y) ? this.verMapEdges[i][j] : null);
			}
		}
	}
	
	public ArrayList<MapNode> getAreaNodes(MapNode center, int size){
		//@REQUIRES: center != null; size >= 0;
		//@MODIFIES: None;
		//@EFFECTS: (\all MapNode n; mapNodes.contains(n) && n is in the (2size) * (2size) area; \result.contains(n));
		int centerI = center.getI();
		int centerJ = center.getJ();
		ArrayList<MapNode> nodes = new ArrayList<MapNode>();
		for (int i=centerI-size; i<=centerI+size; i++){
			if (i < 1 || i > Y){
				continue;
			}
			for (int j=centerJ-size; j<=centerJ+size; j++){
				if (j < 1 || j > X){
					continue;
				}
				nodes.add(mapNodes[i][j]);
			}
		}
		return nodes;
	}
	
	public void reverseEdge(ERrequest er){
		//@REQUIRES: er != null;
		//@MODIFIES: horMapEdges; verMapEdges;
		//@EFFECTS:	(er.edgeDirection == HORIZONAL) ==> horMapEdges[er.i][er.j] is reversed;
		//@			(er.edgeDirection == VERTICAL) ==> verMapEdges[er.i][er.j] is reversed;
		if (er.getEdgeDirection() == EdgeDirection.HORIZONAL){
			horMapEdges[er.getNodeI()][er.getNodeJ()].reverse();
		}else if (er.getEdgeDirection() == EdgeDirection.VERTICAL){
			verMapEdges[er.getNodeI()][er.getNodeJ()].reverse();
		}
	}
	
	public void setEdge(int i, int j, int setCode){
		//@REQUIRES: setCode==0,1,2或3 && i和j为合法的节点坐标
		//@MODIFIES: horMapEdges; verMapEdges;
		//@EFFECTS: 根据setCode的不同，从节点(i,j)处向下或向右建边;
		switch (setCode) {
		case 0:
			break;
		case 1: // right
			horMapEdges[i][j].build();
			break;
		case 2: // down
			verMapEdges[i][j].build();
			break;
		case 3:
			horMapEdges[i][j].build();
			verMapEdges[i][j].build();
			break;
		default:
			assert false : "Unknown setCode.";
			break;
		}
	}
	
	public void setLight(int i, int j){
		//@REQUIRES: i, j are valid coordinates && edges are set;
		//@MODIFIES: mapNodes;
		//@EFFECTS: ((i, j) is crossing) ==> light is built at (i,j);
		if (mapNodes[i][j].isCrossing()){
			mapNodes[i][j].buildTrafficLight();
		}
	}
	
	public MapNode getNode(int i, int j){
		//@REQUIRES: i和j为合法的节点坐标;
		//@MODIFIES: None;
		//@EFFECTS: \result == mapNodes[i][j];
		return this.mapNodes[i][j];
	}
	
	public MapEdge getEdge(int i, int j, EdgeDirection edgeDirection){
		//@REQUIRES: (edgeDirection==HORIZON && 1<i<=Y && 1<j<X || edgeDirection==VERTICAL && 1<i<Y && 1<j<=X);
		//@MODIFIES: None
		//@EFFECTS: (edgeDirection==HORIZON) ==> \result <==> horMapEdges[i][j];
		//			(edgeDirection==VERTICAL) ==> \result <==> verMapEdges[i][j];
		if (edgeDirection == EdgeDirection.HORIZONAL){
			return horMapEdges[i][j];
		}else if (edgeDirection == EdgeDirection.VERTICAL){
			return verMapEdges[i][j];
		}else {
			return null;
		}
	}
}
