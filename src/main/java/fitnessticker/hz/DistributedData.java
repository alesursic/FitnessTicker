package fitnessticker.hz;

import fitnessticker.hz.dto.FilterParams;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Threads calling this class:
 *  1. hazelcast event handler (cluster membership events)
 *  2. RMQ msg receiver
 *
 * Therefore this class must be thread-safe!
 */
public class DistributedData<Member, Client> {
    private final Map<Client, Member> clientMembers;
    private final Map<Client, FilterParams> clientFilters;

    public DistributedData(
            Map<Client, Member> clientMembers,
            Map<Client, FilterParams> clientFilters
    ) {
        this.clientMembers = clientMembers;
        this.clientFilters = clientFilters;
    }

    //curried 2-arg consumer
    public synchronized void onAll(Function<Map<Client, FilterParams>, Consumer<Map<Client, Member>>> f) {
        f.apply(clientFilters).accept(clientMembers);
    }

    public synchronized <R> R onClientMembers(Function<Map<Client, Member>, R> f) {
        return f.apply(clientMembers);
    }

    public <R> R onClientFilters(Function<Map<Client, FilterParams>, R> f) {
        return f.apply(clientFilters);
    }
}
