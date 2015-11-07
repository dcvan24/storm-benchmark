package storm.benchmark.metrics.system;

import java.util.Timer;

public final class SystemMetricCollector 
	implements Runnable{
	
	private final int interval;
	private final int duration;
	private SystemMetricSender sender =
			new LogMetricSender();
	
	public SystemMetricCollector(
			int interval,
			int duration){
		this.interval = interval;
		this.duration = duration;
	}
	
	public SystemMetricCollector(
			int interval,
			int duration,
			String output){
		this(interval, duration);
		sender = new PrintMetricSender(output);
	}
	
	@Override
	public void run(){
		try{
			Timer timer = new Timer(true);
			timer.scheduleAtFixedRate(
					new SystemMetricCollectionTask(sender), 
					0, 
					interval);
			Thread.sleep(duration + 500);
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally{
			sender.close();
		}
	}

}
