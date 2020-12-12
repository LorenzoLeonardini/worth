package dev.leonardini.worth.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import dev.leonardini.worth.networking.NetworkUtils.Operation;

public class WorthBuffer {

	private ByteBuffer buffer;

	public WorthBuffer() {
		this(1024);
	}

	public WorthBuffer(int size) {
		this(size, false);
	}
	
	public WorthBuffer(int size, boolean direct) {
		if(direct) {
			buffer = ByteBuffer.allocateDirect(size);
		} else {
			buffer = ByteBuffer.allocate(size);
		}
	}

	public WorthBuffer(byte data[]) {
		buffer = ByteBuffer.wrap(data);
	}

	public void put(byte b) {
		buffer.put(b);
	}

	public void put(byte arr[]) {
		buffer.putInt(arr.length);
		buffer.put(arr);
	}
	
	public void put(WorthBuffer src) {
		src.buffer.flip();
		buffer.putInt(src.remaining());
		while(src.hasRemaining()) {
			buffer.put(src.get());
		}
	}

	public void put(byte src[], int offset, int length) {
		buffer.putInt(length);
		buffer.put(src, offset, length);
	}

	public void putChar(char c) {
		buffer.putChar(c);
	}

	public void putDouble(double d) {
		buffer.putDouble(d);
	}

	public void putFloat(float f) {
		buffer.putFloat(f);
	}

	public void putInt(int i) {
		buffer.putInt(i);
	}

	public void putLong(long l) {
		buffer.putLong(l);
	}
	
	public void putShort(short s) {
		buffer.putShort(s);
	}
	
	public void putString(String s) {
		buffer.putInt(s.length());
		char arr[] = s.toCharArray();
		for(int i = 0; i < arr.length; i++)
			buffer.putChar(arr[i]);
	}
	
	public void putBoolean(boolean b) {
		buffer.put((byte)(b ? 1 : 0));
	}
	
	public void putOperation(Operation op) {
		buffer.put((byte)op.ordinal());
	}
	
	public byte get() {
		return buffer.get();
	}

	public byte[] getArray() {
		byte data[] = new byte[buffer.getInt()];
		buffer.get(data, 0, data.length);
		return data;
	}
	
	public boolean canGetArray() {
		if(buffer.remaining() < Integer.BYTES) return false;
		buffer.mark();
		int len = buffer.getInt();
		boolean result = true;
		if(buffer.remaining() < len)
			result = false;
		buffer.reset();
		return result;
	}

	public char getChar() {
		return buffer.getChar();
	}

	public double getDouble() {
		return buffer.getDouble();
	}

	public float getFloat() {
		return buffer.getFloat();
	}

	public int getInt() {
		return buffer.getInt();
	}

	public long getLong() {
		return buffer.getLong();
	}
	
	public short getShort() {
		return buffer.getShort();
	}
	
	public String getString() {
		int length = buffer.getInt();
		char data[] = new char[length];
		for(int i = 0; i < length; i++)
			data[i] = buffer.getChar();
		return new String(data);
	}
	
	public boolean getBoolean() {
		return buffer.get() == 1;
	}
	
	public Operation getOperation() {
		return Operation.values()[buffer.get()];
	}
	
	public void end() {
		buffer.put(NetworkUtils.END_CODE);
	}
	
	public boolean isFinished() {
		buffer.mark();
		int length = buffer.remaining();
		int code_len = NetworkUtils.END_CODE.length;
		if(code_len > length) return false;
		byte end[] = new byte[code_len];
		buffer.position(length - code_len);
		buffer.get(end, 0, code_len);
		boolean finished = true;
		for(int i = 0; i < code_len && finished; i++) {
			finished = end[i] == NetworkUtils.END_CODE[i];
		}
		buffer.reset();
		return finished;
	}
	
	public void write(SocketChannel socket) throws IOException {
		buffer.flip();
		socket.write(buffer);
	}
	
	public void write(WritableByteChannel channel) throws IOException {
		buffer.flip();
		channel.write(buffer);
	}
	
	public int read(SocketChannel socket) throws IOException {
		int n = socket.read(buffer);
		buffer.flip();
		return n;
	}
	
	public int read(ReadableByteChannel channel) throws IOException {
		int n = channel.read(buffer);
		buffer.flip();
		return n;
	}
	
	public int remaining() {
		return buffer.remaining();
	}
	
	public int limit() {
		return buffer.limit();
	}
	
	public void rewing() {
		buffer.rewind();
	}
	
	public void clear() {
		buffer.clear();
	}
	
	public void rewind() {
		buffer.remaining();
	}
	
	public void compact() {
		buffer.compact();
	}
	
	public byte[] array() {
		return buffer.array();
	}
	
	public boolean hasRemaining() {
		return buffer.hasRemaining();
	}
	
	public String toString() {
		return buffer.toString();
	}

}
