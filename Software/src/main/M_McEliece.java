package main;

import java.util.List;

import mceliece.McEliece;

public class M_McEliece {

	private final int fieldExponent = 8;
	private final int numErrors = 2;
	private final int supportSize = (int) (Math.pow(2, fieldExponent));

	private McEliece mcEliece;

	private int[][] m_public_key;
	private int[][] o_public_key;

	public M_McEliece() {
		mcEliece = new McEliece();
		m_public_key = mcEliece.createPublicKey(fieldExponent, numErrors, supportSize);
	}

	public int[][] getMyPublicKey() {
		return m_public_key;
	}

	public void setOtherPublicKey(int[][] o_public_key) {
		this.o_public_key = o_public_key;
	}

//	public int[] generateCipherKey() {
//		System.out.println(supportSize - fieldExponent * numErrors);
//		return McEliece.getRandomErrorVector(2, supportSize - fieldExponent * numErrors);
//	}

	// encrypt with receiver's public key
	public List<int[]> encryptCipherKey(int[] cipher_key) {
		return mcEliece.encrypt(cipher_key, numErrors, fieldExponent, supportSize, o_public_key);
	}

	// decrypt with private key
	public int[] decryptCipherKey(List<int[]> encrypted_cipher_key) {
		return mcEliece.pattersonDecode(encrypted_cipher_key);
	}
}
