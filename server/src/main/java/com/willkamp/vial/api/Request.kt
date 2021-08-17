package com.willkamp.vial.api

import java.util.*
import kotlin.collections.Map.Entry

interface Request {
    val bodyText: String?
    fun headers(): Iterable<Entry<CharSequence, CharSequence>>
    fun pathParam(key: String): String?
    fun queryParams(key: String): List<String>
    fun queryKeys(): Set<String>
    fun queryParam(key: String): String?
    fun queryParamOption(key: String): Optional<String> {
        return Optional.ofNullable(queryParam(key))
    }
    fun pathParamOption(key: String): Optional<String> {
        return Optional.ofNullable(pathParam(key))
    }
    fun <T> bodyJson(clazz: Class<T>): T?
}
