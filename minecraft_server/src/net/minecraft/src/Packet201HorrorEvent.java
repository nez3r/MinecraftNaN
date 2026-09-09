package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet201HorrorEvent extends Packet {
	public int eventId;
	public int effectId;
	public int durationTicks;
	public int itemId;
	public int itemCount;

	public Packet201HorrorEvent() {
	}

	public Packet201HorrorEvent(int var1) {
		this.eventId = var1;
	}

	public Packet201HorrorEvent(int var1, int var2, int var3) {
		this(var1, var2, var3, -1, 0);
	}

	public Packet201HorrorEvent(int var1, int var2, int var3, int var4, int var5) {
		this.eventId = var1;
		this.effectId = var2;
		this.durationTicks = var3;
		this.itemId = var4;
		this.itemCount = var5;
	}

	public void readPacketData(DataInputStream var1) throws IOException {
		this.eventId = var1.readInt();
		this.effectId = var1.readInt();
		this.durationTicks = var1.readInt();
		this.itemId = var1.readInt();
		this.itemCount = var1.readInt();
	}

	public void writePacketData(DataOutputStream var1) throws IOException {
		var1.writeInt(this.eventId);
		var1.writeInt(this.effectId);
		var1.writeInt(this.durationTicks);
		var1.writeInt(this.itemId);
		var1.writeInt(this.itemCount);
	}

	public void processPacket(NetHandler var1) {
		var1.handleHorrorEvent(this);
	}

	public int getPacketSize() {
		return 20;
	}
}
