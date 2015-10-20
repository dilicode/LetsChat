package com.mstr.letschat.utils;

import android.content.Context;
import android.util.Base64;

import com.mstr.letschat.R;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by dilli on 10/20/2015.
 */
public class AESEncryption {
    private static final int KEY_SIZE = 128;

    private static final String KEY_GENERATOR_ALGORITHM = "AES";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static final byte[] iv = {1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8};

    private static SecretKey getSecretKey(Context context) throws NoSuchAlgorithmException {
        final String preferenceKey = context.getString(R.string.secret_key_preference);
        String secretKeyString = PreferenceUtils.getSharedPreferences(context).getString(preferenceKey, null);
        if (secretKeyString != null) {
            byte[] bytes = Base64.decode(secretKeyString, Base64.DEFAULT);
            return new SecretKeySpec(bytes, AESEncryption.KEY_GENERATOR_ALGORITHM);
        } else {
            SecretKey secretKey = newSecretKey();
            secretKeyString = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            PreferenceUtils.getSharedPreferences(context).edit().putString(preferenceKey, secretKeyString).commit();

            return secretKey;
        }
    }

    private static SecretKey newSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_GENERATOR_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    public static String encrypt(Context context, String data) {
        try {
            SecretKey secretKey = getSecretKey(context);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] transformedBytes = cipher.doFinal(data.getBytes());

            return Base64.encodeToString(transformedBytes, Base64.DEFAULT);
        } catch(NoSuchAlgorithmException e) {
            return data;
        } catch(NoSuchPaddingException e){
            return data;
        } catch (InvalidKeyException e) {
            return data;
        } catch(IllegalBlockSizeException e) {
            return data;
        } catch(BadPaddingException e) {
            return data;
        } catch(InvalidAlgorithmParameterException e) {
            return data;
        }
    }

    public static String decrypt(Context context, String data) {
        try {
            SecretKey secretKey = getSecretKey(context);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            return new String(cipher.doFinal(bytes));
        } catch(NoSuchAlgorithmException e){
            return data;
        } catch(NoSuchPaddingException e) {
            return data;
        } catch(IllegalBlockSizeException e) {
            return data;
        } catch(BadPaddingException e) {
            return data;
        } catch(InvalidKeyException e) {
            return data;
        } catch(InvalidAlgorithmParameterException e) {
            return data;
        }
    }
}