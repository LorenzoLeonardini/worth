package dev.leonardini.worth.networking;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import dev.leonardini.worth.networking.NetworkUtils.Operation;

public class WorthBuffer {

	private ByteBuffer buffer;
	private boolean direct = false;

	public WorthBuffer() {
		this(1024);
	}

	public WorthBuffer(int size) {
		this(size, false);
	}
	
	public WorthBuffer(int size, boolean direct) {
		this.direct = direct;
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
		if(buffer.remaining() < Byte.BYTES) grow(Byte.BYTES);
		buffer.put(b);
	}

	public void put(byte arr[]) {
		int size = Integer.BYTES + arr.length;
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(arr.length);
		buffer.put(arr);
	}
	
	public void put(WorthBuffer src) {
		src.buffer.flip();
		int size = Integer.BYTES + src.remaining();
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(src.remaining());
		while(src.hasRemaining()) {
			buffer.put(src.get());
		}
	}

	public void put(byte src[], int offset, int length) {
		int size = Integer.BYTES + length;
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(length);
		buffer.put(src, offset, length);
	}

	public void putChar(char c) {
		if(buffer.remaining() < Character.BYTES) grow(Character.BYTES);
		buffer.putChar(c);
	}

	public void putDouble(double d) {
		if(buffer.remaining() < Double.BYTES) grow(Double.BYTES);
		buffer.putDouble(d);
	}

	public void putFloat(float f) {
		if(buffer.remaining() < Float.BYTES) grow(Float.BYTES);
		buffer.putFloat(f);
	}

	public void putInt(int i) {
		if(buffer.remaining() < Integer.BYTES) grow(Integer.BYTES);
		buffer.putInt(i);
	}

	public void putLong(long l) {
		if(buffer.remaining() < Long.BYTES) grow(Long.BYTES);
		buffer.putLong(l);
	}
	
	public void putShort(short s) {
		if(buffer.remaining() < Short.BYTES) grow(Short.BYTES);
		buffer.putShort(s);
	}
	
	public void putString(String s) {
		int size = Integer.BYTES + Character.BYTES * s.length();
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(s.length());
		char arr[] = s.toCharArray();
		for(int i = 0; i < arr.length; i++)
			buffer.putChar(arr[i]);
	}
	
	public void putBoolean(boolean b) {
		if(buffer.remaining() < Byte.BYTES) grow(Byte.BYTES);
		buffer.put((byte)(b ? 1 : 0));
	}
	
	public void putOperation(Operation op) {
		if(buffer.remaining() < Byte.BYTES) grow(Byte.BYTES);
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
	
	public void write(SocketChannel socket) throws IOException {
		buffer.flip();
		socket.write(buffer);
	}
	
	public void write(WritableByteChannel channel) throws IOException {
		buffer.flip();
		channel.write(buffer);
	}
	
	private void grow(int min_increment) {
		int min_size = buffer.position() + min_increment;
		int new_size = buffer.capacity() * 2;
		while(new_size < min_size) new_size *= 2;
		ByteBuffer newBuffer = direct ? ByteBuffer.allocateDirect(new_size) : ByteBuffer.allocate(new_size);
		newBuffer.put(buffer.array());
		newBuffer.position(buffer.position());
		buffer = newBuffer;
	}
	
	public int read(SocketChannel socket) throws IOException {
		int n = socket.read(buffer);
		while(buffer.position() == buffer.limit()) {
			grow(0);
			n += socket.read(buffer);
		}
		buffer.flip();
		return n;
	}
	
	public int read(ReadableByteChannel channel) throws IOException {
		int n = channel.read(buffer);
		while(buffer.position() == buffer.limit()) {
			grow(0);
			n += channel.read(buffer);
		}
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
	
	public void flip() {
		buffer.flip();
	}
	
	public void mark() {
		buffer.mark();
	}
	
	public void reset() {
		buffer.reset();
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

	public SocketAddress receive(DatagramChannel dc) throws IOException {
		return dc.receive(buffer);
	}

}
