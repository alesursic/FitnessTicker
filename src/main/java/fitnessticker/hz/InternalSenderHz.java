package fitnessticker.hz;

import fitnessticker.hz.dto.IntMsg;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.ITopic;

import java.util.UUID;

public class InternalSenderHz implements InternalSender<Member, UUID> {
    private final LocalData<Member, UUID> localData;

    public InternalSenderHz(LocalData<Member, UUID> localData) {
        this.localData = localData;
    }

    @Override
    public void send(Member dest, IntMsg<UUID> msg) {
        localData.onAll(memberTopics -> {
            if (memberTopics.containsKey(dest)) {
                ITopic<IntMsg<UUID>> pubsub = memberTopics.get(dest);
                pubsub.publishAsync(msg);
            }
        });
    }
}
