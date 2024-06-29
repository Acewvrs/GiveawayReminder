package com.example.giveawayreminder.data

import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class QueryResponse(
    val data: Data
)

@Serializable
data class Data(
    @SerializedName("Catalog")
    val catalog: Catalog
)

@Serializable
data class Catalog(
    val searchStore: SearchStore
)

@Serializable
data class SearchStore(
    @SerializedName("elements")
    val gamesList: MutableList<Game>
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getGamesCurrentlyOnPromotion(): List<Game> {
        val currentTime = ZonedDateTime.now().toString()

        // games on display must currently be on promotion and it must be free after discount
        gamesList.removeAll {
            val startTime = it.promotions?.promotionalOffers?.getOrNull(0)?.offers?.getOrNull(0)?.startDate ?: currentTime
            val endTime = it.promotions?.promotionalOffers?.getOrNull(0)?.offers?.getOrNull(0)?.endDate ?: currentTime

            val result = compareZonedDateTimes(currentTime, startTime)
            val result2 = compareZonedDateTimes(currentTime, endTime)

            val gameDiscountPrice = it.price?.totalPrice?.discountPrice
            result2 >= 0 || result <= 0 || gameDiscountPrice != 0
        }
        return gamesList
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun compareZonedDateTimes(dateTime1: String, dateTime2: String): Int {
    val dt1 = ZonedDateTime.parse(dateTime1)
    val dt2 = ZonedDateTime.parse(dateTime2)
    return dt1.compareTo(dt2)
}

// Game data received from web
@Serializable
data class Game(
    val id: String,
    val title: String,

    @SerializedName("keyImages")
    val imgUrls: List<ImageUrl>,

    val price: Price? = null,
    val promotions: Promotions? = null
) {
    fun getThumbnailUrl(): String? {
        for (url in imgUrls) {
            if (url.type == "OfferImageWide") {
                return url.url
            }
        }
        return null
    }

    fun getCurrentPromotionEndDate(): String {
        val unformattedDate = promotions?.promotionalOffers?.getOrNull(0)?.offers?.getOrNull(0)?.endDate ?: ""

        return if (unformattedDate == "") unformattedDate
        else {
            return formatDate(unformattedDate)
        }
    }

    //
    fun getSalePercentage(): String {
        return if (price?.totalPrice?.discountPrice == 0) return "100%"
        else {
            val percentage = promotions?.promotionalOffers?.getOrNull(0)?.offers?.getOrNull(0)?.discountSetting?.discountPercentage

            if (percentage == null) return ""
            return String.format("%d%%", percentage)
        }
    }

    // the api has games that are marked to have the original cost of $0 when they are clearly not free before promo
    // hide the prices of these games as they're misleading
    fun getOriginalPrice(): String {
        return if (price?.totalPrice?.originalPrice == 0) return ""
        else {
            val origPrice = price?.totalPrice?.originalPrice
            val decimals = price?.totalPrice?.currencyInfo?.decimals ?: 2 // number of decimal places for price

            if (origPrice == null) return ""

            val offset = origPrice.toString().length - decimals + 1 // determines where the decimal point should go
            val result = StringBuilder(String.format("$%d", origPrice)).insert(offset,'.')
            return result.toString()
        }
    }

    fun getDiscountPrice(): String {
        if (price?.totalPrice?.discountPrice == 0) return "Free"
        return String.format("$%.2f", price?.totalPrice?.discountPrice?.toDouble())
    }

    // Convert the string format of ZonedDateTime to 'YYYY/MM/DD'
    // e.g. 2024-06-13T15:00:00.000Z -> 2024/06/13
    fun formatDate(unformattedDate: String): String {
        return unformattedDate.take(10).replace('-', '/')
    }
}

@Serializable
data class ImageUrl(
    val type: String,
    val url: String
)

@Serializable
data class Price(
    val totalPrice: TotalPrice,
)

@Serializable
data class TotalPrice(
    val originalPrice: Int,
    val discountPrice: Int,
    val currencyInfo: CurrencyInfo
)

@Serializable
data class CurrencyInfo(
    val decimals: Int
)

@Serializable
data class PromotionalOffers(
    @SerializedName("promotionalOffers")
    val offers: List<Offer>?
)

@Serializable
data class Promotions(
    val promotionalOffers: List<PromotionalOffers>
)

@Serializable
data class Offer(
    val startDate: String,
    val endDate: String,
    val discountSetting: DiscountSetting
)

@Serializable
data class DiscountSetting(
    val discountPercentage: Int
)

// Game Table Entity
@Entity("free_games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    @NonNull
    val title: String,
)