package dev.leonardini.worth.data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import dev.leonardini.worth.networking.WorthBuffer;

public class User {
	
	private String username;
	private byte[] password;
	private byte[] salt;
	private String email;
	private String mail_hash;
	
	public User(String username, String password) {
		this.username = username;
		
		SecureRandom random = new SecureRandom();
		salt = new byte[16];
		random.nextBytes(salt);
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			this.password = factory.generateSecret(spec).getEncoded();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	public User(byte data[]) {
		WorthBuffer buffer = new WorthBuffer(data);
		username = buffer.getString();
		password = buffer.getArray();
		salt = buffer.getArray();
		if(buffer.hasRemaining()) {
			email = buffer.getString();
			mail_hash = buffer.getString();
		}
	}
	
	public void setEmail(String email) {
		this.email = email.trim().toLowerCase();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(this.email.getBytes()); 
			  
            BigInteger no = new BigInteger(1, messageDigest); 
            this.mail_hash = no.toString(16); 
            while (this.mail_hash.length() < 32) { 
            	this.mail_hash = "0" + this.mail_hash; 
            } 
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public String getEmail(String email) {
		return this.email;
	}
	
	public String getMailHash() {
		return this.mail_hash;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public boolean checkPassword(String password) {
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte to_compare[] = factory.generateSecret(spec).getEncoded();
			if(this.password.length != to_compare.length) return false;
			for(int i = 0; i < this.password.length; i++) {
				if(this.password[i] != to_compare[i])
					return false;
			}
			return true;
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public byte[] toByteArray() {
		int size = Integer.BYTES * 3 + username.length() * Character.BYTES + password.length + salt.length;
		if(email != null) {
			size += Integer.BYTES * 2 + email.length() * Character.BYTES + mail_hash.length() * Character.BYTES;
		}
		WorthBuffer buffer = new WorthBuffer(size);
		buffer.putString(username);
		buffer.put(password);
		buffer.put(salt);
		if(email != null) {
			buffer.putString(email);
			buffer.putString(mail_hash);
		}
		return buffer.array();
	}
	
}
