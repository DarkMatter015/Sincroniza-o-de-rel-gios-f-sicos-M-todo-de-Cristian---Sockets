import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

/**
 * Servidor UDP simples para simular o Algoritmo de Cristian.
 * Ele adiciona um offset (atraso/adiantamento) ao seu tempo
 * para que os clientes tenham algo para sincronizar.
 */
public class TimeServer {
    // Simula um relógio de servidor que está 30 segundos adiantado.
    // Mude este valor para testar (ex: -10000 para 10s atrasado)
    private static final long SERVER_TIME_OFFSET_MS = 30000;
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Servidor de Tempo UDP iniciado na porta " + PORT);
        System.out.println("Tempo 'real' do servidor: " + new Date(System.currentTimeMillis()));
        System.out.println("Tempo 'simulado' (enviado): " + new Date(getSimulatedServerTime()));
        System.out.println("Offset: " + (SERVER_TIME_OFFSET_MS / 1000) + " segundos.");
        System.out.println("Aguardando requisições...");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[1024];

            while (true) {
                // 1. Espera por um pacote de requisição
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                // 2. Obtém o tempo simulado (Ts)
                long serverTime = getSimulatedServerTime();
                byte[] sendData = String.valueOf(serverTime).getBytes();

                // 3. Obtém o endereço e porta do cliente (do pacote recebido)
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // 4. Envia a resposta (Ts) de volta ao cliente
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(sendPacket);

                System.out.println("Resposta enviada para " + clientAddress + ":" + clientPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna o tempo de sistema atual mais o offset de simulação.
     */
    private static long getSimulatedServerTime() {
        return System.currentTimeMillis() + SERVER_TIME_OFFSET_MS;
    }
}