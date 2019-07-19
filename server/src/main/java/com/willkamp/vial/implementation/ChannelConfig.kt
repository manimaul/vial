package com.willkamp.vial.implementation

import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

private const val BOSS_THREADS = 5
private const val WORKER_THREADS = 10

internal class ChannelConfig {
    val bossEventLoopGroup: EventLoopGroup by lazy {
        when {
            Epoll.isAvailable() -> EpollEventLoopGroup(BOSS_THREADS)
            KQueue.isAvailable() -> KQueueEventLoopGroup(BOSS_THREADS)
            else -> NioEventLoopGroup(BOSS_THREADS)
        }
    }
    val eventLoopGroup: EventLoopGroup by lazy {
        when {
            Epoll.isAvailable() -> EpollEventLoopGroup(WORKER_THREADS)
            KQueue.isAvailable() -> KQueueEventLoopGroup(WORKER_THREADS)
            else -> NioEventLoopGroup(WORKER_THREADS)
        }
    }
    val channelClass: Class<out ServerChannel> by lazy {
        when {
            Epoll.isAvailable() -> EpollServerSocketChannel::class.java
            KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
            else -> NioServerSocketChannel::class.java
        } as Class<out ServerChannel>
    }
}
