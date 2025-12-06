package sincclock;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Servidor gRPC para simular o Algoritmo de Cristian.
 * Ele adiciona um offset (atraso/adiantamento) ao seu tempo
 * para que os clientes tenham algo para sincronizar.
 */
public class TimeServer {
    private static final Logger logger = Logger.getLogger(TimeServer.class.getName());

    // Simula um relógio de servidor que está 30 segundos adiantado.
    // Mude este valor para testar (ex: -10000 para 10s atrasado)
    private static final long SERVER_TIME_OFFSET_MS = 30000;
    private static final int PORT = 5000;

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        server = ServerBuilder.forPort(PORT)
                .addService(new TimeServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + PORT);
        System.out.println("Tempo 'real' do servidor: " + new Date(System.currentTimeMillis()));
        System.out.println("Tempo 'simulado' (enviado): " + new Date(getSimulatedServerTime()));
        System.out.println("Offset: " + (SERVER_TIME_OFFSET_MS / 1000) + " segundos.");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    TimeServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final TimeServer server = new TimeServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Retorna o tempo de sistema atual mais o offset de simulação.
     */
    private static long getSimulatedServerTime() {
        return System.currentTimeMillis() + SERVER_TIME_OFFSET_MS;
    }

    static class TimeServiceImpl extends TimeServiceGrpc.TimeServiceImplBase {
        @Override
        public void getTime(TimeRequest req, StreamObserver<TimeResponse> responseObserver) {
            long serverTime = getSimulatedServerTime();
            TimeResponse reply = TimeResponse.newBuilder().setServerTime(serverTime).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            // System.out.println("Resposta enviada com tempo: " + serverTime);
        }
    }
}
