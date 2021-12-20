package mceliece;

import java.util.Scanner;

public class Demo {

	@SuppressWarnings({ "static-access", "resource" })
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		McEliece mcEliece = new McEliece();

		int fieldExponent =  6;
		int numErrors = 2;
		int supportSize = (int)(Math.pow(2, fieldExponent));
		// int codeLength = 0;

		if (supportSize <= numErrors * fieldExponent)
			System.out.println("Invalid parameters.");

		System.out.println("Using parameters:"
				+ "\n Field exponent: " + fieldExponent
				+ "\n Number of errors to correct: " + numErrors);  

		int[] toEncrypt = mcEliece.getRandomErrorVector(2, supportSize - fieldExponent * numErrors);

		System.out.println("Encrypting:");
		McEliece.print(toEncrypt);

		int[] encrypted = mcEliece.encrypt(toEncrypt, numErrors, fieldExponent, supportSize);

		System.out.println("Goppa Polynomial: " + mcEliece.irredPoly.toString());
		System.out.println("Field Polynomial: " + mcEliece.gF.gf_irredPoly.toString());
		System.out.println("Support size: " + mcEliece.support.length);
		System.out.println("Minimum distance >= " + (2 * mcEliece.irredPoly.degree + 1));
		System.out.println("Error correction capacity: " + mcEliece.irredPoly.degree);
		scan.nextLine();

		System.out.println("Parity check:");
		McEliece.print(mcEliece.parityCheck);
		if (!McEliece.isSystematic(mcEliece.parityCheck, mcEliece.parityCheck[0].length - mcEliece.parityCheck.length))
			scan.nextLine();


		System.out.println("Public key:");
		McEliece.print(mcEliece.pubKey);

		System.out.println("Encrypted:");
		McEliece.print(encrypted);

		// DECRYPTION
		int[] decrypted = mcEliece.pattersonDecode(encrypted, mcEliece.parityCheck);
		System.out.println("Message = Decrypted?");
		System.out.println(McEliece.vecEquals(toEncrypt, decrypted));

		//		StringBuilder sb = new StringBuilder(toEncrypt.length);
		//		for (int i = 0; i < toEncrypt.length; i++)
		//			sb.append(toEncrypt[i] == 0 ? '0' : '1');
		//		byte[] bval = new BigInteger(sb.toString(), 2).toByteArray();
		//		System.out.println("toEncrypt = " + new String(bval, StandardCharsets.UTF_8));
		//		
		//		sb = new StringBuilder(decrypted.length);
		//		for (int i = 0; i < decrypted.length; i++)
		//			sb.append(decrypted[i] == 0 ? '0' : '1');
		//		bval = new BigInteger(sb.toString(), 2).toByteArray();
		//		System.out.println("decrypt = " + new String(bval, StandardCharsets.UTF_8));
	}
}
