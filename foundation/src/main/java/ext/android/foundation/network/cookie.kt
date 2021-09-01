package ext.android.foundation.network

import android.content.Context
import androidx.core.content.edit
import ext.android.foundation.extensions.hex
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okio.ByteString.Companion.decodeHex
import java.io.*
import java.util.concurrent.ConcurrentHashMap

class PersistCookie(@Transient private val srcCookie: Cookie) : Serializable {
    private val name: String = srcCookie.name
    private val value: String = srcCookie.value
    private val expiresAt: Long = srcCookie.expiresAt
    private val domain: String = srcCookie.domain
    private val path: String = srcCookie.path
    private val secure: Boolean = srcCookie.secure
    private val httpOnly: Boolean = srcCookie.httpOnly
    private val persistent: Boolean = srcCookie.persistent
    private val hostOnly: Boolean = srcCookie.hostOnly

    fun toCookie() = Cookie.Builder()
        .name(name)
        .value(value)
        .expiresAt(expiresAt)
        .path(path)
        .apply {
            if (hostOnly) hostOnlyDomain(domain) else domain(domain)
            if (secure) secure()
            if (httpOnly) httpOnly()
        }
        .build()
}

class PersistentCookieStore(context: Context) {
    companion object {
        private const val TAG = "PersistentCookieStore"
        private const val COOKIE_PREFS = "cookie_prefs"
        private const val PREFIX_HOST_NAME = "host_"
        private const val PREFIX_COOKIE_NAME = "cookie_"
    }

    private val cookiesStore: HashMap<String, ConcurrentHashMap<String, Cookie>> = hashMapOf()
    private val prefs = context.applicationContext.getSharedPreferences(COOKIE_PREFS, 0)

    init {
        val local = prefs.all
        local.forEach {
            if (it.key !is String || !it.key.contains(PREFIX_HOST_NAME))
                return@forEach
            if (it.value !is String || (it.value as String).isEmpty())
                return@forEach
            if (!cookiesStore.containsKey(it.key))
                cookiesStore[it.key] = ConcurrentHashMap()
            (it.value as String).split(",").forEach decode@{ name ->
                val encodedCookie =
                    prefs.getString("$PREFIX_COOKIE_NAME$name", null) ?: return@decode
                val cookie = decodeCookie(encodedCookie)
                cookiesStore[it.key]?.put(name, cookie)
            }
        }
        local.clear()
        clearExpired()
    }

    operator fun set(httpUrl: HttpUrl, value: List<Cookie>) {
        value.filterNot { it.expiresAt < System.currentTimeMillis() }
            .forEach { addCookie(httpUrl, it) }
    }

    operator fun get(httpUrl: HttpUrl) = getCookies("$PREFIX_HOST_NAME${httpUrl.host}")

    fun remove(httpUrl: HttpUrl, cookie: Cookie) {
        val cookieName = "${cookie.domain}#${cookie.name}"
        val httpHost = "$PREFIX_HOST_NAME${httpUrl.host}"
        cookiesStore[httpHost]?.remove(cookieName)?.let {
            prefs.edit {
                remove("$PREFIX_COOKIE_NAME${cookieName}")
                putString(httpHost, cookiesStore.keys.joinToString(","))
            }
        }
    }

    fun clear() {
        prefs.edit { clear() }
        cookiesStore.clear()
    }

    val cookies: List<Cookie>
        get() = arrayListOf<Cookie>().apply {
            cookiesStore.forEach {
                addAll(getCookies(it.key))
            }
        }

    private fun addCookie(httpUrl: HttpUrl, cookie: Cookie) {
        if (!cookie.persistent) return
        val cookieName = "${cookie.domain}#${cookie.name}"
        val httpHost = "$PREFIX_HOST_NAME${httpUrl.host}"
        if (!cookiesStore.containsKey(httpHost))
            cookiesStore[httpHost] = ConcurrentHashMap()
        cookiesStore[httpHost]?.put(cookieName, cookie)
        prefs.edit {
            putString(httpHost, cookiesStore[httpHost]?.keys?.joinToString(","))
            putString("$PREFIX_COOKIE_NAME$cookieName", encodeCookie(cookie))
        }
    }

    private fun getCookies(httpHost: String): List<Cookie> {
        return cookiesStore[httpHost]?.values
            ?.filterNot { it.expiresAt < System.currentTimeMillis() }
            ?: emptyList()
    }

    private fun clearExpired() {
        prefs.edit {
            cookiesStore.forEach {
                val hasExpired = it.value.any { entry ->
                    val isExpired = entry.value.expiresAt < System.currentTimeMillis()
                    if (isExpired) {
                        it.value.remove(entry.key)
                        remove("$PREFIX_COOKIE_NAME${entry.key}")
                    }
                    return@any isExpired
                }
                if (hasExpired)
                    putString(it.key, cookiesStore.keys.joinToString(","))
            }
        }
    }

    @Throws(IOException::class)
    private fun encodeCookie(cookie: Cookie): String {
        return ByteArrayOutputStream().apply {
            ObjectOutputStream(this).use {
                it.writeObject(PersistCookie(cookie))
            }
        }.toByteArray().hex
    }

    @Throws(IOException::class)
    private fun decodeCookie(encodedCookie: String): Cookie {
        return encodedCookie.decodeHex().toByteArray().inputStream()
            .run { ObjectInputStream(this) }
            .use { it.readObject() as PersistCookie }
            .toCookie()
    }
}

class TokenCookieJar(private val cookieStore: PersistentCookieStore) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url]
    }
}
