package ooProject07;

public class ERrequest {
	/*@Overview: 
	 * ERrequest是一个ER请求。
	 */
	final private int nodeI;
	final private int nodeJ;
	final private EdgeDirection edgeDirection;
	
	//不变式	c.nodeI > 0 && c.nodeJ > 0 &&
	//		(c.edgeDirection == EdgeDirection.HORIZONAL && c.nodeI <= TaxiMap.Y && c.nodeJ < TaxiMap.X ||
	//		c.edgeDirection == EdgeDirection.VERTICAL && c.nodeI < TaxiMap.Y && c.nodeJ <= TaxiMap.X)	
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (
			nodeI > 0 && nodeJ > 0 &&
			(edgeDirection == EdgeDirection.HORIZONAL && nodeI <= TaxiMap.Y && nodeJ < TaxiMap.X ||
			edgeDirection == EdgeDirection.VERTICAL && nodeI < TaxiMap.Y && nodeJ <= TaxiMap.X)	
		);
	}
	
	public ERrequest(int nodeI, int nodeJ, EdgeDirection edgeDirection) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.nodeI; this.nodeJ; this.edgeDirection;
		//@EFFECTS: 创建了一个新的ERrequest对象并初始化;
		this.nodeI = nodeI;
		this.nodeJ = nodeJ;
		this.edgeDirection = edgeDirection;
	}
	
	public int getNodeI() {
		//@REQUIRES: 
		//@MODIFIES: None;
		//@EFFECTS: \result == nodeI;
		return nodeI;
	}
	
	public int getNodeJ() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == nodeJ;
		return nodeJ;
	}
	
	public EdgeDirection getEdgeDirection() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == edgeDirection;
		return edgeDirection;
	}
	
	public boolean equals(ERrequest er) {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (er == null) ==> \result <==> false;
		//@			(er != null) ==> \result <==> (this.nodeI == er.nodeI && this.nodeJ == er.nodeJ && this.edgeDirection == er.edgeDirection);
		if (er == null){
			return false;
		}
		return (this.nodeI == er.nodeI && this.nodeJ == er.nodeJ && this.edgeDirection == er.edgeDirection);
	}
}
