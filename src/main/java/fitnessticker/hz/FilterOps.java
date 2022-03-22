package fitnessticker.hz;

public class FilterOps {
    private final int weight;

    public FilterOps(int weight) {
        this.weight = weight;
    }

    public boolean isLte(Integer limit) {
        return limit == null || weight <= limit;
    }

    public boolean isGte(Integer limit) {
        return limit == null || weight >= limit;
    }
}
