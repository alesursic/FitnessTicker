package fitnessticker.hz;

import com.hazelcast.cluster.Member;

public class PubSubHzUtil {
    public static String toTopic(Member member) {
        return member.getUuid().toString();
    }
}
