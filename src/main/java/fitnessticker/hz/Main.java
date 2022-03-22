package fitnessticker.hz;

import fitnessticker.hz.dto.FilterParams;
import fitnessticker.hz.dto.IntMsg;
import fitnessticker.hz.web.WebServer;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static fitnessticker.hz.PubSubHzUtil.toTopic;

public class Main {
    public static void main(String[] args) {
        //General hazelcast stuff
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Cluster cluster = hz.getCluster();
        Member localMember = cluster.getLocalMember();

        //Distributed data & local data
        LocalData<Member, UUID> localData = new LocalData<>(new HashMap<>());
        IMap<UUID, Member> clientMembers = hz.getMap("member-clients");
        IMap<UUID, FilterParams> clientFilters = hz.getMap("client-filters");
        DistributedData<Member, UUID> distributedData = new DistributedData<>(clientMembers, clientFilters);

        //Web server
        Configuration serverConfig = new Configuration();
        serverConfig.setHostname("localhost");
        int port = Integer.parseInt(System.getenv("SOCKET_IO_PORT"));
        serverConfig.setPort(port);
        serverConfig.setUpgradeTimeout(5000);
        serverConfig.setPingInterval(10000);
        serverConfig.setPingTimeout(30000);
        serverConfig.setFirstDataTimeout(10000);
        SocketIOServer socketIOServer = new SocketIOServer(serverConfig);
        WebServer webServer = new WebServer(socketIOServer, localMember, distributedData);

        //HZ pubsub subscription (I subscribe myself to "my" topic)
        ITopic<IntMsg<UUID>> pubsub = hz.getTopic(toTopic(localMember));
        pubsub.addMessageListener(new InternalReceiver(webServer));

        //Cluster membership event listener
        DistributedDataChanges distributedDataChanges = new DistributedDataChanges(distributedData);
        LocalDataChanges localDataChanges = new LocalDataChanges(hz, localData);
        cluster.addMembershipListener(distributedDataChanges);
        cluster.addMembershipListener(localDataChanges);

        //Initial discovery (for local data only)
        cluster
                .getMembers()
                .stream()
                .map(member -> new MembershipEvent(cluster, member, 0, null))
                .forEach(localDataChanges::memberAdded);

        //RMQ listener thread
        Thread t = new Thread(
                new EventLoop(
                        hz.getQueue("rmq"),
                        new ExternalReceiver<>(
                                new InternalSenderHz(localData),
                                distributedData
                        )
                ),
                "event-dispatcher-t"
        );
        t.start();

        //todo: close SocketIOServer
    }

    //NOTE: temp code
    private static void populateClientFilters(
            IMap<Member, List<UUID>> memberClients,
            IMap<UUID, FilterParams> clientFilters,
            Member localMember
    ) {
        List<UUID> clients = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        memberClients.put(localMember, clients);
        int uniqueId = Integer.parseInt(System.getenv("UNIQUE_ID"));
        if (uniqueId == 0) {
            //slim fitness ticker
            clientFilters.put(clients.get(0), new FilterParams(null, 60));
            clientFilters.put(clients.get(1), new FilterParams(null, 75));
        } else {
            //fat fitness ticker
            clientFilters.put(clients.get(0), new FilterParams(75, null));
            clientFilters.put(clients.get(1), new FilterParams(90, null));
        }
    }
}
