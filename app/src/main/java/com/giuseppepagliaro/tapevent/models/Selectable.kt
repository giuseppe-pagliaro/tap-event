package com.giuseppepagliaro.tapevent.models

import android.net.Uri
import androidx.room.Ignore
import com.giuseppepagliaro.tapevent.entities.Product

// Le proprietà sono pubbliche per essere accessibili dal Room ORM.
@Suppress("MemberVisibilityCanBePrivate")
abstract class Selectable(
    val name: String,
    @Ignore
    val currencyName: String,
    @Ignore
    val thumbnailUri: String? = null
) {
    val thumbnail: Uri
        get() = Uri.parse(thumbnailUri ?: Product.DEFAULT_THUMBNAIL_URL)

    abstract fun getPrice(count: Int = 1): String
}
