@file:JvmName("SslUtils")

package ext.android.foundation.network


import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

fun trustAllOkHttpClient(): OkHttpClient {
    val trustManager = TrustAllTrustManager()
    val sslContext = getSSLContext(arrayOf(trustManager), null, null)
    val sslSocketFactory = sslContext.socketFactory
    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(HostnameVerifier { _, _ -> true })
        .build()
}

fun trustAllSSLContext(): SSLContext {
    val trustManager = TrustAllTrustManager()
    return getSSLContext(arrayOf(trustManager), null, null)
}

fun trustOkHttpClient(vararg certificates: InputStream): OkHttpClient {
    val trustManager = getX509TrustManager(*certificates)
    val sslContext = getSSLContext(arrayOf(trustManager), null, null)
    val sslSocketFactory = sslContext.socketFactory
    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustManager)
        .build()
}

fun trustMeSSLContext(vararg certificates: InputStream): SSLContext {
    val trustManager = getX509TrustManager(*certificates)
    return getSSLContext(arrayOf(trustManager), null, null)
}

fun getSSLSocketFactory(
    certificates: Array<out InputStream>,
    pkcsFile: InputStream?,
    password: String?
): SSLSocketFactory {
    try {
        val sslContext = getSSLContext(certificates, pkcsFile, password)
        return sslContext.socketFactory
    } catch (e: Throwable) {
        e.printStackTrace()
        throw RuntimeException(e)
    }
}

@Throws(RuntimeException::class)
fun getX509TrustManager(vararg certificates: InputStream): X509TrustManager {
    val trustManagers = generateTrustManagers(*certificates)
    return if (trustManagers.isNotEmpty()) {
        X509TrustManagerImpl(getX509TrustManager(trustManagers))
    } else {
        TrustAllTrustManager()
    }
}

@Throws(RuntimeException::class)
fun getSSLContext(
    certificates: Array<out InputStream>,
    pkcsFile: InputStream?,
    password: String?
): SSLContext =
    SSLContext.getInstance("TLS").apply {
        val keyManagers = generateKeyManagers(pkcsFile, password)
        val trustManager: TrustManager = getX509TrustManager(*certificates)
        this.init(keyManagers, arrayOf(trustManager), SecureRandom())
    }

@Throws(RuntimeException::class)
fun getSSLContext(
    trustManagers: Array<out TrustManager>,
    pkcsFile: InputStream?,
    password: String?
): SSLContext =
    SSLContext.getInstance("TLS").apply {
        val keyManagers = generateKeyManagers(pkcsFile, password)
        this.init(keyManagers, trustManagers, SecureRandom())
    }

private fun generateTrustManagers(vararg certificateStreams: InputStream): Array<TrustManager> {
    try {
        // fix Android P. BC provider no longer supported after Android P
        //CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        certificateStreams.forEachIndexed { i, certificate ->
            val certificateAlias = i.toString() + ""
            val x509 = certificateFactory.generateCertificate(certificate) as X509Certificate
            keyStore.setCertificateEntry(certificateAlias, x509)
            certificate.close()
        }
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        return trustManagerFactory.trustManagers
    } catch (e: CertificateException) {
        throw RuntimeException(e)
    } catch (e: KeyStoreException) {
        throw RuntimeException(e)
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException(e)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}

private fun generateKeyManagers(pkcsFile: InputStream?, password: String?): Array<KeyManager>? {
    if (pkcsFile == null || password == null) {
        return null
    }
    try {
        val clientKeyStore = KeyStore.getInstance("PKCS12")
        clientKeyStore.load(pkcsFile, password.toCharArray())
        val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(clientKeyStore, password.toCharArray())
        return keyManagerFactory.keyManagers
    } catch (e: KeyStoreException) {
        throw RuntimeException(e)
    } catch (e: CertificateException) {
        throw RuntimeException(e)
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException(e)
    } catch (e: IOException) {
        throw RuntimeException(e)
    } catch (e: UnrecoverableKeyException) {
        throw RuntimeException(e)
    }
}

private fun getX509TrustManager(trustManagers: Array<TrustManager>?): X509TrustManager? {
    if (trustManagers == null) {
        return null
    }
    return trustManagers.first { it is X509TrustManager } as? X509TrustManager
}

private class X509TrustManagerImpl @Throws(
    NoSuchAlgorithmException::class,
    KeyStoreException::class
)
constructor(private val localTrustManager: X509TrustManager?) : X509TrustManager {

    private val defaultTrustManager: X509TrustManager?

    init {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        this.defaultTrustManager = getX509TrustManager(trustManagerFactory.trustManagers)
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        try {
            if (this.defaultTrustManager != null) {
                this.defaultTrustManager.checkClientTrusted(chain, authType)
            } else {
                checkClientTrustedWithLocal(chain, authType)
            }
        } catch (e: CertificateException) {
            checkClientTrustedWithLocal(chain, authType)
        }
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        try {
            if (this.defaultTrustManager != null) {
                this.defaultTrustManager.checkServerTrusted(chain, authType)
            } else {
                checkServerTrustedWithLocal(chain, authType)
            }
        } catch (e: CertificateException) {
            checkServerTrustedWithLocal(chain, authType)
        }
    }

    @Throws(CertificateException::class)
    private fun checkServerTrustedWithLocal(chain: Array<X509Certificate>, authType: String) {
        if (this.localTrustManager != null) {
            this.localTrustManager.checkServerTrusted(chain, authType)
        }
    }

    @Throws(CertificateException::class)
    private fun checkClientTrustedWithLocal(chain: Array<X509Certificate>, authType: String) {
        if (this.localTrustManager != null) {
            this.localTrustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}

private class TrustAllTrustManager : X509TrustManager {

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, authType: String) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}


