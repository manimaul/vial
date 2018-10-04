package com.willkamp.vial.implementation;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

class ChannelConfig {
  private final EventLoopGroup eventLoopGroup;
  private final Class<? extends ServerChannel> channelClass;

  final EventLoopGroup getEventLoopGroup() {
    return this.eventLoopGroup;
  }

  final Class<? extends ServerChannel> getChannelClass() {
    return this.channelClass;
  }

  ChannelConfig() {
    if (Epoll.isAvailable()) {
      this.eventLoopGroup = new EpollEventLoopGroup(10);
      this.channelClass = EpollServerSocketChannel.class;
    } else if (KQueue.isAvailable()) {
      this.eventLoopGroup = new KQueueEventLoopGroup(10);
      this.channelClass = KQueueServerSocketChannel.class;
    } else {
      this.eventLoopGroup = new NioEventLoopGroup(10);
      this.channelClass = NioServerSocketChannel.class;
    }
  }
}
