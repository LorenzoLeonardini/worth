package dev.leonardini.worth.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;

import dev.leonardini.worth.data.CardLocation;
import dev.leonardini.worth.networking.NetworkUtils;
import dev.leonardini.worth.networking.WorthBuffer;

public class ServerChat {

	protected void sendChatNotification(String projectName, String card, String user, CardLocation from, CardLocation to) {
		long now = System.currentTimeMillis();
		try {
			InetAddress ia = InetAddress.getByName(NetworkUtils.MULTICAST_ADDRESS);
			WorthBuffer buffer = new WorthBuffer();
			buffer.putInt(NetworkUtils.ChatOperation.SERVER.ordinal());
			buffer.putLong(now);
			buffer.putString(projectName);
			buffer.putString(card);
			buffer.putString(user);
			buffer.putInt(from == null ? -1 : from.ordinal());
			buffer.putInt(to.ordinal());
			buffer.flip();
			
			DatagramSocket ms = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit(), ia, NetworkUtils.MULTICAST_PORT);
			ms.send(packet);
			ms.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// Send also RMI because it's important
		try {
			RMIServer.chatForwarder.send(now, projectName, card, user, from, to);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
}
