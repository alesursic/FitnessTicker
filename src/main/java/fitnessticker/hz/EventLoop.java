package fitnessticker.hz;

import fitnessticker.hz.dto.ExtMsg;
import com.hazelcast.cluster.Member;
import com.hazelcast.collection.IQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EventLoop implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventLoop.class);

    private final IQueue<String> externalQueue;
    private final ExternalReceiver<Member, UUID> msgHandler;

    public EventLoop(IQueue<String> externalQueue, ExternalReceiver<Member, UUID> msgHandler) {
        this.externalQueue = externalQueue;
        this.msgHandler = msgHandler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                //why does cmdQueue.take not support thread interrupts?
                String msg = externalQueue.poll(100, TimeUnit.MILLISECONDS);
                if (msg == null) {
                    continue;
                }
                LOGGER.debug("RMQ consumed {}", msg);

                ExtMsg parsed = ExtMsg.parse(msg);
                msgHandler.accept(parsed);
            } catch (InterruptedException e) {
                LOGGER.warn("External queue consumer's event loop has been interrupted. Exiting..");
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Consumer finished");
    }
}
