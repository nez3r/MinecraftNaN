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
	public int sequence;
	public String message;

	public Packet201HorrorEvent() {
	}

	public Packet201HorrorEvent(int var1) {
		this.eventId = var1;
	}

	public Packet201HorrorEvent(int var1, int var2, int var3) {
		this(var1, var2, var3, -1, 0);
	}

	public Packet201HorrorEvent(int var1, int var2, int var3, int var4, int var5) {
		this(var1, var2, var3, var4, var5, 0, "");
	}

	public Packet201HorrorEvent(int var1, int var2, int var3, int var4, int var5, int var6, String var7) {
		this.eventId = var1;
		this.effectId = var2;
		this.durationTicks = var3;
		this.itemId = var4;
		this.itemCount = var5;
		this.sequence = var6;
		this.message = var7 == null ? "" : var7;
	}

	public void readPacketData(DataInputStream var1) throws IOException {
		this.eventId = var1.readInt();
		this.effectId = var1.readInt();
		this.durationTicks = var1.readInt();
		this.itemId = var1.readInt();
		this.itemCount = var1.readInt();
		this.sequence = var1.readInt();
		this.message = var1.readUTF();
	}

	public void writePacketData(DataOutputStream var1) throws IOException {
		var1.writeInt(this.eventId);
		var1.writeInt(this.effectId);
		var1.writeInt(this.durationTicks);
		var1.writeInt(this.itemId);
		var1.writeInt(this.itemCount);
		var1.writeInt(this.sequence);
		var1.writeUTF(this.message == null ? "" : this.message);
	}

	public void processPacket(NetHandler var1) {
		var1.handleHorrorEvent(this);
	}

	public int getPacketSize() {
		String text = this.message == null ? "" : this.message;
		return 26 + text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
	}
}
