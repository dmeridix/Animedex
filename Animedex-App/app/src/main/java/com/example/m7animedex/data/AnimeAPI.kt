package com.example.m7animedex.data

// data/api/AnimeAPI.kt
import com.example.m7animedex.data.api.AnimeService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class AnimeAPI {
    companion object {
        private var mAPI: AnimeService? = null

        @Synchronized
        fun getService(): AnimeService {
            if (mAPI == null) {
                val client: OkHttpClient = getUnsafeOkHttpClient()
                val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create()

                mAPI = Retrofit.Builder()
                    .baseUrl("https://3.217.167.4/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(AnimeService::class.java)
            }
            return mAPI!!
        }

        private fun getUnsafeOkHttpClient(): OkHttpClient {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { hostname, session -> true }

                return builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}