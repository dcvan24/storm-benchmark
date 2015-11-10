package storm.benchmark.metrics.system;

import static storm.benchmark.metrics.system.SystemMetricCollectionTask.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.log4j.Logger;

public final class PrintMetricSender 
	implements SystemMetricSender{
	
	private static final Logger LOG = Logger.getLogger(PrintMetricSender.class);
	
	private PrintWriter writer;
	
	public PrintMetricSender(
			String output){
		try{
			writer = new PrintWriter(
					new File(output));
		}catch(FileNotFoundException e){
			LOG.error(null, e);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void send(
			Map<String, Object> metrics){
		Map<String, Object> diskMetrics = (Map<String, Object>)metrics.get(DISK),
											 netMetrics = (Map<String, Object>)metrics.get(NET);
		writer.printf("%.3f, %.3f, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d\n", 
				(Double)metrics.get(CPU),
				(Double)metrics.get(MEM),
				(Long)metrics.get(BUFFER_CACHED),
				(Long)diskMetrics.get(READS),
				(Long)diskMetrics.get(READ_BYTES),
				(Long)diskMetrics.get(WRITES),
				(Long)diskMetrics.get(WRITE_BYTES),
				(Long)netMetrics.get(TX_PACKETS),
				(Long)netMetrics.get(TX_BYTES),
				(Long)netMetrics.get(TX_DROPPED),
				(Long)netMetrics.get(RX_PACKETS),
				(Long)netMetrics.get(RX_BYTES),
				(Long)netMetrics.get(RX_DROPPED));
		writer.flush();
	}

	@Override
	public void close(){
		writer.close();
	}

}
