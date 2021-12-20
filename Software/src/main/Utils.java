package main;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static byte[] binArrToByteArr(int[] binArr) {
		StringBuilder sb = new StringBuilder(binArr.length);
		for (int i = 0; i < binArr.length; i++)
			sb.append(binArr[i] == 0 ? '0' : '1');

		byte[] byteArr = new BigInteger(sb.toString(), 2).toByteArray();
		return byteArr;
	}

	public static String byteArrToString(byte[] byteArr) {
		return new String(byteArr, StandardCharsets.UTF_8);
	}

	public static String binArrToString(int[] binArr) {
		return byteArrToString(binArrToByteArr(binArr));
	}

	public static byte[] stringToByteArr(String str) {
		return str.getBytes();
	}

//	public static int[] stringToBinArr(String str) {
//		StringBuilder sb = new StringBuilder();
//		char[] chArr = str.toCharArray();
//		for (char ch : chArr)
//			sb.append(String.format("%8s", Integer.toBinaryString(ch)).replaceAll(" ", "0"));
//
//		char[] new_chArr = sb.toString().toCharArray();
//		int[] binArr = new int[new_chArr.length];
//		for (int i = 0; i < new_chArr.length; i++)
//			binArr[i] = (new_chArr[i] == '0' ? 0 : 1); 
//
//		return binArr;
//	}

	static boolean checkEquality(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	public static List<Password> parseStringToPasswords(String s) {
		List<Password> output = new ArrayList<>();
		String s1 = s.substring(10, s.length() - 1); // chop off brackets
		String[] listString = s1.split(", Password ");
		for (String s2 : listString) {
			if (s2.isEmpty())
				continue;
			s2 = s2.substring(1, s2.length() - 1);
			String[] subStr = s2.split("site=");
			String[] subStr1 = subStr[1].split(", username=");
			String[] subStr2 = subStr1[1].split(", password=");
			String site = subStr1[0];
			String username = subStr2[0];
			String password = subStr2[1];
			output.add(new Password(site, username, password));
		}
		return output;
	}

	public static Password parseStringToPassword(String s) {
		String s1 = s.substring(10, s.length() - 1); // chop off brackets
		String[] subStr = s1.split("site=");
		String[] subStr1 = subStr[1].split(", username=");
		String[] subStr2 = subStr1[1].split(", password=");
		String site = subStr1[0];
		String username = subStr2[0];
		String password = subStr2[1];
		return new Password(site, username, password);
	}

	public static void main(String[] args) {
		Client alice = new Client();
		Server bob = new Server();
		alice.sendPublicKeysToServer(bob);
		bob.sendPublicKeysToClient(alice);

		alice.sendMessageToServer(bob, "get passwords");
	}

	//	public static void main(String[] args) {
	//		/***************************************************************************/
	//		/*********************** Alice / TX ******************************************/
	//		/***************************************************************************/
	//
	//		McEliece mcEliece = new McEliece();
	//
	//		// determine what message Alice will send Bob
	//		String msg_str = "Hello World!";
	//		byte[] msg = msg_str.getBytes();
	//
	//		// generate CipherKey using McEliece
	//		int fieldExponent = 8;
	//		int numErrors = 2;
	//		int supportSize = (int) (Math.pow(2, fieldExponent));
	//		int[] toEncrypt = McEliece.getRandomErrorVector(2, supportSize - fieldExponent * numErrors);
	//		System.out.println("toEncrypt Length = " + toEncrypt.length + "\n");
	//
	//		StringBuilder sb = new StringBuilder(toEncrypt.length);
	//		for (int i = 0; i < toEncrypt.length; i++)
	//			sb.append(toEncrypt[i] == 0 ? '0' : '1');
	//
	//		byte[] cipherKey = new BigInteger(sb.toString(), 2).toByteArray();
	//
	//		// use CipherKey to encrypt Alice's message using RabbitCipher
	//		RabbitCipher alice = new RabbitCipher();
	//		alice.setupKey(cipherKey);
	//		alice.crypt(msg);
	//
	//		byte[] msg_encrypted = msg.clone();
	//		System.out.println("Alice sent:\n" + msg_str + "\n");
	//
	//		// encrypt CipherKey using McEliece
	//		int[] cipherKey_encrypted = mcEliece.encrypt(toEncrypt, numErrors, fieldExponent, supportSize);
	//
	//		// sign Alice's message using Ecdsa
	//		PrivateKey ecdsa_privateKey = new PrivateKey();
	//		PublicKey ecdsa_publicKey = ecdsa_privateKey.publicKey();
	//		Signature alice_signature = Ecdsa.sign(msg_str, ecdsa_privateKey);
	//
	//		/***************************************************************************/
	//		/*********************** Bob / RX ******************************************/
	//		/***************************************************************************/
	//
	//		// decrypt CipherKey using McEliece
	//		int[] decrypted = mcEliece.pattersonDecode(cipherKey_encrypted, mcEliece.parityCheck);
	//		sb = new StringBuilder(decrypted.length);
	//		for (int i = 0; i < decrypted.length; i++)
	//			sb.append(decrypted[i] == 0 ? '0' : '1');
	//		byte[] cipherKey_decrypted = new BigInteger(sb.toString(), 2).toByteArray();
	//
	//		// use decrypted CipherKey to decrypt Alice's message using RabbitCipher
	//		RabbitCipher bob = new RabbitCipher();
	//		bob.setupKey(cipherKey_decrypted);
	//		bob.crypt(msg_encrypted);
	//
	//		// verify Alice's message using Ecdsa
	//		byte[] msg_decrypted = msg_encrypted.clone();
	//		if (Ecdsa.verify(msg_str, alice_signature, ecdsa_publicKey)) {
	//			System.out.println("Bob verified Alice sent the message");
	//			System.out.println("Bob got:\n" + new String(msg_decrypted, StandardCharsets.UTF_8) + "\n");
	//		} else {
	//			System.out.println("Bob knows Alice didnt send the message");
	//		}
	//	}
}
