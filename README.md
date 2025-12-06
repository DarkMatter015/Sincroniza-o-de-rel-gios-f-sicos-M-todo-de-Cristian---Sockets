# SincClock - Sincronização de Relógio com gRPC

Este projeto implementa o Algoritmo de Cristian para sincronização de relógios utilizando Java e gRPC.

## Pré-requisitos

Para compilar e rodar este projeto, você precisará ter instalado:

*   **Java JDK 11** ou superior (recomendado JDK 21).
*   **Gradle** (opcional, o projeto inclui o wrapper, mas ter instalado ajuda).

## Compilando o Projeto

O projeto utiliza o Gradle para gerenciar dependências e compilar o código, incluindo a geração das classes gRPC a partir do arquivo `.proto`.

Para compilar o projeto, execute o seguinte comando na raiz do projeto:

```bash
gradle build
```

Este comando irá baixar as dependências, gerar o código Java a partir do arquivo `src/main/proto/sincclock.proto` e compilar as classes do servidor e do cliente.

## Rodando o Servidor

Para iniciar o servidor de tempo, execute:

```bash
gradle runServer
```

O servidor iniciará na porta `5000` e exibirá logs informando o tempo real e o tempo simulado (com offset).

## Rodando o Cliente

Para iniciar o cliente e sincronizar o tempo com o servidor, abra um novo terminal e execute:

```bash
gradle runClient
```

O cliente tentará se conectar ao servidor em `localhost:5000` e exibirá o RTT (Round-Trip Time), o novo tempo sincronizado e o offset calculado.

**Nota:** Por padrão, o cliente se conecta a `localhost`. Se você estiver rodando o servidor em outra máquina, precisará alterar o endereço no arquivo `src/main/java/sincclock/TimeClient.java` e recompilar.

## Estrutura do Projeto

*   `src/main/proto/sincclock.proto`: Definição do serviço gRPC.
*   `src/main/java/sincclock/TimeServer.java`: Implementação do servidor gRPC.
*   `src/main/java/sincclock/TimeClient.java`: Implementação do cliente gRPC.
*   `build.gradle`: Configuração do build e dependências.
