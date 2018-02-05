package ooProject06;

public class InfoHandle {
	static void error(String text){
		// error information
		System.out.println("[ERROR] " + text + ":");
		// location
		System.out.println("\tat " + new Exception().getStackTrace()[1].getMethodName() + " in " + new Exception().getStackTrace()[1].getClassName());
		// exit
		System.exit(0);
	}
	
	static void printInfo(String info){
		System.out.println("[INFO] " + info);
	}
	
	static void test(String test){
		System.out.println("[TEST] " + test);
	}
}
