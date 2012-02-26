package se.purplescout.purplemow;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import android.widget.TextView;

public class RemoteController extends Thread{
	
	private static final String broadcastAddress = "224.1.1.0";
	private static final int heartbeat_interval = 250;
	private static final int port = 22001;
	
	private boolean runFlag;
	private TextView mTextView;
	private MotorController motorController;
	
	public RemoteController(MotorController mc, TextView textView) {
		this.motorController = mc;
		this.mTextView = textView;
	}
	
	public void start(TextView textView) {
		this.mTextView = textView;
		runFlag = true;
		start();
	}

	public void halt() {
		runFlag = false;
	}
	
	@Override
	public void run() {
		InetAddress networkGroup;
		MulticastSocket socket;

		try {
			networkGroup = InetAddress.getByName(broadcastAddress);
			socket = new MulticastSocket(port);
			socket.setSoTimeout(heartbeat_interval * 2); // consider remote lost after two skipped heartbeats
			socket.joinGroup(networkGroup);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			while (runFlag) {
				//mc.readSensor();
				try {
					DatagramPacket packet = new DatagramPacket(new byte[256], 256);
					try {
						socket.receive(packet);
						new String(packet.getData(), "UTF-8");
					} catch (SocketTimeoutException ste) {
					}
					
					//Test
					byte[] data = packet.getData();
					if(data[0] == 1){
						motorController.moveForward(null);
						mTextView.post(new Runnable() {
							
							@Override
							public void run() {
								mTextView.setText("Moving forward");
							}
						});
					}else if(data[0] == 2){
						motorController.stop(null);
						mTextView.post(new Runnable() {
							
							@Override
							public void run() {
								mTextView.setText("Stopping");
							}
						});
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(heartbeat_interval);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
					return;
				}
			}
		} finally {
			try {
				socket.leaveGroup(networkGroup);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
