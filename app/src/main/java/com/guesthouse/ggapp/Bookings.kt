data class Bookings(
    val id: Int? = null,
    val unitNumber: String = "",
    val price: Int = 0,
    val sleeper: Int = 0,
    val status: String = "",
    val unitImages: String? = null
)

data class BookedRequest(
    val unit_number: String,
    val start_date: String,
    val end_date: String,
    val user_email: String,
    val total_amount: Long,
)

data class BookedResponse(
    val id: Int,
    val unit_number: String,
    val start_date: String,
    val end_date: String,
    val user_email: String,
    val unitImages: String? = null,
    var payment_status: String? = null,
    var isCanceled: Boolean = false,
    val removed: Boolean = false
)


