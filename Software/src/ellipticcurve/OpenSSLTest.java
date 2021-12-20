package ellipticcurve;

import ecdsa.utils.ByteString;
import ecdsa.*;
import org.junit.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import static org.junit.Assert.assertTrue;

public class OpenSSLTest {

    @Test
    public void testAssign() throws URISyntaxException, IOException {
        // Generated by:openssl ecparam - name secp256k1 - genkey - out privateKey.pem
        String privateKeyPem = Utils.readFileAsString("privateKey.pem");

        PrivateKey privateKey = PrivateKey.fromPem(privateKeyPem);

        String message = Utils.readFileAsString("message.txt");

        Signature signature = Ecdsa.sign(message, privateKey);

        PublicKey publicKey = privateKey.publicKey();

        assertTrue(Ecdsa.verify(message, signature, publicKey));
    }

    @Test
    public void testVerifySignature() throws IOException, URISyntaxException {
        // openssl ec -in privateKey.pem - pubout - out publicKey.pem
        String publicKeyPem = Utils.readFileAsString("publicKey.pem");
        
        // openssl dgst -sha256 -sign privateKey.pem -out signature.binary message.txt
        ByteString signatureBin = new ByteString(Utils.readFileAsBytes("signature.binary"));

        String message = Utils.readFileAsString("message.txt");

        PublicKey publicKey = PublicKey.fromPem(publicKeyPem);

        Signature signature = Signature.fromDer(signatureBin);

        assertTrue(Ecdsa.verify(message, signature, publicKey));
    }
}
