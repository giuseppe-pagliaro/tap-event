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

    var thumbnailUri: String? = null
) {
    val thumbnail: Uri
        get() = Uri.parse(thumbnailUri ?: Product.DEFAULT_THUMBNAIL_URL)

    abstract fun getTicketAmount(count: Int = 1): Int
    abstract fun getPriceStr(count: Int = 1): String
}
