package com.pignier.instagramdm.Login;

import java.security.GeneralSecurityException;
import java.util.Arrays;




/**
 * Example how to open sealed boxes in pure java (libsodium sealed boxes according to 
 * https://download.libsodium.org/doc/public-key_cryptography/sealed_boxes.html)
 * 
 * Has a dependency on TweetNaclFast and Blake2B, for example
 * 
 * https://github.com/alphazero/Blake2b
 * and
 * https://github.com/InstantWebP2P/tweetnacl-java
 * 
 */

//https://stackoverflow.com/questions/42456624/how-can-i-create-or-open-a-libsodium-compatible-sealed-box-in-pure-java/42456750#42456750
public class SealedBoxUtility {


public static final int crypto_box_NONCEBYTES = 24;
public static final int crypto_box_PUBLICKEYBYTES = 32;
public static final int crypto_box_MACBYTES = 16;
public static final int crypto_box_SEALBYTES = (crypto_box_PUBLICKEYBYTES + crypto_box_MACBYTES);

//  libsodium
//  int crypto_box_seal(unsigned char *c, const unsigned char *m,
//            unsigned long long mlen, const unsigned char *pk);


/**
 * Encrypt in  a sealed box
 *
 * @param clearText clear text
 * @param receiverPubKey receiver public key
 * @return encrypted message
 * @throws GeneralSecurityException 
 */
public static byte[] crypto_box_seal(byte[] clearText, byte[] receiverPubKey) throws GeneralSecurityException {

	// create ephemeral keypair for sender
	TweetNaclFast.Box.KeyPair ephkeypair = TweetNaclFast.Box.keyPair();
	// create nonce
	byte[] nonce = crypto_box_seal_nonce(ephkeypair.getPublicKey(), receiverPubKey);
	TweetNaclFast.Box box = new TweetNaclFast.Box(receiverPubKey, ephkeypair.getSecretKey(),68);
	byte[] ciphertext = box.box(clearText,0,clearText.length, nonce);
	if (ciphertext == null) throw new GeneralSecurityException("could not create box");

	byte[] sealedbox = new byte[ciphertext.length + crypto_box_PUBLICKEYBYTES];
	for (int i = 0; i < crypto_box_PUBLICKEYBYTES; i ++)
		sealedbox[i] = ephkeypair.getPublicKey()[i];

	for(int i = 0; i < ciphertext.length; i ++)
		sealedbox[i+crypto_box_PUBLICKEYBYTES]=ciphertext[i];

	return sealedbox;
}



/**
 *  hash the combination of senderpk + mypk into nonce using blake2b hash
 * @param senderpk the senders public key
 * @param mypk my own public key
 * @return the nonce computed using Blake2b generic hash
 */
public static byte[] crypto_box_seal_nonce(byte[] senderpk, byte[] mypk){
// C source ported from libsodium
//      crypto_generichash_state st;
//
//      crypto_generichash_init(&st, NULL, 0U, crypto_box_NONCEBYTES);
//      crypto_generichash_update(&st, pk1, crypto_box_PUBLICKEYBYTES);
//      crypto_generichash_update(&st, pk2, crypto_box_PUBLICKEYBYTES);
//      crypto_generichash_final(&st, nonce, crypto_box_NONCEBYTES);
//
//      return 0;
	final Blake2b blake2b = Blake2b.Digest.newInstance( crypto_box_NONCEBYTES ); 
	blake2b.update(senderpk);
	blake2b.update(mypk);
	byte[] nonce = blake2b.digest();
	if (nonce == null || nonce.length!=crypto_box_NONCEBYTES) throw new IllegalArgumentException("Blake2b hashing failed");
	return nonce;


}

}