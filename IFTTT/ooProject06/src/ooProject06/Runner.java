package ooProject06;

public class Runner {
	public static void main(String[] args) {
		FileShare fileShare = new FileShare();
		InputHandle iHandle = new InputHandle(fileShare);
		iHandle.inputTask();
		new TestThread(fileShare).start();
	}
}
