package sincclock;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação: Cliente Cristian (Moderno com java.time) usando gRPC.
 */
public class TimeClient {
    private static final Logger logger = Logger.getLogger(TimeClient.class.getName());

    private final ManagedChannel channel;
    private final TimeServiceGrpc.TimeServiceBlockingStub blockingStub;

    public TimeClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    TimeClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = TimeServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void syncTime() {
        // T0 (Wall-Clock): carimbo de data/hora de envio
        Instant t0_wall = Instant.now();
        // T0 (Monotonic): início da medição de RTT
        long t0_nano = System.nanoTime();

        TimeRequest request = TimeRequest.newBuilder().build();
        TimeResponse response;
        try {
            response = blockingStub.getTime(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }

        // T1 (Monotonic): fim da medição de RTT
        long t1_nano = System.nanoTime();
        // T1 (Wall-Clock): carimbo de data/hora de recebimento
        Instant t1_wall = Instant.now();

        // Processa a hora do servidor (Ts)
        long serverTimeMillis = response.getServerTime();
        Instant serverTime = Instant.ofEpochMilli(serverTimeMillis);

        // 1. Calcula o RTT usando nanoTime e encapsula em um Duration
        Duration rtt = Duration.ofNanos(t1_nano - t0_nano);

        // 2. Aplica a fórmula de Cristian
        Duration estimatedDelay = rtt.dividedBy(2);
        Instant newClientTime = serverTime.plus(estimatedDelay);

        // 3. Calcula o offset
        Duration offset = Duration.between(t1_wall, newClientTime);

        System.out.println("--- [Cliente gRPC (java.time)] ---");
        System.out.println("Tempo local (T0): " + t0_wall.toString());
        System.out.println("Tempo do Servidor (Ts): " + serverTime.toString());
        System.out.println("Tempo local (T1): " + t1_wall.toString());
        System.out.println("RTT (calculado c/ nano): " + rtt.toMillis() + " ms");
        System.out.println("Novo tempo sincronizado: " + newClientTime.toString());
        System.out.println("Offset (ajuste): " + offset.toMillis() + " ms");
        System.out.println("-------------------------------------");
    }

    public static void main(String[] args) throws InterruptedException {
        // Use localhost for local testing, or the specific IP if needed.
        // The original code had "172.30.20.238", changing to localhost for portability in this environment.
        String target = "localhost";
        int port = 5000;

        TimeClient client = new TimeClient(target, port);
        try {
            while (true) {
                client.syncTime();
                Thread.sleep(5000);
            }
        } finally {
            client.shutdown();
        }
    }
}
