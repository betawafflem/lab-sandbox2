/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.bootstrap.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alipay.sofa.rpc.bootstrap.dubbo.demo.DemoService;
import com.alipay.sofa.rpc.bootstrap.dubbo.demo.DemoServiceImpl;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.MethodConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.context.RpcRunningState;
import com.alipay.sofa.rpc.context.RpcRuntimeContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author <a href=mailto:leizhiyuan@gmail.com>leizhiyuan</a>
 */
public class DubooServerTest {

    ProviderConfig<DemoService> providerConfig;

    ConsumerConfig<DemoService> consumerConfig;

    //dubbo close wait time
    @AfterClass
    public static void before() {
        ConfigUtils.getProperties().put(Constants.SHUTDOWN_WAIT_KEY, "1");
    }

    @Test
    //????????????,??????
    public void testSync() {
        try {
            // ??????1????????? ??????
            ServerConfig serverConfig = new ServerConfig()
                .setStopTimeout(60000)
                .setPort(20880)
                .setProtocol("dubbo")
                .setQueues(100).setCoreThreads(1).setMaxThreads(2);

            // ??????????????????????????????????????????1???
            ApplicationConfig serverApplacation = new ApplicationConfig();
            serverApplacation.setAppName("server");
            providerConfig = new ProviderConfig<DemoService>()
                .setInterfaceId(DemoService.class.getName())
                .setRef(new DemoServiceImpl())
                .setBootstrap("dubbo")
                .setServer(serverConfig)
                // .setParameter(RpcConstants.CONFIG_HIDDEN_KEY_WARNING, "false")
                .setRegister(false).setApplication(serverApplacation);
            providerConfig.export();

            ApplicationConfig clientApplication = new ApplicationConfig();
            clientApplication.setAppName("client");
            consumerConfig = new ConsumerConfig<DemoService>()
                .setInterfaceId(DemoService.class.getName())
                .setDirectUrl("dubbo://127.0.0.1:20880")
                .setBootstrap("dubbo")
                .setTimeout(30000)
                .setRegister(false).setProtocol("dubbo").setApplication(clientApplication);
            final DemoService demoService = consumerConfig.refer();

            String result = demoService.sayHello("xxx");
            Assert.assertTrue(result.equalsIgnoreCase("hello xxx"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    //????????????
    public void testOneWay() {
        // ??????1????????? ??????
        ServerConfig serverConfig = new ServerConfig()
            .setStopTimeout(60000)
            .setPort(20880)
            .setProtocol("dubbo")
            .setQueues(100).setCoreThreads(1).setMaxThreads(2);

        // ??????????????????????????????????????????1???
        ApplicationConfig serverApplacation = new ApplicationConfig();
        serverApplacation.setAppName("server");
        providerConfig = new ProviderConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setRef(new DemoServiceImpl())
            .setServer(serverConfig)
            .setBootstrap("dubbo")
            // .setParameter(RpcConstants.CONFIG_HIDDEN_KEY_WARNING, "false")
            .setRegister(false).setApplication(serverApplacation);
        providerConfig.export();

        ApplicationConfig clientApplication = new ApplicationConfig();
        clientApplication.setAppName("client");

        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>();

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setInvokeType(RpcConstants.INVOKER_TYPE_ONEWAY);
        methodConfig.setName("sayHello");

        methodConfigs.add(methodConfig);
        consumerConfig = new ConsumerConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setDirectUrl("dubbo://127.0.0.1:20880")
            .setTimeout(30000)
            .setRegister(false)
            .setProtocol("dubbo")
            .setBootstrap("dubbo")
            .setApplication(clientApplication)
            .setInvokeType(RpcConstants.INVOKER_TYPE_ONEWAY)
            .setMethods(methodConfigs);
        final DemoService demoService = consumerConfig.refer();
        String tmp = demoService.sayHello("xxx");
        Assert.assertEquals(null, tmp);

    }

    @Test
    //future??????,???future?????????.
    public void testFuture() {
        // ??????1????????? ??????
        ServerConfig serverConfig = new ServerConfig()
            .setStopTimeout(60000)
            .setPort(20880)
            .setProtocol("dubbo")
            .setQueues(100).setCoreThreads(1).setMaxThreads(2);

        // ??????????????????????????????????????????1???
        ApplicationConfig serverApplacation = new ApplicationConfig();
        serverApplacation.setAppName("server");
        providerConfig = new ProviderConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setRef(new DemoServiceImpl())
            .setServer(serverConfig)
            .setBootstrap("dubbo")
            // .setParameter(RpcConstants.CONFIG_HIDDEN_KEY_WARNING, "false")
            .setRegister(false).setApplication(serverApplacation);
        providerConfig.export();

        ApplicationConfig clientApplication = new ApplicationConfig();
        clientApplication.setAppName("client");
        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>();

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE);
        methodConfig.setName("sayHello");
        consumerConfig = new ConsumerConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setDirectUrl("dubbo://127.0.0.1:20880")
            .setTimeout(30000)
            .setRegister(false).setProtocol("dubbo")
            .setBootstrap("dubbo")
            .setApplication(clientApplication)
            .setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE)
            .setMethods(methodConfigs);
        final DemoService demoService = consumerConfig.refer();

        String result = demoService.sayHello("xxx");
        Assert.assertEquals(null, result);

        Future<Object> future = RpcContext.getContext().getFuture();

        String futureResult = null;
        try {
            futureResult = (String) future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Hello xxx", futureResult);

    }

    @Test
    //??????????????????,??????
    public void testGenericSync() {
        // ??????1????????? ??????
        ServerConfig serverConfig = new ServerConfig()
            .setStopTimeout(60000)
            .setPort(20880)
            .setProtocol("dubbo")
            .setQueues(100).setCoreThreads(1).setMaxThreads(2);

        // ??????????????????????????????????????????1???
        ApplicationConfig serverApplacation = new ApplicationConfig();
        serverApplacation.setAppName("server");
        providerConfig = new ProviderConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setRef(new DemoServiceImpl())
            .setBootstrap("dubbo")
            .setServer(serverConfig)
            // .setParameter(RpcConstants.CONFIG_HIDDEN_KEY_WARNING, "false")
            .setRegister(false).setApplication(serverApplacation);
        providerConfig.export();

        ApplicationConfig clientApplication = new ApplicationConfig();
        clientApplication.setAppName("client");
        consumerConfig = new ConsumerConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setDirectUrl("dubbo://127.0.0.1:20880")
            .setBootstrap("dubbo")
            .setTimeout(30000)
            .setRegister(false)
            .setProtocol("dubbo")
            .setApplication(clientApplication)
            .setGeneric(true);
        final GenericService demoService = (GenericService) consumerConfig.refer();

        String result = (String) demoService.$invoke("sayHello", new String[] { "java.lang.String" },
            new Object[] { "xxx" });
        Assert.assertEquals(result, "Hello xxx");

    }

    @Test
    //????????????,??????,dubbo ??????????????????version
    public void testWithParameterWithVersion() {
        try {
            // ??????1????????? ??????
            ServerConfig serverConfig = new ServerConfig()
                .setStopTimeout(60000)
                .setPort(20880)
                .setProtocol("dubbo")
                .setQueues(100).setCoreThreads(1).setMaxThreads(2);

            // ??????????????????????????????????????????1???
            ApplicationConfig serverApplacation = new ApplicationConfig();
            serverApplacation.setAppName("server");
            providerConfig = new ProviderConfig<DemoService>()
                .setInterfaceId(DemoService.class.getName())
                .setRef(new DemoServiceImpl())
                .setBootstrap("dubbo")
                .setServer(serverConfig)
                .setParameter("version", "1.0.1")
                .setRegister(false).setApplication(serverApplacation);
            providerConfig.export();

            ApplicationConfig clientApplication = new ApplicationConfig();
            clientApplication.setAppName("client");
            consumerConfig = new ConsumerConfig<DemoService>()
                .setInterfaceId(DemoService.class.getName())
                .setDirectUrl("dubbo://127.0.0.1:20880")
                .setBootstrap("dubbo")
                .setTimeout(30000)
                .setParameter("version", "1.0.1")
                .setRegister(false).setProtocol("dubbo").setApplication(clientApplication);
            final DemoService demoService = consumerConfig.refer();

            String result = demoService.sayHello("xxx");
            Assert.assertTrue(result.equalsIgnoreCase("hello xxx"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test(expected = com.alibaba.dubbo.rpc.RpcException.class)
    //????????????,??????,dubbo ??????????????????dubbo????????????version
    public void testConsumerWithNoDubboServiceVersion() {
        // ??????1????????? ??????
        ServerConfig serverConfig = new ServerConfig()
            .setStopTimeout(60000)
            .setPort(20880)
            .setProtocol("dubbo")
            .setQueues(100).setCoreThreads(1).setMaxThreads(2);

        // ??????????????????????????????????????????1???
        ApplicationConfig serverApplacation = new ApplicationConfig();
        serverApplacation.setAppName("server");
        providerConfig = new ProviderConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setRef(new DemoServiceImpl())
            .setBootstrap("dubbo")
            .setServer(serverConfig)
            .setParameter("version", "1.0.1")
            .setRegister(false).setApplication(serverApplacation);
        providerConfig.export();

        ApplicationConfig clientApplication = new ApplicationConfig();
        clientApplication.setAppName("client");
        consumerConfig = new ConsumerConfig<DemoService>()
            .setInterfaceId(DemoService.class.getName())
            .setDirectUrl("dubbo://127.0.0.1:20880")
            .setBootstrap("dubbo")
            .setTimeout(30000)
            .setRegister(false).setProtocol("dubbo").setApplication(clientApplication);
        final DemoService demoService = consumerConfig.refer();

        String result = demoService.sayHello("xxx");
        Assert.assertTrue(result.equalsIgnoreCase("hello xxx"));

    }

    @BeforeClass
    public static void adBeforeClass() {
        RpcRunningState.setUnitTestMode(true);
    }

    @After
    public void afterMethod() {
        DubboSingleton.destroyAll();
        RpcRuntimeContext.destroy();
        RpcInternalContext.removeAllContext();
        RpcInvokeContext.removeContext();
    }
}