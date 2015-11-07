package storm.benchmark.metrics.system;

import java.util.Map;

public interface SystemMetricSender{

	public void send(
			Map<String, Object> metrics);
	
	public void close();
	
}
