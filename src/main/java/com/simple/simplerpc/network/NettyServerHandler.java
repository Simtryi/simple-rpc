package com.simple.simplerpc.network;

import com.alibaba.fastjson.JSON;
import com.simple.simplerpc.common.ProviderUtil;
import com.simple.simplerpc.provider.Provider;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;


/**
 * @author simple
 *
 * 自定义ChannelHandler:
 * 1.接收客户端请求
 * 2.根据 RpcRequest 调用相应的 Bean
 * 3.将结果返回给客户端
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        Provider.submit(() -> {
            log.info("Netty service receive request: {}", JSON.toJSON(request));

            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());

            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Throwable throwable) {
                response.setError(throwable.getMessage());
                log.error("Netty service handle request error: {}", throwable.getMessage());
            }

            ChannelFuture future = channelHandlerContext.writeAndFlush(response);
            future.addListener((ChannelFutureListener) channelFuture -> {
                if(channelFuture.isSuccess()) {
                    log.info("Netty service send response: {}", JSON.toJSON(response));
                } else {
                    log.info("Netty service send response fail!");
                    channelFuture.cause().printStackTrace();
                }
            });

        });
    }

    /**
     * 处理接收的 RpcRequest
     */
    private Object handle(RpcRequest request) throws Throwable {
        //从缓存中获取 Bean
        String serviceKey = ProviderUtil.generateKey(request.getClassName(), request.getServiceVersion());
        Object bean = Provider.PROVIDER_MAP.get(serviceKey);

        if (bean == null) {
            throw new IllegalArgumentException(String.format("ClassName=%s, MethodName=%s not exist", request.getClassName(), request.getMethodName()));
        }

        //通过反射调用 Bean
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();

        FastClass proxyClass = FastClass.create(bean.getClass());
        FastMethod method = proxyClass.getMethod(methodName, parameterTypes);
        return method.invoke(bean, parameters);
    }

}
