package com.willkamp.vial.implementation;

import com.willkamp.vial.api.RequestHandler;
import com.willkamp.vial.api.VialServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpMethod;
import java.io.Closeable;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VialServerImpl implements VialServer, Closeable {
  private final VialConfig vialConfig;
  private final ChannelConfig channelConfig;
  private final VialChannelInitializer vialChannelInitializer;
  private final RouteRegistry routeRegistry;

  VialServerImpl(
      VialConfig config,
      ChannelConfig channelConfig,
      VialChannelInitializer vialChannelInitializer,
      RouteRegistry routeRegistry) {
    this.vialChannelInitializer = vialChannelInitializer;
    this.vialConfig = config;
    this.channelConfig = channelConfig;
    this.routeRegistry = routeRegistry;
  }

  @Override
  public VialServerImpl request(HttpMethod method, String route, RequestHandler handler) {
    routeRegistry.registerRoute(method, route, handler);
    return this;
  }

  @Override
  public VialServer staticContent(File rootDirectory) {
    log.error("static content noop - not implemented");
    return this;
  }

  @Override
  public void listenAndServeBlocking() {
    serve(null);
  }

  @Override
  public CompletableFuture<Closeable> listenAndServe() {
    CompletableFuture<Closeable> future = new CompletableFuture<>();
    new Thread(() -> serve(future)).run();
    return future;
  }

  private void serve(@Nullable CompletableFuture<Closeable> future) {
    ServerBootstrap bootstrap = new ServerBootstrap();
    try {
      InetAddress address = InetAddress.getByName(vialConfig.getAddress());
      InetSocketAddress socketAddress = new InetSocketAddress(address, vialConfig.getPort());
      bootstrap
          .group(channelConfig.getEventLoopGroup())
          .channel(channelConfig.getChannelClass())
          .localAddress(socketAddress)
          .childHandler(vialChannelInitializer);
      ChannelFuture channelFuture = bootstrap.bind();
      channelFuture.addListener(
          f -> {
            if (future != null) {
              future.complete(this);
            }
          });
      channelFuture.sync();
      channelFuture.channel().closeFuture().sync();
    } catch (UnknownHostException | InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      close();
    }
  }

  @Override
  public void close() {
    try {
      channelConfig.getEventLoopGroup().shutdownGracefully().sync();
    } catch (InterruptedException e) {
      log.error("error", e);
      throw new RuntimeException(e);
    }
  }
}
