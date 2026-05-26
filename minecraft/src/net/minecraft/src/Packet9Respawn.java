package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet9Respawn extends Packet {
	public void processPacket(NetHandler var1) {
		var1.func_9448_a(this);
	}

	public void readPacketData(DataInputStream var1) throws IOException {
	}

	public void writePacketData(DataOutputStream var1) throws IOException {
	}

	public int getPacketSize() {
		return 0;
	}
}
