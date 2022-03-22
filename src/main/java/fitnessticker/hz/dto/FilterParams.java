package fitnessticker.hz.dto;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class FilterParams implements DataSerializable {
    private Integer fromWeight;
    private Integer toWeight;

    public FilterParams() {
    }

    public FilterParams(Integer fromWeight, Integer toWeight) {
        this.fromWeight = fromWeight;
        this.toWeight = toWeight;
    }

    public Integer getFromWeight() {
        return fromWeight;
    }

    public void setFromWeight(Integer fromWeight) {
        this.fromWeight = fromWeight;
    }

    public Integer getToWeight() {
        return toWeight;
    }

    public void setToWeight(Integer toWeight) {
        this.toWeight = toWeight;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(fromWeight == null ? -1 : fromWeight);
        out.writeInt(toWeight == null ? -1 : toWeight);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int temp = in.readInt();
        fromWeight = temp == -1 ? null : temp;

        temp = in.readInt();
        toWeight = temp == -1 ? null : temp;
    }
}
