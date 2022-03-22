package fitnessticker.hz;

import fitnessticker.hz.dto.IntMsg;

public interface InternalSender<Member, Client> {
    void send(Member dest, IntMsg<Client> msg);
}
