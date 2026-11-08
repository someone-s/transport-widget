package com.eden.livewidget.data.agency.common.keys

import android.content.Context
import android.provider.Settings.Secure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class ObscuredKeyProvider(
    private val context: Context,
    private val id: String,
): KeyProvider {

    private val keyDirName = "keys"

    // Get the file for this key, file may not exist
    private fun getFile(): File {

        // Context create directory if not found
        val keyDir = context.getDir(keyDirName, Context.MODE_PRIVATE)

        val keyFile = File(keyDir, id)

        return keyFile
    }

    private val cryptKeyAlgo = "PBKDF2WithHmacSHA256"
    private val cryptTransform = "AES/GCM/NoPadding"
    // IV size of using AES-GCM
    private val cryptInitVectorSize = 12
    private val cryptTLen = 128
    private fun getCryptKey(): SecretKey {

        val cryptFactory = SecretKeyFactory.getInstance(cryptKeyAlgo)
        val cryptSpec = PBEKeySpec(Secure.ANDROID_ID.toCharArray(), id.toByteArray(), 65536, 256)
        val cryptKey = SecretKeySpec(cryptFactory.generateSecret(cryptSpec).encoded, "AES")

        return cryptKey
    }

    private val _key by lazy { MutableStateFlow(getKey()) }
    override val key: StateFlow<String> by lazy { _key }

    private fun getKey(): String {
        val keyFile = getFile()
        if (!keyFile.exists())
            return ""

        val keyBytes = keyFile.readBytes()

        // Must have at least bytes for IV
        assert(keyBytes.size >= cryptInitVectorSize)

        val initVector = ByteArray(cryptInitVectorSize)
        System.arraycopy(keyBytes, 0, initVector, 0, cryptInitVectorSize)

        val remainingSize = keyBytes.size - cryptInitVectorSize
        val cryptMessage = ByteArray(remainingSize)
        System.arraycopy(keyBytes, cryptInitVectorSize, cryptMessage, 0, remainingSize)

        val gcmSpec = GCMParameterSpec(cryptTLen, initVector)

        val cipher = Cipher.getInstance(cryptTransform)
        cipher.init(Cipher.DECRYPT_MODE, getCryptKey(), gcmSpec)
        val fetchedKey = String(cipher.doFinal(cryptMessage))

        return fetchedKey
    }

    override fun refreshKey(): String {
        val fetchedKey = getKey()
        _key.tryEmit(fetchedKey)
        return fetchedKey
    }

    override fun setKey(newKey: String) {
        val keyFile = getFile()

        val secureRandom = SecureRandom()
        val initVector = ByteArray(cryptInitVectorSize)
        secureRandom.nextBytes(initVector)
        val gcmSpec = GCMParameterSpec(cryptTLen, initVector)

        val cipher = Cipher.getInstance(cryptTransform)
        cipher.init(Cipher.ENCRYPT_MODE, getCryptKey(), gcmSpec)
        val cryptMessage = cipher.doFinal(newKey.toByteArray())

        // AES-GCM IV must be of length cryptInitVectorSize which is 12
        assert(initVector.size == cryptInitVectorSize)

        // Concat the byte arrays and write
        keyFile.writeBytes(initVector + cryptMessage)

        _key.tryEmit(newKey)
    }
}