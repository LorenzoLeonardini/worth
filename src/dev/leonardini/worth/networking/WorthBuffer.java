package dev.leonardini.worth.networking;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import dev.leonardini.worth.networking.NetworkUtils.Operation;

/**
 * This is a wrapper around the ByteBuffer class. It adds the possibility to write and
 * read more types of data (i.e. Strings) and generally provides some sort of 
 * serialization logic in order to "structure" data like byte arrays.
 * 
 * Another important feature is the automatic growth of the buffer, avoiding overflow
 * exceptions and making easy to send and receive data.
 */
public class WorthBuffer {

	private ByteBuffer buffer;
	private boolean direct = false;

	/**
	 * Allocates a new worth buffer.
	 * Default capacity is 1024 bytes
	 */
	public WorthBuffer() {
		this(1024);
	}

	/**
	 * Allocates a new worth buffer.
	 * @param size the initial capacity of the buffer
	 */
	public WorthBuffer(int size) {
		this(size, false);
	}
	
	/**
	 * Allocats a new worth buffer.
	 * @param size the initial capacity of the buffer
	 * @param direct define if `allocate` or `allocateDirect`
	 */
	public WorthBuffer(int size, boolean direct) {
		this.direct = direct;
		if(direct) {
			buffer = ByteBuffer.allocateDirect(size);
		} else {
			buffer = ByteBuffer.allocate(size);
		}
	}

	/**
	 * Create a new worth buffer, given an already existing byte array
	 * @param data
	 */
	public WorthBuffer(byte data[]) {
		buffer = ByteBuffer.wrap(data);
	}

	/**
	 * Put a byte into the buffer
	 * @param b
	 */
	public void put(byte b) {
		if(buffer.remaining() < Byte.BYTES) grow(Byte.BYTES);
		buffer.put(b);
	}

	/**
	 * Put a structured byte array into the buffer.
	 * 
	 * Structured means that the array is put alongside its length, allowing
	 * to retrieve that when reading from the buffer
	 * @param arr
	 */
	public void put(byte arr[]) {
		int size = Integer.BYTES + arr.length;
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(arr.length);
		buffer.put(arr);
	}
	
	/**
	 * Put a worth buffer into the buffer. The put operation is done in a
	 * structured way, as if it was put(src.array()), but it only copies
	 * remaining bytes.
	 * 
	 * The buffer needs to be passed in read mode, as it will be flipped
	 * @param src
	 */
	public void put(WorthBuffer src) {
		src.buffer.flip();
		int size = Integer.BYTES + src.remaining();
		if(buffer.remaining() < size) grow(size);
		buffer.putInt(src.remaining());
		while(src.hasRemaining()) {
			buffer.put(src.get());
		}
	}

	/**
	 * Put a structured byte array into the buffer, but defining custom offset and length
	 * @param src
	 * @param offset
	 * @param length
	 */
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
	
	/**
	 * Put a structured String into the buffer
	 * @param s
	 */
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

	/**
	 * Put an operation code into the buffer
	 * @param op
	 */
	public void putOperation(Operation op) {
		if(buffer.remaining() < Byte.BYTES) grow(Byte.BYTES);
		buffer.put((byte)op.ordinal());
	}
	
	/**
	 * Get a byte from the buffer
	 * @return
	 */
	public byte get() {
		return buffer.get();
	}

	/**
	 * Get a byte array from the buffer
	 * @return
	 */
	public byte[] getArray() {
		byte data[] = new byte[buffer.getInt()];
		buffer.get(data, 0, data.length);
		return data;
	}
	
	/**
	 * Check if another byte array can be read from the buffer
	 * @return
	 */
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
	
	/**
	 * Get a string from the buffer. The string must have been put with the
	 * 'putString' method
	 */
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

	/**
	 * Write the buffer to a SocketChannel
	 * 
	 * Useless, but allows you to forget about flipping
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public void write(SocketChannel socket) throws IOException {
		buffer.flip();
		socket.write(buffer);
	}

	/**
	 * Write the buffer to a WritableByteChannel
	 * 
	 * Useless, but allows you to forget about flipping
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public void write(WritableByteChannel channel) throws IOException {
		buffer.flip();
		channel.write(buffer);
	}
	
	/**
	 * Grow the buffer size. A new buffer of size n is created and all the data
	 * is copied. n is calculated doubling the current size, but making sure that
	 * the new buffer size is at least the old position + min_increment
	 * 
	 * "Example": if the current size is 1024 and min_increment is 10, new size is 2048
	 * 			  if the current size is 1024 and min_increment is 1500, new size is 4096
	 * 
	 * Note that the increment is relative to the current position of the buffer, not its
	 * actual capacity. It is therefore used to say "I need to put min_increment bytes"
	 * 
	 * @param min_increment
	 */
	private void grow(int min_increment) {
		int min_size = buffer.position() + min_increment;
		int new_size = buffer.capacity() * 2;
		while(new_size < min_size) new_size *= 2;
		ByteBuffer newBuffer = direct ? ByteBuffer.allocateDirect(new_size) : ByteBuffer.allocate(new_size);
		newBuffer.put(buffer.array());
		newBuffer.position(buffer.position());
		buffer = newBuffer;
	}
	
	/**
	 * Read from a SocketChannel. This automatically grow the buffer in case
	 * it cannot contains all the data.
	 * 
	 * @param socket
	 * @return the number of bytes read
	 * @throws IOException
	 */
	public int read(SocketChannel socket) throws IOException {
		int n = socket.read(buffer);
		while(buffer.position() == buffer.limit()) {
			grow(0);
			n += socket.read(buffer);
		}
		buffer.flip();
		return n;
	}
	
	/**
	 * Read from a ReadableByteChannel. This automatically grow the buffer in case
	 * it cannot contains all the data.
	 * 
	 * @param socket
	 * @return the number of bytes read
	 * @throws IOException
	 */
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
