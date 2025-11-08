package fr.anthonus.dataBase;

public class IP {
    public String address;
    public boolean banned;
    public String until;
    public String reason;

    public IP(String address, boolean banned, String until, String reason) {
        this.address = address;
        this.banned = banned;
        this.until = until;
        this.reason = reason;
    }

    public IP(String address) {
        this.address = address;
        this.banned = false;
        this.until = null;
        this.reason = null;
    }
}
