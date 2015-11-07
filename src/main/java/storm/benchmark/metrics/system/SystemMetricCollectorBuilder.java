package storm.benchmark.metrics.system;

public final class SystemMetricCollectorBuilder{
	
	public static Thread build(
			int interval,
			int duration){
		Thread t = new Thread(
				new SystemMetricCollector(interval, duration));
		t.setDaemon(true);
		return t;
	}
	
	public static Thread build(
			int interval,
			int duration,
			String output){
		Thread t = new Thread(
				new SystemMetricCollector(interval, duration, output));
		t.setDaemon(true);
		return t;
	}
	
	private SystemMetricCollectorBuilder(){}

}
