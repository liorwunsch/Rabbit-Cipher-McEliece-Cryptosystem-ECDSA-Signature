package main;

import java.util.List;

import ecdsa.PublicKey;
import ecdsa.Signature;

public class Client {
	private List<Password> passwords; // can be outdated

	private M_RabbitCipher m_rabbitCipher;
	private M_McEliece m_mcEliece;
	private M_Ecdsa m_ecdsa;

	public Client() {
		m_rabbitCipher = new M_RabbitCipher();
		m_mcEliece = new M_McEliece();
		m_ecdsa = new M_Ecdsa();
	}

	public List<Password> getPasswords() {
		return passwords;
	}

	public void sendPublicKeysToServer(Server server) {
		server.receivePublicKeysFromClient(m_mcEliece.getMyPublicKey(), m_ecdsa.getMyPublicKey());
	}

	public void receivePublicKeysFromServer(int[][] o_mceliece_public_key, PublicKey o_ecdsa_public_key) {
		m_mcEliece.setOtherPublicKey(o_mceliece_public_key);
		m_ecdsa.setOtherPublicKey(o_ecdsa_public_key);
	}

	public void sendMessageToServer(Server server, String message) {
		// get rabbit_key
		int[] rabbit_key = m_rabbitCipher.generateCipherKey();

		// use rabbit_key to encrypt message
		byte[] encrypted_message = m_rabbitCipher.encryptMessage(message, rabbit_key);

		// encrypt rabbit_key with Server's McEliece public_key
		List<int[]> encrypted_rabbit_key = m_mcEliece.encryptCipherKey(rabbit_key);

		// sign message
		Signature signature = m_ecdsa.signMessage(encrypted_message);

		// display sent message
		System.out.println("Client sent: " + message);

		// send message to server
		server.receiveMessageFromClient(this, encrypted_message, encrypted_rabbit_key, signature);
	}

	public void receiveMessageFromServer(byte[] encrypted_message, List<int[]> encrypted_rabbit_key, Signature signature) {
		// verify message from Server
		boolean verification = m_ecdsa.verifyMessage(encrypted_message, signature);
		if (!verification) {
			System.out.println("Client did not verify message from Server\n");
			return;
		}

		// decrypt rabbit_key
		int[] rabbit_key = m_mcEliece.decryptCipherKey(encrypted_rabbit_key);

		// use rabbit_key to decrypt message
		String message = m_rabbitCipher.decryptMessage(encrypted_message, rabbit_key);

		// display received message
		System.out.println("Client received: " + message + "\n");

		// handle received message
		if (message.startsWith("[Password")) {
			passwords = Utils.parseStringToPasswords(message);
		}
	}
}
