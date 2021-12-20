package main;

import ecdsa.Ecdsa;
import ecdsa.PrivateKey;
import ecdsa.PublicKey;
import ecdsa.Signature;

public class M_Ecdsa {

	private PrivateKey m_private_key;

	private PublicKey m_public_key;
	private PublicKey o_public_key;

	public M_Ecdsa() {
		m_private_key = new PrivateKey();
		m_public_key = m_private_key.publicKey();
	}

	public PublicKey getMyPublicKey() {
		return m_public_key;
	}

	public void setOtherPublicKey(PublicKey o_public_key) {
		this.o_public_key = o_public_key;
	}

	// sign with sender's private key
	public Signature signMessage(byte[] msg) {
		String message = Utils.byteArrToString(msg);
		return Ecdsa.sign(message, m_private_key);
	}

	// verify with sender's public key
	public boolean verifyMessage(byte[] msg, Signature signature) {
		String message = Utils.byteArrToString(msg);
		return Ecdsa.verify(message, signature, o_public_key);
	}
}
