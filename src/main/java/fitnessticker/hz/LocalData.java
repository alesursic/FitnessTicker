package fitnessticker.hz;

import fitnessticker.hz.dto.IntMsg;
import com.hazelcast.topic.ITopic;

import java.util.Map;
import java.util.function.Consumer;

/*
 * Threads calling this class:
 *  1. hazelcast event handler (cluster membership events)
 *  2. RMQ msg receiver
 *
 * Therefore this class must be thread-safe!
 */
public class LocalData<Member, Client> {
    private final Map<Member, ITopic<IntMsg<Client>>> memberTopics;

    public LocalData(Map<Member, ITopic<IntMsg<Client>>> memberTopics) {
        this.memberTopics = memberTopics;
    }

    public synchronized void onAll(Consumer<Map<Member, ITopic<IntMsg<Client>>>> f) {
        f.accept(memberTopics);
    }
}
