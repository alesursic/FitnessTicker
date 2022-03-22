package fitnessticker.hz;

import fitnessticker.hz.dto.IntMsg;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;

import java.util.UUID;

import static fitnessticker.hz.PubSubHzUtil.toTopic;

public class LocalDataChanges implements MembershipListener {
    private final HazelcastInstance hazelcast;
    private final LocalData<Member, UUID> localData;

    public LocalDataChanges(HazelcastInstance hazelcast, LocalData<Member, UUID> localData) {
        this.hazelcast = hazelcast;
        this.localData = localData;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        Member addedMember = membershipEvent.getMember();
        localData.onAll(memberTopics -> {
            if (!memberTopics.containsKey(addedMember)) {
                ITopic<IntMsg<UUID>> pubsub = hazelcast.getTopic(toTopic(addedMember));
                memberTopics.put(addedMember, pubsub);
            }
        });
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Member removedMember = membershipEvent.getMember();
        localData.onAll(memberTopics -> memberTopics.remove(removedMember));
    }
}
