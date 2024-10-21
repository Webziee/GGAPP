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
)

data class BookedResponse(
    val id: Int,
    val unit_number: String,
    val start_date: String,
    val end_date: String,
    val user_email: String,
    val unitImages: String? = null,
    val payment_status: String? = null,
)

