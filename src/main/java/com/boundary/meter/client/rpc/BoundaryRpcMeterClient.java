package com.boundary.meter.client.rpc;

import com.boundary.meter.client.BoundaryMeterClient;
import com.boundary.meter.client.command.AddMeasures;
import com.boundary.meter.client.command.Command;
import com.boundary.meter.client.command.Discovery;
import com.boundary.meter.client.command.DiscoveryResponse;
import com.boundary.meter.client.command.GetServiceListeners;
import com.boundary.meter.client.command.GetServiceListenersResponse;
import com.boundary.meter.client.command.Response;
import com.boundary.meter.client.command.VoidResponse;
import com.boundary.meter.client.model.Measure;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Exposes the {@link com.boundary.meter.client.BoundaryMeterClient} api to clients
 * and manages creation/reconnection of underlying {@link BoundaryNettyRpc}
 */
public class BoundaryRpcMeterClient implements BoundaryMeterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundaryRpcMeterClient.class);

    private final Supplier<BoundaryNettyRpc> rpcFactory;
    private final AtomicBoolean connectionPending = new AtomicBoolean(false);
    private final AtomicBoolean shutDown = new AtomicBoolean(false);
    private final ExecutorService executor;
    private volatile BoundaryNettyRpc rpc;

    public BoundaryRpcMeterClient(HostAndPort meter) throws Exception {

        this.rpcFactory = () -> new BoundaryNettyRpc(meter);
        executor = Executors.newSingleThreadExecutor();
        this.rpc = rpcFactory.get();
        rpc.connect();
        rpc.awaitConnected(1, TimeUnit.SECONDS);
    }

    @Override
    public ListenableFuture<VoidResponse> addMeasures(List<Measure> measures) {
        return send(new AddMeasures(measures));
    }

    @Override
    public ListenableFuture<DiscoveryResponse> discovery() {
        return send(Discovery.INSTANCE);
    }

    @Override
    public ListenableFuture<GetServiceListenersResponse> getServiceListeners() {
        return send(GetServiceListeners.INSTANCE);
    }

    private <T extends Response> ListenableFuture<T> send(Command<T> command) {

        try {
            if (!connected()) {
                throw new DisconnectedException("Not connected", command);
            }
            return rpc.sendCommand(command);
        } catch (Exception e) {
            tryReconnect();
            SettableFuture<T> exFuture = SettableFuture.create();
            exFuture.setException(e);
            return exFuture;
        }
    }

    private boolean connected() {
        return !connectionPending.get()
                && rpc != null
                && rpc.isConnected();

    }

    private void tryReconnect() {
        if (connectionPending.compareAndSet(false, true)) {
            executor.submit(() -> {
                    rpc = rpcFactory.get();
                    try {
                        rpc.connect();
                        rpc.awaitConnected(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LOGGER.error("Exception reconnecting", e);
                    }
                    connectionPending.set(false);

            });
        }

    }


    @Override
    public void close() throws InterruptedException {
        if (shutDown.compareAndSet(false, true)) {
            rpc.close();
        }
    }


}
