package fitnessticker.hz.dto;

public class ExtMsg {
    private String firstname;
    private String lastname;
    private int weight;
    private String address;

    public ExtMsg() {
    }

    public ExtMsg(String firstname, String lastname, int weight, String address) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.weight = weight;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    //Helpers:

    public static ExtMsg parse(String input) {
        String[] parts = input.split("-");
        return new ExtMsg(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                null
        );
    }
}
