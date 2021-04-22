package com.pignier.instagramdm.Login;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.ArrayList;
import java.security.spec.X509EncodedKeySpec ;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.lang.Math;
import java.util.Arrays;
import java.util.Date;



public class CryptoUtil{
	public static String encrypt_password(String password,String publickey,int keyid,int appid) throws Exception{
		String time = String.valueOf(System.currentTimeMillis()/1000);
		//int key = 201;
		//String pkey = "2acf74bb38eaf22e2852184a3c576d4383af69986ddfea55530bddefa455ec76";
		
		
		// Parse publikey
		byte[] PUBLIC_KEY_PARSED = new byte[publickey.length() / 2];
		for (int character = 0; character < PUBLIC_KEY_PARSED.length; character++) {
			int index = character * 2;
			int decimal = Integer.parseInt(publickey.substring(index, index + 2), 16);
			PUBLIC_KEY_PARSED[character] = (byte) decimal;
		}
		
		
		// Generate Key
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(256);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] IV = new byte[12];

		//Encrypt the PASSWORD with random key and random iv
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, IV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
		cipher.updateAAD(time.getBytes());
		
		// Generate random SealBox with PUBLIC_KEY + random keypair
		byte [] sealed = SealedBoxUtility.crypto_box_seal(secretKey.getEncoded(),PUBLIC_KEY_PARSED);
		byte[] cipherText = cipher.doFinal(password.getBytes());
		
		// Create the final enc_pass_array
		int overheadLength = 48;
		byte [] enc_pass_array = new byte[password.length()+36+16+overheadLength];
		int cursor = 0;
		enc_pass_array[cursor] = 1;
		enc_pass_array[cursor += 1] = (byte)keyid;
		cursor += 1;

		
		// Add KEY_ID and sealed_array size to enc_pass_array
		enc_pass_array[cursor] = (byte) (255 & sealed.length);
		enc_pass_array[cursor + 1] = (byte) (sealed.length >> 8 & 255);
		cursor += 2;
		
		// Add sealed_array to enc_pass_array
		for(int j=cursor;j<cursor+sealed.length;j++){
			enc_pass_array[j] = sealed[j-cursor];
		}
		cursor += 32;
		cursor += overheadLength;
		
		// Separate cipherText_array in two sections
		byte [] lastsection = Arrays.copyOfRange(cipherText,cipherText.length -16,cipherText.length);
		byte [] firstsection = Arrays.copyOfRange(cipherText,0,cipherText.length - 16);
		
		// Add sections to enc_pass_array
		for(int j=cursor;j<cursor+lastsection.length;j++){
			enc_pass_array[j] = lastsection[j-cursor];
		}
		cursor += 16;
		for(int j=cursor;j<cursor+firstsection.length;j++){
			enc_pass_array[j] = firstsection[j-cursor];
		}
		
		// Convert enc_pass_bytes to enc_pass_64 for instagram format
		String encPassword64 =  Base64.getEncoder().encodeToString(enc_pass_array);
		String encPassword = "#PWD_INSTAGRAM_BROWSER:"+appid+":"+time+":"+encPassword64;
		return encPassword;
	}
}