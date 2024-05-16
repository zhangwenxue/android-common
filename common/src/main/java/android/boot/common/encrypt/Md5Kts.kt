package com.wehealth.core.common.encrypt

import java.security.MessageDigest
import java.util.Locale

val String.md5: String
    get() {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(this.toByteArray())
        val digest = md5.digest()
        val ret = digest.joinToString(separator = "") {
            "%02x".format(it)
        }
        return ret.uppercase(Locale.US)
    }