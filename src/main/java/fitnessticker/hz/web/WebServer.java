package fitnessticker.hz.web;

import fitnessticker.hz.DistributedData;
import fitnessticker.hz.dto.ClientMsg;
import fitnessticker.hz.dto.FilterParams;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.hazelcast.cluster.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

    private final SocketIOServer serverSocket;
    private final Member localMember;
    private final DistributedData<Member, UUID> distributedData;
    private final Map<UUID, SocketIOClient> clients = new HashMap<>();

    public WebServer(
            SocketIOServer serverSocket,
            Member localMember,
            DistributedData<Member, UUID> distributedData
    ) {
        this.serverSocket = serverSocket;
        this.localMember = localMember;
        this.distributedData = distributedData;
        init();
    }

    private void init() {
        serverSocket.addConnectListener(this::onConnect);
        serverSocket.addDisconnectListener(this::onDisconnect);
        serverSocket.addEventListener("save-filter-params", FilterParams.class, this::onSaveFilters);
        serverSocket.start();
    }

    private void onConnect(SocketIOClient client) {
        UUID sessionId = client.getSessionId();
        //store new client for this local member (it may not be actually stored in this hz member's partition)
        distributedData.onClientMembers(clientMembers -> clientMembers.put(sessionId, localMember));
        clients.put(sessionId, client);
        LOGGER.info("Connected ws client {}", sessionId);
    }

    //called also on inactive connection (when no data is pushed through the web socket)
    private void onDisconnect(SocketIOClient client) {
        UUID sessionId = client.getSessionId();
        distributedData.onClientMembers(clientMembers -> clientMembers.remove(sessionId));
        clients.remove(sessionId);
        LOGGER.info("Disconnected ws client {}", sessionId);
    }

    private void onSaveFilters(SocketIOClient client, FilterParams filterParams, AckRequest ackRequest) {
        UUID sessionId = client.getSessionId();
        distributedData.onClientFilters(clientFilters -> clientFilters.put(sessionId, filterParams));
    }

    public void send(UUID sessionId, ClientMsg msg) {
        clients.get(sessionId).sendEvent("person", msg);
    }

    @Override
    public void close() {
        serverSocket.stop();
    }
}
