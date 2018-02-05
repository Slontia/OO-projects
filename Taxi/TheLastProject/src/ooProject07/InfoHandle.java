package ooProject07;

abstract public class InfoHandle {
	/*@Overview: 
	 * InfoHandle是一个输出信息的接口类。
	 */
	
	static public void error(String msg){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: ("[ERROR] " + msg) is printed;
		System.out.println("[" + gv.getFormatTime() + "]" + "[ERROR] " + msg);
	}
	
	static public void info(String msg) {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: ("[INFO] " + msg) is printed;
		System.out.println("[" + gv.getFormatTime() + "]" + "[INFO] " + msg);
	}
	
	static public void test(String msg) {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: ("[TEST] " + msg) is printed;
		System.out.println("[" + gv.getFormatTime() + "]" + "[TEST] " + msg);
	}
}
