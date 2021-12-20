package main;

import java.util.Random;

import rabbit.RabbitCipher;

public class M_RabbitCipher {

	public byte[] encryptMessage(String message, int[] cipher_key) {
		byte[] msg = Utils.stringToByteArr(message);
		byte[] key = Utils.binArrToByteArr(cipher_key);

		RabbitCipher rc = new RabbitCipher();
		rc.setupKey(key);
		rc.crypt(msg);

		return msg.clone();
	}

	public String decryptMessage(byte[] encrypted_message, int[] cipher_key) {
		byte[] msg = encrypted_message.clone();
		byte[] key = Utils.binArrToByteArr(cipher_key);

		RabbitCipher rc = new RabbitCipher();
		rc.setupKey(key);
		rc.crypt(msg);

		return Utils.byteArrToString(msg);
	}

	public int[] generateCipherKey() {
		int[] cipher_key = new int[240];
		Random rand = new Random();
		for (int i = 0 ; i < 128; i++)
			cipher_key[i] = rand.nextInt(2);
		return cipher_key;
	}
}
