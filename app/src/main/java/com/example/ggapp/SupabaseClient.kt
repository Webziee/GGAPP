import com.example.goergesgraceapp.SupabaseApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private const val BASE_URL = "https://kdbbxrjosejndmanzish.supabase.co/rest/v1/"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtkYmJ4cmpvc2VqbmRtYW56aXNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjg0ODU3NTYsImV4cCI6MjA0NDA2MTc1Nn0.gvpWqWCYQKsr29GvXjYheYjvaVlv322X8gk1ccMPths"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SupabaseApi = retrofit.create(SupabaseApi::class.java)

    fun getApiKey(): String = API_KEY
}

