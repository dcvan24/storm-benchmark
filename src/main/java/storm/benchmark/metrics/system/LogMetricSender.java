package storm.benchmark.metrics.system;

import java.util.Map;

import org.apache.log4j.Logger;

public final class LogMetricSender 
	implements SystemMetricSender{
	
	private static final Logger LOG = Logger.getLogger(LogMetricSender.class);
	
	@Override
	public void send(
			Map<String, Object> metrics){
		LOG.info(metrics);
	}

	@Override
	public void close(){}

}
