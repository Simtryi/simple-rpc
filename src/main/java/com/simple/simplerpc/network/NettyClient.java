package com.simple.simplerpc.network;

import com.alibaba.fastjson.JSON;
import com.simple.simplerpc.common.ProviderUtil;
import com.simple.simplerpc.protocol.RpcDecoder;
import com.simple.simplerpc.protocol.RpcEncoder;
import com.simple.simplerpc.registry.Service;
import com.simple.simplerpc.registry.ServiceRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author simple
 */
@Slf4j
public class NettyClient extends SimpleChannelInboundHandler<RpcResponse>{

    private final ServiceRegistry serviceRegistry;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private Channel channel;

    private RpcResponse response;
    private final Object lock = new Object();


    public NettyClient(ServiceRegistry serviceRegistry) { this.serviceRegistry = serviceRegistry; }


    public RpcResponse sendRequest(RpcRequest request) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(NettyClient.this);
                        }
                    });

            //服务发现
            String serviceKey = ProviderUtil.generateKey(request.getClassName(), request.getServiceVersion());
            Service service = serviceRegistry.discovery(serviceKey);
            if (service == null) {
                //没有获取到服务提供方
                throw new RuntimeException("No available service provider for " + serviceKey);
            }

            //连接 rpc provider
            final ChannelFuture channelFuture = bootstrap.connect(service.getHost(), service.getPort()).sync();

            channelFuture.addListener((ChannelFutureListener) arg0 -> {
                if (channelFuture.isSuccess()) {
                    log.info("Connect rpc provider success");
                } else {
                    log.info("Connect rpc provider fail");
                    channelFuture.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });

            //向 channel 发送 RpcRequest
            this.channel = channelFuture.channel();
            this.channel.writeAndFlush(request).sync();
            log.info("Netty client send request: {}", JSON.toJSON(request));

            //等待结果返回
            synchronized (this.lock) {
                this.lock.wait();
            }

            return this.response;
        } finally {
            if (this.channel != null) {
                this.channel.close();
            }

            if (this.eventLoopGroup != null) {
                this.eventLoopGroup.shutdownGracefully();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        log.info("Netty client receive response: {}", JSON.toJSON(response));

        this.response = response;

        synchronized (lock) {
            lock.notifyAll();
        }
    }

}
