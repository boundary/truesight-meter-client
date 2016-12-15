package com.bmc.truesight.saas.meter.client.rpc;

import com.bmc.truesight.saas.meter.client.TruesightMeterClient;
import com.bmc.truesight.saas.meter.client.command.AddEvents;
import com.bmc.truesight.saas.meter.client.command.AddMeasures;
import com.bmc.truesight.saas.meter.client.command.Command;
import com.bmc.truesight.saas.meter.client.command.Debug;
import com.bmc.truesight.saas.meter.client.command.Discovery;
import com.bmc.truesight.saas.meter.client.command.GetProcessInfo;
import com.bmc.truesight.saas.meter.client.command.GetProcessTopK;
import com.bmc.truesight.saas.meter.client.command.GetServiceListeners;
import com.bmc.truesight.saas.meter.client.command.GetSystemInfo;
import com.bmc.truesight.saas.meter.client.command.QueryMetric;
import com.bmc.truesight.saas.meter.client.response.DebugResponse;
import com.bmc.truesight.saas.meter.client.response.DiscoveryResponse;
import com.bmc.truesight.saas.meter.client.response.GetProcessInfoResponse;
import com.bmc.truesight.saas.meter.client.response.GetProcessTopKResponse;
import com.bmc.truesight.saas.meter.client.response.GetServiceListenersResponse;
import com.bmc.truesight.saas.meter.client.response.GetSystemInfoResponse;
import com.bmc.truesight.saas.meter.client.response.QueryMetricResponse;
import com.bmc.truesight.saas.meter.client.response.Response;
import com.bmc.truesight.saas.meter.client.response.VoidResponse;
import com.bmc.truesight.saas.meter.client.model.Event;
import com.bmc.truesight.saas.meter.client.model.Measure;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

/**
 * Exposes the {@link TruesightMeterClient} api to clients
 * and manages creation/reconnection of underlying {@link TruesightMeterNettyRpc}
 */
public class TruesightRpcClient implements TruesightMeterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TruesightRpcClient.class);

    private final Supplier<TruesightMeterNettyRpc> rpcFactory;
    private final AtomicBoolean connectionPending = new AtomicBoolean(false);
    private final AtomicBoolean shutDown = new AtomicBoolean(false);
    private final ExecutorService executor;
    private volatile TruesightMeterNettyRpc rpc;

    public TruesightRpcClient(TruesightMeterRpcClientConfig config) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        this.rpcFactory = () -> new TruesightMeterNettyRpc(config, workerGroup);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public CompletableFuture<VoidResponse> addMeasures(List<Measure> measures) {
        return send(AddMeasures.of(measures));
    }

    @Override
    public CompletableFuture<VoidResponse> addMeasure(Measure measure) {
        return send(AddMeasures.of(measure));
    }

    @Override
    public CompletableFuture<VoidResponse> addEvents(List<Event> events) {
        return send(AddEvents.of(events));
    }

    @Override
    public CompletableFuture<VoidResponse> addEvent(Event event) {
        return send(AddEvents.of(event));
    }

    @Override
    public CompletableFuture<DiscoveryResponse> discovery() {
        return send(Discovery.of());
    }

    @Override
    public CompletableFuture<GetSystemInfoResponse> systemInformation() {
        return send(GetSystemInfo.of());
    }

    @Override
    public CompletableFuture<DebugResponse> debug(String section, int level) {
        return send(Debug.of(section, level));
    }

    @Override
    public CompletableFuture<GetProcessInfoResponse> getProcessInfo(GetProcessInfo.TypedExpression expression, GetProcessInfo.TypedExpression ... optional) {
        return send(GetProcessInfo.of(expression, optional));
    }

    @Override
    public CompletableFuture<GetProcessTopKResponse> getProcessTopK(GetProcessTopK.TypedNumber number, GetProcessTopK.TypedNumber ... optional) {
        return send(GetProcessTopK.of(number, optional));
    }

    @Override
    public CompletableFuture<QueryMetricResponse> queryMetric(String metric, boolean exact) {
        return send(QueryMetric.of(metric, exact));
    }

    @Override
    public CompletableFuture<GetServiceListenersResponse> getServiceListeners() {
        return send(GetServiceListeners.of());
    }

    protected  <T extends Response> CompletableFuture<T> send(Command<T> command) {

        try {
            if (!connected()) {
                throw new DisconnectedException("Not connected", command);
            }
            return rpc.sendCommand(command);
        } catch (Exception e) {
            tryReconnect();
            CompletableFuture<T> exFuture = new CompletableFuture<>();
            exFuture.completeExceptionally(e);
            return exFuture;
        }
    }

    private boolean connected() {
        return !connectionPending.get()
                && rpc != null
                && rpc.isConnected();
    }

    /**
     * public connect which should be called by clients
     * following instantiation
     * @return true if connected, false otherwise
     * @throws Exception
     */
    public boolean connect() throws Exception {
        if (connectionPending.compareAndSet(false, true)) {
            try {
                return connectSync();
            } finally {
                connectionPending.set(false);
            }
        }
        return false;
    }

    private void tryReconnect() {
        if (connectionPending.compareAndSet(false, true)) {
            executor.submit(() -> {
                try {
                    this.connectSync();
                } catch (Exception e) {
                    LOGGER.error("Exception reconnecting, make sure your TrueSight meter is running");
                } finally {
                    connectionPending.set(false);
                }

            });
        }
    }

    private boolean connectSync() throws Exception {
        checkState(connectionPending.get(), "Connection attempted but not marked as pending");
        rpc = rpcFactory.get();
        try {
            rpc.connect();
            return rpc.awaitConnected(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void close() throws InterruptedException {
        if (shutDown.compareAndSet(false, true)) {
            rpc.close();
        }
    }
}
