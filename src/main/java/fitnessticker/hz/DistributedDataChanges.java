package fitnessticker.hz;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DistributedDataChanges implements MembershipListener {
    private final DistributedData<Member, UUID> distributedData;

    public DistributedDataChanges(DistributedData<Member, UUID> distributedData) {
        this.distributedData = distributedData;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        //NOTE: Clients (and their filters) are added on client (web-socket) connect events
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Member removedMember = membershipEvent.getMember();
        distributedData.onAll(clientFilters -> clientMembers -> {
            Set<UUID> removedClients = clientMembers
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(removedMember))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            removedClients.forEach(clientFilters::remove); //remove filters held by clients
            removedClients.forEach(clientMembers::remove); //remove clients connected to the removed member
        });
    }
}
