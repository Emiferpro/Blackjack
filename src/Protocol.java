public class Protocol {
    public String InitMessage(String pname) {
        return "CONNECT;" + pname;
    }

    public String NewPlayerPacket(int id, String pname) {
        return "ADD;" + pname + ";" + id;
    }

    public String IDPacket(int id) {
        return "ID;" + id;
    }
}
