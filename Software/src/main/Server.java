package main;

import java.util.ArrayList;
import java.util.List;

import ecdsa.PublicKey;
import ecdsa.Signature;

public class Server {

	private List<Password> passwords; // most updated

	private M_RabbitCipher m_rabbitCipher;
	private M_McEliece m_mcEliece;
	private M_Ecdsa m_ecdsa;

	public Server() {
		passwords = new ArrayList<>();
		m_rabbitCipher = new M_RabbitCipher();
		m_mcEliece = new M_McEliece();
		m_ecdsa = new M_Ecdsa();

		passwords.add(new Password("Google","alice@gmail.com","iLoveCrypto!"));
		passwords.add(new Password("Yahoo","alice9231","206598535"));
	}

	public void sendPublicKeysToClient(Client client) {
		client.receivePublicKeysFromServer(m_mcEliece.getMyPublicKey(), m_ecdsa.getMyPublicKey());
	}

	public void receivePublicKeysFromClient(int[][] o_mceliece_public_key, PublicKey o_ecdsa_public_key) {
		m_mcEliece.setOtherPublicKey(o_mceliece_public_key);
		m_ecdsa.setOtherPublicKey(o_ecdsa_public_key);
	}

	public void sendMessageToClient(Client client, String message) {
		// get rabbit_key
		int[] rabbit_key = m_rabbitCipher.generateCipherKey();

		// use rabbit_key to encrypt message
		byte[] encrypted_message = m_rabbitCipher.encryptMessage(message, rabbit_key);

		// encrypt rabbit_key with Client's McEliece public_key
		List<int[]> encrypted_rabbit_key = m_mcEliece.encryptCipherKey(rabbit_key);

		// sign message
		Signature signature = m_ecdsa.signMessage(encrypted_message);

		// display sent message
		System.out.println("Server sent: " + message);

		// send message to server
		client.receiveMessageFromServer(encrypted_message, encrypted_rabbit_key, signature);
	}

	public void receiveMessageFromClient(Client client, byte[] encrypted_message, List<int[]> encrypted_rabbit_key, Signature signature) {
		// verify message from Client
		boolean verification = m_ecdsa.verifyMessage(encrypted_message, signature);
		if (!verification) {
			System.out.println("Server did not verify message from Client\n");
			return;
		}

		// decrypt rabbit_key
		int[] cipher_key = m_mcEliece.decryptCipherKey(encrypted_rabbit_key);

		// use rabbit_key to decrypt message
		String message = m_rabbitCipher.decryptMessage(encrypted_message, cipher_key);

		// display received message
		System.out.println("Server received: " + message);

		// handle received message
		if (message.equals("get passwords")) {
			sendMessageToClient(client, passwords.toString());

		} else if (message.startsWith("add")) {
			String[] listString = message.split("add ");
			passwords.add(Utils.parseStringToPassword(listString[1]));

		} else if (message.startsWith("edit")) {
			String[] listString = message.split("edit ");
			passwords = Utils.parseStringToPasswords(listString[1]);

		} else if (message.startsWith("delete")) {
			String[] listString = message.split("delete ");
			passwords.remove(Integer.parseInt(listString[1]));
		}
	}
}
