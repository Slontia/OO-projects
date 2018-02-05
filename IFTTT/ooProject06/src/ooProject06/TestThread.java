package ooProject06;

import java.io.File;

public class TestThread extends Thread{
	private FileShare fileShare;
	
	public TestThread(FileShare fileShare) {
		this.fileShare = fileShare;
	}
	
	@Override
	public void run() {
		fileShare.createDir(new File("D:\\人呐就都不知道\\自己就不可以预料\\一个人的命运啊\\当然要靠自我奋斗\\但是也要考虑到历史的行程\\我绝对不知道\\我作为一个上海市委书记怎么把我选到北京去了\\所以邓小平同志跟我讲话\\说\\中央都决定啦，你来当总书记\\我说另请高明吧\\我实在我也不是谦虚\\我一个上海市委书记怎么到北京来了呢\\但是呢\\小平同志讲\\大家已经研究决定了\\所以后来我就念了两首诗\\叫\\苟利国家生死以\\岂因祸福避趋之\\那麼所以我就到了北京"));
		/* Write your test code! */
	}
}
