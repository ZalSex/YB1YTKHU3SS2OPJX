package com.cloudsync.pro.mobile

import android.content.Context
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AssetCrypto {

    fun open(context: Context, fileName: String): ByteArray {
        return context.assets.open(fileName).readBytes()
    }

    fun openStream(context: Context, fileName: String): InputStream =
        open(context, fileName).inputStream()
}
