package android.net

import android.os.Parcel

class TestUri(private val raw: String) : Uri() {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = Unit

    override fun buildUpon(): Builder = throw UnsupportedOperationException()

    override fun isHierarchical(): Boolean = true

    override fun isRelative(): Boolean = false

    override fun getScheme(): String? = raw.substringBefore("://", missingDelimiterValue = "")

    override fun getEncodedSchemeSpecificPart(): String? = null

    override fun getSchemeSpecificPart(): String? = null

    override fun getEncodedAuthority(): String? = null

    override fun getAuthority(): String? = null

    override fun getEncodedUserInfo(): String? = null

    override fun getUserInfo(): String? = null

    override fun getHost(): String? = null

    override fun getPort(): Int = -1

    override fun getEncodedPath(): String? = null

    override fun getPath(): String? = null

    override fun getPathSegments(): List<String> = emptyList()

    override fun getLastPathSegment(): String? = null

    override fun getEncodedQuery(): String? = null

    override fun getQuery(): String? = null

    override fun getEncodedFragment(): String? = null

    override fun getFragment(): String? = null

    override fun equals(other: Any?): Boolean = other is TestUri && raw == other.raw

    override fun hashCode(): Int = raw.hashCode()

    override fun toString(): String = raw
}
