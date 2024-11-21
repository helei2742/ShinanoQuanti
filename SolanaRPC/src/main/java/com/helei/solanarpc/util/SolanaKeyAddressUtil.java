
package com.helei.solanarpc.util;


import cn.hutool.core.codec.Base58;
import cn.hutool.core.lang.Pair;
import com.portto.solana.web3.Account;
import com.portto.solana.web3.util.TweetNaclFast;

public class SolanaKeyAddressUtil {


    /**
     * 从私匙中获取公匙和密匙
     *
     * @param privateKey privateKey
     * @return (publicKey, secretKey)
     */
    public static Pair<String, String> getPSKeyFromPrivateKey(String privateKey) {
        byte[] decode = Base58.decode(privateKey);
        Account account = new Account(TweetNaclFast.Signature.keyPair_fromSecretKey(decode));
        return new Pair<>(account.getPublicKey().toBase58(), Base58.encode(account.getSecretKey()));
    }

}
