package fitnessticker.hz;

import fitnessticker.hz.dto.ExtMsg;
import fitnessticker.hz.dto.FilterParams;
import fitnessticker.hz.dto.IntMsg;

import java.util.*;
import java.util.function.Consumer;

public class ExternalReceiver<Member, Client> implements Consumer<ExtMsg> {
    private final InternalSender<Member, Client> internalSender;
    private final DistributedData<Member, Client> distributedData;

    public ExternalReceiver(
            InternalSender<Member, Client> internalSender,
            DistributedData<Member, Client> distributedData
    ) {
        this.internalSender = internalSender;
        this.distributedData = distributedData;
    }

    @Override
    public void accept(ExtMsg extMsg) {
        //find interested clients based on their filters
        FilterOps filterOps = new FilterOps(extMsg.getWeight());
        List<Client> interestedClients = distributedData.onClientFilters((Map<Client, FilterParams> clientFilters) -> clientFilters
                .entrySet()
                .stream()
                .filter(entry -> {
                    FilterParams filterParams = entry.getValue();
                    return filterOps.isGte(filterParams.getFromWeight()) && filterOps.isLte(filterParams.getToWeight());
                })
                .map(Map.Entry::getKey)
                .toList()
        );

        //find members that these clients are connected to (some members don't have any interested clients - skip sending)
        Map<Member, List<Client>> interestedMemberClients = distributedData.onClientMembers(clientMembers -> {
            Map<Member, List<Client>> result = new HashMap<>();
            for (Client client : interestedClients) {
                result.merge(
                        clientMembers.get(client),
                        new LinkedList<>(List.of(client)),
                        (oldValue, newValue) -> {
                            oldValue.addAll(newValue);
                            return oldValue;
                        }
                );
            }
            return result;
        });

        //send a message to each member that has one or more interested clients
        //in the case more than 1 client from the same member is interested, only 1 message is sent to that member
        interestedMemberClients.forEach((memberDest, clients) -> internalSender.send(
                memberDest,
                new IntMsg<>(clients, extMsg)
        ));
    }

    //Helpers:

    private static <A, B> Map<B, A> reverse(Map<A, Set<B>> memberClients) {
        Map<B, A> result = new HashMap<>();
        memberClients.forEach((member, clients) -> clients.forEach(client -> result.put(client, member)));
        return result;
    }
}
