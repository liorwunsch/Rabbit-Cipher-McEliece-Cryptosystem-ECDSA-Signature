package rabbit;

import java.util.Arrays;

/**
 * Rabbit stream cipher implementation.
 * @link http://tools.ietf.org/rfc/rfc4503.txt
 * Currently IV usage not implemented.
 *
 * Usage:
 *
 *  byte[] msg = "Hello World!".getBytes();
 *
 *  RabbitCipher cipher = new RabbitCipher();
 *  cipher.setupKey(key);
 *  cipher.crypt(msg);
 *
 *
 * Created by Nikita Timofeev on 20.04.15.
 */
public final class RabbitCipher {

	private final static int[] A = { // constant coefficients
			0x4D34D34D, 0xD34D34D3,
			0x34D34D34, 0x4D34D34D,
			0xD34D34D3, 0x34D34D34,
			0x4D34D34D, 0xD34D34D3
	};
	private final int[] X = new int[8]; // variables
	private final int[] C = new int[8]; // counters
	private int b;

	private boolean ready = false;

	private final int[] G = new int[8]; // generator
	private final byte[] S = new byte[16]; // cipher key stream

	private static final int BLOCK_LENGTH = 16; // key length

	/**
	 * Original byte array used to return encrypted bytes
	 * @param message (encrypted) message to be (de-)encrypted
	 */
	public void crypt(byte[] message) {
		if (!ready)
			throw new IllegalStateException("Key is not setup. You need to call setupKey() prior encrypting data.");

		for (int i = 0; i < message.length; i++) {
			if (i % BLOCK_LENGTH == 0)
				nextBlock();
			message[i] ^= S[i % BLOCK_LENGTH];
		}
	}

	/**
	 * @param key 128 bit key (16 bytes)
	 */
	public void setupKey(byte[] key_) {
		assert key_.length >= BLOCK_LENGTH;

		byte[] key = new byte[16];
		if (key_.length < BLOCK_LENGTH) {
			for (int i = 0; i < key_.length; i++)
				key[i] = key_[i];
		} else {
			key = key_.clone();		
		}

		int[] K = new int[8];
		for (int i = 0; i < 8; i++)
			K[i] = (key[2 * i + 1] << 8) | (key[2 * i] & 0xff);

		// x(*,0)
		for (int i = 0; i < 8; i++) {
			if ((i & 1) == 0) {
				X[i] = (K[(i + 1) % 8] << 16) | (K[i] & 0xFFFF);
				C[i] = (K[(i + 4) % 8] << 16) | (K[(i + 5) % 8] & 0xFFFF);
			} else {
				X[i] = (K[(i + 5) % 8] << 16) | (K[(i + 4) % 8] & 0xFFFF);
				C[i] = (K[i] << 16) | (K[(i + 1) % 8] & 0xFFFF);
			}
		}

		// x(*,1:4)
		nextState();
		nextState();
		nextState();
		nextState();

		// c(*,4) reinitialized
		for (int i = 0; i < 8; i++)
			C[i] = C[i] ^ X[(i+4) % 8];

		ready = true;
	}

	public void setupIV(byte[] IV) {
		// TODO - default IV = 0
		/*
        C[0] = C[0] ^ IV[31..0];
        C[1] = C[1] ^ (IV[63..48] || IV[31..16]);
        C[2] = C[2] ^ IV[63..32];
        C[3] = C[3] ^ (IV[47..32] || IV[15..0]);
        C[4] = C[4] ^ IV[31..0];
        C[5] = C[5] ^ (IV[63..48] || IV[31..16]);
        C[6] = C[6] ^ IV[63..32];
        C[7] = C[7] ^ (IV[47..32] || IV[15..0]);
		 */
	}

	/**
	 * After reset key must be setup again
	 */
	public void reset() {
		Arrays.fill(X, 0);
		Arrays.fill(C, 0);
		Arrays.fill(S, (byte)0);
		b = 0;
		ready = false;
	}

	/**
	 * Package private access for tests
	 */
	byte[] nextBlock() {
		nextState();

		int x = X[0] ^ X[5] >>> 16;
				S[0] = (byte) x;
				S[1] = (byte) (x >> 8);

				x = X[0] >>> 16 ^ X[3];
				S[2] = (byte) x;
				S[3] = (byte) (x >> 8);

				x = X[2] ^ X[7] >>> 16;
			S[4] = (byte) x;
			S[5] = (byte) (x >> 8);

			x = X[2] >> 16 ^ X[5];
			S[6] = (byte) x;
			S[7] = (byte) (x >> 8);

			x = X[4] ^ X[1] >>> 16;
			S[8] = (byte) x;
			S[9] = (byte) (x >> 8);

			x = X[4] >>> 16 ^ X[7];
			S[10] = (byte) x;
			S[11] = (byte) (x >> 8);

			x = X[6] ^ X[3] >>> 16;
			S[12] = (byte) x;
			S[13] = (byte) (x >> 8);

			x = X[6] >>> 16 ^ X[1];
			S[14] = (byte) x;
			S[15] = (byte) (x >> 8);

			return S;
	}

	private void nextState() {
		long temp;
		for (int i = 0; i < 8; i++) {
			temp = (C[i] & 0xFFFFFFFFl) + (A[i] & 0xFFFFFFFFl) + b;
			b = (int) (temp >>> 32);
			C[i] = (int) (temp & 0xFFFFFFFFl);
		}

		for (int i = 0; i < 8; i++)
			G[i] = g(X[i], C[i]);

		X[0] = G[0] + rotate(G[7], 16) + rotate(G[6], 16);
		X[1] = G[1] + rotate(G[0], 8) + G[7];
		X[2] = G[2] + rotate(G[1], 16) + rotate(G[0], 16);
		X[3] = G[3] + rotate(G[2], 8) + G[1];
		X[4] = G[4] + rotate(G[3], 16) + rotate(G[2], 16);
		X[5] = G[5] + rotate(G[4], 8) + G[3];
		X[6] = G[6] + rotate(G[5], 16) + rotate(G[4], 16);
		X[7] = G[7] + rotate(G[6], 8) + G[5];
	}

	private int g(int u, int v) {
		long square = u + v & 0xFFFFFFFFl;
		square *= square;
		return (int) (square ^ square >>> 32);
	}

	/**
	 * Left circular bit shift
	 * @param value
	 * @param shift
	 * @return
	 */
	static private int rotate(int value, int shift) {
		return (value << shift | value >>> (32 - shift));
	}
}
