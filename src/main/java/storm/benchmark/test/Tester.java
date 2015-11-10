package storm.benchmark.test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class Tester{

	public static void main(String[] args) 
			throws InterruptedException, ExecutionException, SigarException{
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				Timer timer = new Timer(true);
				timer.scheduleAtFixedRate(				
						new Task(),
						0,
						2000);
				try {
					Thread.sleep(11000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
		t.setDaemon(true);
		t.start();
		while(true){}
	}

}

class Task
	extends TimerTask{

	private Sigar sigar = new Sigar();
	private Cpu cpu = null;
	private Mem mem = null;
	private NetInterfaceStat net = null;
	private FileSystemUsage disk = null;
	
	private long _prevUser = 0,
							_prevSys = 0,
							_prevIdle = 0,
							_prevIrq = 0,
							_prevSoftIrq = 0,
							_prevStolen = 0,
							_prevNice = 0,
							_prevWait = 0,
							_prevTxBytes = 0,
							_prevRxBytes = 0,
							_prevTxPkt = 0,
							_prevRxPkt = 0,
							_prevTxDropped = 0,
							_prevRxDropped = 0,
							_prevReads = 0,
							_prevWrites = 0,
							_prevReadBytes = 0,
							_prevWriteBytes = 0;
	
	@Override
	public void run(){
		try{
			cpu = sigar.getCpu();
			mem = sigar.getMem();
			net = sigar.getNetInterfaceStat("eth0");
			disk = sigar.getFileSystemUsage("/");
		}catch (SigarException e){
			e.printStackTrace();
		}
		long sys = cpu.getSys(),
				 user = cpu.getUser(),
				 nice = cpu.getNice(),
				 irq = cpu.getIrq(),
				 softIrq = cpu.getSoftIrq(),
				 stolen = cpu.getStolen(),
				 wait = cpu.getWait(),
				 idle = cpu.getIdle();
		long total = 0;
		total += _prevSys == 0 ? 0 : sys - _prevSys;
		total += _prevUser == 0 ? 0 : user - _prevUser;
		total += _prevIdle == 0 ? 0 : idle - _prevIdle;
		total += _prevIrq == 0 ? 0 : irq - _prevIrq;
		total += _prevSoftIrq == 0 ? 0 : softIrq - _prevSoftIrq;
		total += _prevWait == 0 ? 0 : wait - _prevWait;
		total += _prevStolen == 0 ? 0 : wait - _prevWait;
		total += _prevNice == 0 ? 0 : nice - _prevNice;
		System.out.println("***********CPU************");
		if(total != 0){
			System.out.println("Sys%: " + (sys - _prevSys) * 100.0 / total + "%");
			System.out.println("User%: " + (user - _prevUser) * 100.0 / total + "%");
			System.out.println("Idle%: " + (idle - _prevIdle) * 100.0 / total + "%");
			System.out.println("Irq%: " + (irq - _prevIrq) * 100.0 / total + "%");
			System.out.println("softIrq%: " + (softIrq - _prevSoftIrq) * 100.0 / total + "%");
			System.out.println("stolen%: " + (stolen - _prevStolen) * 100.0 / total + "%");
			System.out.println("wait%: " + (wait - _prevWait) * 100.0 / total + "%");
		}
			
		System.out.println("***********Memory************");
		System.out.println("Total: " + mem.getTotal() / 1000 / 1000 + " MB");
		System.out.println("Used: " + mem.getUsed() / 1000 / 1000 + " MB");
		System.out.println("Actual used: " + mem.getActualUsed() / 1000 / 1000 + " MB");
		System.out.println("Used: " + mem.getUsed() / 1000 / 1000 + " MB");
		System.out.println("Used%: " + mem.getUsedPercent() + "%");
		System.out.println("Actual Used: " + mem.getActualUsed() / 1000 / 1000 + " MB" );
		System.out.println("===========================");
		
		long txBytes = net.getTxBytes(),
				  rxBytes = net.getRxBytes(),
				  txPkt = net.getTxPackets(),
				  rxPkt = net.getRxPackets(),
				  txDropped = net.getTxDropped(),
				  rxDropped = net.getRxDropped();
		
		System.out.println("***********Network(eth0)************");
		System.out.println("Bytes transmitted: " + (txBytes - _prevTxBytes));
		System.out.println("Bytes received: " + (rxBytes - _prevRxBytes));
		System.out.println("Packets transmitted: " + (txPkt - _prevTxPkt));
		System.out.println("Packets received: " + (rxPkt - _prevRxPkt));
		System.out.println("Transmitted dropped: " + (txDropped - _prevTxDropped));
		System.out.println("Received dropped: " + (rxDropped - _prevRxDropped));
		
		long reads = disk.getDiskReads(),
				 writes = disk.getDiskWrites(),
				 readBytes = disk.getDiskReadBytes(),
				 writeBytes = disk.getDiskWriteBytes();
		System.out.println("***********Disk('/')************");
		System.out.println("Bytes read: " + (readBytes - _prevReadBytes));
		System.out.println("Bytes written: " + (writeBytes - _prevWriteBytes));
		System.out.println("Number of reads: " + (reads - _prevReads));
		System.out.println("Number of writes: " + (writes - _prevWrites));
//		
		System.out.println();
		System.out.println();
		
		_prevSys = sys;
		_prevUser = user;
		_prevNice = nice;
		_prevIrq = irq;
		_prevSoftIrq = softIrq;
		_prevStolen = stolen;
		_prevWait = wait;
		_prevIdle = idle;
		_prevTxBytes = txBytes;
		_prevRxBytes = rxBytes;
		_prevTxPkt = txPkt;
		_prevRxPkt = rxPkt;
		_prevTxDropped = txDropped;
		_prevRxDropped = rxDropped;
		_prevReadBytes = readBytes;
		_prevWriteBytes = writeBytes;
		_prevReads = reads;
		_prevWrites = writes;
	}
	
}

