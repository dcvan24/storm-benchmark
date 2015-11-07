package storm.benchmark.metrics.system;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.google.common.collect.Maps;

public final class SystemMetricCollectionTask 
	extends TimerTask{

	public static final String CPU = "cpu",
												  MEM = "memory",
												  DISK = "disk",
												  READS = "reads",
												  WRITES = "writes",
												  READ_BYTES = "readBytes",
												  WRITE_BYTES = "writeBytes",
												  NET = "network",
												  TX_BYTES = "txBytes",
												  TX_PACKETS = "txPackets",
												  TX_DROPPED = "txDropped",
												  RX_BYTES = "rxBytes",
												  RX_PACKETS = "rxPackets",
												  RX_DROPPED = "rxDropped";
												  
	private static final String FS = "/",
													IFACE_NAME = "eth0";
	
	private final Sigar sigar;
	private Map<String, Object> metrics = 
			new HashMap<String, Object>();
	private final SystemMetricSender sender;
	private final Map<String, Object> prevDiskMetrics = new HashMap<String, Object>(),
																  prevNetMetrics = new HashMap<String, Object>(),
																  diskMetrics = new HashMap<String, Object>(),
										 						  netMetrics = new HashMap<String, Object>();
	
	public SystemMetricCollectionTask(
			SystemMetricSender sender){
		sigar = new Sigar();
		this.sender = sender;
		init();
	}

	@Override
	public void run(){
		try{
			collectCpuMetrics();
			collectMemoryMetrics();
			collectDiskMetrics();
			collectNetworkMetrics();
		}catch(SigarException e){
			e.printStackTrace();
		}
		sender.send(Maps.newHashMap(metrics));
	}
	
	private void init(){
		try{
			initDiskMetrics();
			initNetworkMetrics();
		}catch(SigarException e){
			e.printStackTrace();
		}
	}
	
	private void collectCpuMetrics() 
			throws SigarException{
		CpuPerc cpu = sigar.getCpuPerc();
		metrics.put(CPU, cpu.getCombined());
	}

	private void collectMemoryMetrics() 
			throws SigarException{
		Mem mem = sigar.getMem();
		metrics.put(MEM, mem.getUsedPercent());
	}
	
	private void initDiskMetrics() 
			throws SigarException{
		FileSystemUsage disk = sigar.getFileSystemUsage(FS);
		prevDiskMetrics.put(READS, disk.getDiskReads());
		prevDiskMetrics.put(WRITES, disk.getDiskWrites());
		prevDiskMetrics.put(READ_BYTES, disk.getDiskReadBytes());
		prevDiskMetrics.put(WRITE_BYTES, disk.getDiskWriteBytes());
	}
	
	private void collectDiskMetrics() 
			throws SigarException{
		FileSystemUsage disk = sigar.getFileSystemUsage(FS);
		long curReads = disk.getDiskReads(),
				 curWrites = disk.getDiskWrites(),
				 curReadBytes = disk.getDiskReadBytes(),
				 curWriteBytes = disk.getDiskWriteBytes();
		
		diskMetrics.put(READS, curReads - (Long)prevDiskMetrics.get(READS));
		diskMetrics.put(WRITES, curWrites - (Long)prevDiskMetrics.get(WRITES));
		diskMetrics.put(READ_BYTES, curReadBytes - (Long)prevDiskMetrics.get(READ_BYTES));
		diskMetrics.put(WRITE_BYTES, curWriteBytes - (Long)prevDiskMetrics.get(WRITE_BYTES));
		
		prevDiskMetrics.put(READS, curReads);
		prevDiskMetrics.put(WRITES, curWrites);
		prevDiskMetrics.put(READ_BYTES, curReadBytes);
		prevDiskMetrics.put(WRITE_BYTES, curWriteBytes);
		
		metrics.put(DISK, Maps.newHashMap(diskMetrics));
	}
	
	private void initNetworkMetrics() 
			throws SigarException{
		NetInterfaceStat net = sigar.getNetInterfaceStat(IFACE_NAME);
		prevNetMetrics.put(TX_BYTES, net.getTxBytes());
		prevNetMetrics.put(RX_BYTES, net.getRxBytes());
		prevNetMetrics.put(TX_PACKETS, net.getTxPackets());
		prevNetMetrics.put(RX_PACKETS, net.getRxPackets());
		prevNetMetrics.put(TX_DROPPED, net.getTxDropped());
		prevNetMetrics.put(RX_DROPPED, net.getRxDropped());
	}
	
	private void collectNetworkMetrics() 
			throws SigarException{
		NetInterfaceStat net = sigar.getNetInterfaceStat(IFACE_NAME);
		long curTxBytes = net.getTxBytes(),
				  curRxBytes = net.getRxBytes(),
				  curTxPackets = net.getTxPackets(),
				  curRxPackets = net.getRxPackets(),
				  curTxDropped = net.getTxDropped(),
				  curRxDropped = net.getRxDropped();
		netMetrics.put(TX_BYTES, curTxBytes - (Long)prevNetMetrics.get(TX_BYTES));
		netMetrics.put(RX_BYTES, curRxBytes - (Long)prevNetMetrics.get(RX_BYTES));
		netMetrics.put(TX_PACKETS, curTxPackets - (Long)prevNetMetrics.get(TX_PACKETS));
		netMetrics.put(RX_PACKETS, curRxPackets - (Long)prevNetMetrics.get(RX_PACKETS));
		netMetrics.put(TX_DROPPED, curTxDropped - (Long)prevNetMetrics.get(TX_DROPPED));
		netMetrics.put(RX_DROPPED, curRxDropped - (Long)prevNetMetrics.get(RX_DROPPED));
		
		prevNetMetrics.put(TX_BYTES, curTxBytes);
		prevNetMetrics.put(RX_BYTES, curRxBytes);
		prevNetMetrics.put(TX_PACKETS, curTxPackets);
		prevNetMetrics.put(RX_PACKETS, curRxPackets);
		prevNetMetrics.put(TX_DROPPED, curTxDropped);
		prevNetMetrics.put(RX_DROPPED, curRxDropped);
		
		metrics.put(NET, Maps.newHashMap(netMetrics));
	}
	
}
