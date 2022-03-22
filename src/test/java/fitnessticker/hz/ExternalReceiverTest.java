package fitnessticker.hz;

import fitnessticker.hz.dto.ExtMsg;
import fitnessticker.hz.dto.FilterParams;
import fitnessticker.hz.dto.IntMsg;
import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExternalReceiverTest {
    @Test
    public void singleBoundFilters() {
        //Prepare
        final int member0 = 0;
        final int member1 = 1;
        final int client0 = 10; //interested in very slim people
        final int client1 = 11; //interested in slim people
        final int client2 = 12; //interested in fat people
        final int client3 = 13; //interested in very fat people

        Map<Integer, Integer> clientMembers = ImmutableMap.of(
                client0, member0,
                client1, member0,
                client2, member1,
                client3, member1
        );
        Map<Integer, FilterParams> clientFilters = ImmutableMap.of(
                client0, new FilterParams(null, 60),
                client1, new FilterParams(null, 75),
                client2, new FilterParams(75, null),
                client3, new FilterParams(90, null)
        );

        DistributedData<Integer, Integer> distributedData = new DistributedData<>(clientMembers, clientFilters);
        InternalSenderSink sink = new InternalSenderSink();
        ExternalReceiver<Integer, Integer> externalReceiver = new ExternalReceiver<>(sink, distributedData);

        //Execute
        ExtMsg verySlim = new ExtMsg("x0", "y0", 55, null);
        ExtMsg slim = new ExtMsg("x1", "y1", 65, null);
        ExtMsg heavy = new ExtMsg("x2", "y2", 80, null);
        ExtMsg veryHeavy = new ExtMsg("x3", "y3", 100, null);

        externalReceiver.accept(verySlim);
        externalReceiver.accept(slim);
        externalReceiver.accept(heavy);
        externalReceiver.accept(veryHeavy);

        //Verify
        List<P2<Integer, IntMsg<Integer>>> expected = Arrays.asList(
                P.p(member0, new IntMsg<>(Arrays.asList(client0, client1), verySlim)), //2 clients of interest!
                P.p(member0, new IntMsg<>(List.of(client1), slim)),
                P.p(member1, new IntMsg<>(List.of(client2), heavy)),
                P.p(member1, new IntMsg<>(Arrays.asList(client2, client3), veryHeavy)) //2 clients of interest!
        );
        Assert.assertEquals(expected, sink.sent);
    }

    @Test
    public void intervalFilters() {
        //Prepare
        final int member0 = 0;
        final int client0 = 10;

        Map<Integer, Integer> clientMembers = ImmutableMap.of(client0, member0);
        Map<Integer, FilterParams> clientFilters = ImmutableMap.of(client0, new FilterParams(60, 75));

        InternalSenderSink sink = new InternalSenderSink();
        DistributedData<Integer, Integer> distributedData = new DistributedData<>(clientMembers, clientFilters);
        ExternalReceiver<Integer, Integer> externalReceiver = new ExternalReceiver<>(sink, distributedData);

        //Execute
        ExtMsg verySlim = new ExtMsg("x0", "y0", 55, null);
        ExtMsg slim = new ExtMsg("x1", "y1", 65, null); //only this message matches client filters
        ExtMsg heavy = new ExtMsg("x2", "y2", 80, null);

        externalReceiver.accept(verySlim);
        externalReceiver.accept(slim);
        externalReceiver.accept(heavy);

        //Verify
        List<P2<Integer, IntMsg<Integer>>> expected = List.of(
                P.p(member0, new IntMsg<>(List.of(client0), slim))
        );
        Assert.assertEquals(expected, sink.sent);
    }

    //Helpers:

    private static class InternalSenderSink implements InternalSender<Integer, Integer> {
        private final List<P2<Integer, IntMsg<Integer>>> sent = new LinkedList<>();

        @Override
        public void send(Integer dest, IntMsg<Integer> msg) {
            sent.add(P.p(dest, msg));
        }
    }
}
