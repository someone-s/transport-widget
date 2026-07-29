package com.eden.livewidget

import android.content.Context
import com.eden.livewidget.data.keys.ObscuredKeyProvider
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ObscuredKeyProviderUnitTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun noFile_isEmpty() {
        val provider = ObscuredKeyProvider("testId")

        val noFile = provider.getKey(mockContext)
        println(noFile)
    }

    @Test
    fun encryptDecrypt_isMatch() {
        val provider = ObscuredKeyProvider("testId")

        val testKey = "abcdefg12345!!!!!"
        provider.setKey(mockContext, testKey)
        val resultKey = provider.getKey(mockContext)

        assertEquals(testKey, resultKey)
    }
}