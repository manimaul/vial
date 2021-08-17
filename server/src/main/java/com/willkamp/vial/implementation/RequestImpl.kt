package com.willkamp.vial.implementation

import com.willkamp.vial.api.Request
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.CompositeByteBuf
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http2.Http2Headers
import io.netty.util.CharsetUtil
import java.io.IOException
import java.util.*
import kotlin.collections.Map.Entry
import java.util.stream.Collectors
import java.util.stream.StreamSupport

internal class RequestImpl private constructor(
        alloc: ByteBufAllocator,
        val path: CharSequence,
        private val headers: Iterable<Entry<CharSequence, CharSequence>>? = null) : Request {

    private val body: CompositeByteBuf?
    private val objectMapper = Assembly.objectMapper
    private var pathParamGroups: Map<String, String>? = null
    private val queryParams by lazy {
        QueryStringDecoder("$path").parameters()
    }
    private var pathParamGroupSupplier: () -> Map<String, String> = { emptyMap() }
    private val log = logger()


    override val bodyText: String?
        get() = body?.toString(CharsetUtil.UTF_8)

    init {
        this.body = alloc.compositeBuffer()
    }

    fun appendData(dataFrame: ByteBuf): Int {
        // CompositeByteBuf releases data
        body?.addComponent(true, dataFrame.retain())
        return body?.readableBytes() ?: 0
    }

    override fun <T> bodyJson(clazz: Class<T>): T? {
        if (body != null) {
            try {
                return objectMapper.readValue(body.array(), clazz)
            } catch (e: IOException) {
                log.error("error deserializing json", e)
            }

        }
        return null
    }

    override fun headers(): Iterable<Entry<CharSequence, CharSequence>> {
        return headers ?: emptyList()
    }

    override fun pathParam(key: String): String? {
        if (pathParamGroups == null) {
            pathParamGroups = pathParamGroupSupplier()
        }
        return pathParamGroups?.let {
            it[key]
        }
    }

    override fun queryParams(key: String): List<String> {
        return queryParams.getOrDefault(key, emptyList())
    }

    override fun queryKeys(): Set<String> {
        return queryParams.keys
    }

    override fun queryParam(key: String): String? {
        return queryParams(key).firstOrNull()
    }

    fun setPathParamGroupSupplier(supplier: () -> Map<String, String>) {
        pathParamGroups = null
        pathParamGroupSupplier = supplier
    }

    companion object {

        fun fromH2Headers(alloc: ByteBufAllocator, headers: Http2Headers): RequestImpl {
            return RequestImpl(alloc, headers.path(), headers)
        }

        fun fromStringHeaders(
                alloc: ByteBufAllocator,
                path: CharSequence,
                headers: Iterable<Entry<String, String>>,
                body: ByteBuf): RequestImpl {
            val list = StreamSupport.stream(headers.spliterator(), false)
                    .map {
                        AbstractMap.SimpleEntry<CharSequence, CharSequence>(it.key, it.value)
                    }
                    .collect(Collectors.toList<Entry<CharSequence, CharSequence>>())
            val impl = RequestImpl(alloc, path, list)
            impl.appendData(body)
            return impl
        }
    }
}
