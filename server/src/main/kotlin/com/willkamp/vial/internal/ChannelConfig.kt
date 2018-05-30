package com.willkamp.vial.internal

import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

private const val NUM_THREADS = 10

internal class ChannelConfig internal constructor() {
    val eventLoopGroup: EventLoopGroup
    val channelClass: Class<out ServerChannel>

    init {
        if (Epoll.isAvailable()) {
            eventLoopGroup = EpollEventLoopGroup(NUM_THREADS)
            channelClass = EpollServerSocketChannel::class.java
        } else if(KQueue.isAvailable()) {
            eventLoopGroup = KQueueEventLoopGroup(NUM_THREADS)
            channelClass = KQueueServerSocketChannel::class.java
        } else {
            eventLoopGroup = NioEventLoopGroup(NUM_THREADS)
            channelClass = NioServerSocketChannel::class.java
        }
    }
}