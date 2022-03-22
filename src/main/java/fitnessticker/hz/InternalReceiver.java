package fitnessticker.hz;

import fitnessticker.hz.dto.ClientMsg;
import fitnessticker.hz.dto.IntMsg;
import fitnessticker.hz.web.WebServer;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class InternalReceiver implements MessageListener<IntMsg<UUID>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalReceiver.class);

    private final WebServer webServer;

    public InternalReceiver(WebServer webServer) {
        this.webServer = webServer;
    }

    @Override
    public void onMessage(Message<IntMsg<UUID>> message) {
        Member src = message.getPublishingMember();
        IntMsg<UUID> msgObj = message.getMessageObject();

        LOGGER.info("From {} received {}", src.getAddress(), msgObj);

        ClientMsg clientMsg = new ClientMsg(msgObj.getFirstname(), msgObj.getLastname(), msgObj.getWeight());
        msgObj.getClients().forEach(uuid -> webServer.send(uuid, clientMsg));
    }
}
