package fitnessticker.hz.dto;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class IntMsg<Client> implements DataSerializable {
    private List<Client> clients;
    private String firstname;
    private String lastname;
    private int weight;

    public IntMsg() {
    }

    public IntMsg(List<Client> clients, ExtMsg extMsg) {
        this(clients, extMsg.getFirstname(), extMsg.getLastname(), extMsg.getWeight());
    }

    public IntMsg(List<Client> clients, String firstname, String lastname, int weight) {
        this.clients = clients;
        this.firstname = firstname;
        this.lastname = lastname;
        this.weight = weight;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntMsg)) return false;
        IntMsg<?> intMsg = (IntMsg<?>) o;
        return weight == intMsg.weight && Objects.equals(clients, intMsg.clients) && Objects.equals(firstname, intMsg.firstname) && Objects.equals(lastname, intMsg.lastname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clients, firstname, lastname, weight);
    }

    @Override
    public String toString() {
        return "IntMsg{" +
                "clients=" + clients +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(clients);
        out.writeString(firstname);
        out.writeString(lastname);
        out.writeInt(weight);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        clients = in.readObject();
        firstname = in.readString();
        lastname = in.readString();
        weight = in.readInt();
    }
}
