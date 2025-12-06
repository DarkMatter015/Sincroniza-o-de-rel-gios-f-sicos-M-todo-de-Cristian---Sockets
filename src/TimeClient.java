import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;

/**
 * Implementação 2: Cliente Cristian (Moderno com java.time).
 * Usa as "classes prontas" (API java.time) para os cálculos.
 * - Instant: Representa um carimbo de data/hora UTC (substituto de 'long millis')
 * - Duration: Representa uma diferença de tempo (substituto de 'long rtt')
 * - Clock: Permite abstrair a fonte do tempo.
 *
 * Note: Ainda usamos nanoTime() para o RTT, mas o encapsulamos em um 'Duration'.
 */
public class TimeClient {

    public static void main(String[] args) {
        String serverAddress = "172.30.20.238";
        int port = 5000;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000); // 3 segundos
            InetAddress address = InetAddress.getByName(serverAddress);

            while (true) {
                try {
                    // T0 (Wall-Clock): carimbo de data/hora de envio
                    Instant t0_wall = Instant.now();
                    // T0 (Monotonic): início da medição de RTT
                    long t0_nano = System.nanoTime();

                    byte[] sendData = new byte[1024]; // Requisição vazia
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                    socket.send(sendPacket);

                    // Recebe a resposta do servidor
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    // T1 (Monotonic): fim da medição de RTT
                    long t1_nano = System.nanoTime();
                    // T1 (Wall-Clock): carimbo de data/hora de recebimento
                    Instant t1_wall = Instant.now();

                    // Processa a hora do servidor (Ts)
                    String serverTimeStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Instant serverTime = Instant.ofEpochMilli(Long.parseLong(serverTimeStr));

                    // 1. Calcula o RTT usando nanoTime e encapsula em um Duration
                    // Esta é a "classe pronta" para representar a diferença
                    Duration rtt = Duration.ofNanos(t1_nano - t0_nano);

                    // 2. Aplica a fórmula de Cristian (agora de forma semântica)
                    Duration estimatedDelay = rtt.dividedBy(2);
                    Instant newClientTime = serverTime.plus(estimatedDelay);

                    // 3. Calcula o offset (a diferença que precisamos ajustar)
                    Duration offset = Duration.between(t1_wall, newClientTime);

                    System.out.println("--- [Cliente Moderno (java.time)] ---");
                    // SIMPLIFICADO: Trocado formatter.format() por .toString()
                    System.out.println("Tempo local (T0): " + t0_wall.toString());
                    System.out.println("Tempo do Servidor (Ts): " + serverTime.toString());
                    System.out.println("Tempo local (T1): " + t1_wall.toString());
                    System.out.println("RTT (calculado c/ nano): " + rtt.toMillis() + " ms");
                    System.out.println("Novo tempo sincronizado: " + newClientTime.toString());
                    System.out.println("Offset (ajuste): " + offset.toMillis() + " ms");
                    System.out.println("-------------------------------------");

                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: Servidor '" + serverAddress + "' não respondeu.");
                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}