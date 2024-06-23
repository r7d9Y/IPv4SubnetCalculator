/*
 * @author Rand7Y9Z@gmail.com
 * @since 2024
 * https://github.com/r7d9Y/R7Calc
 */

import java.util.stream.IntStream;

public class IpAddress implements Comparable<IpAddress> {
    private int address;
    private static final IllegalArgumentException invalidValue = new IllegalArgumentException("Invalid Value!");


    IpAddress(int[] ip) {
        if (ip.length != 4 || !isValidIPAddress(ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3])) throw invalidValue;
        set(ip[0], ip[1], ip[2], ip[3]);
    }

    IpAddress(int ip1, int ip2, int ip3, int ip4) {
        if (!isValidIPAddress(ip1 + "." + ip2 + "." + ip3 + "." + ip4)) throw invalidValue;
        set(ip1, ip2, ip3, ip4);
    }

    IpAddress() {
        set(127, 0, 0, 1);
    }

    IpAddress(int ip) {
        address = ip;
    }

    IpAddress(String ip) {
        ip = ip.trim();
        if (!isValidIPAddress(ip)) throw invalidValue;

        String[] octs = ip.split("[.]", 4);

        set(Integer.parseInt(octs[0]), Integer.parseInt(octs[1]), Integer.parseInt(octs[2]), Integer.parseInt(octs[3]));
    }

    public void set(int ip1, int ip2, int ip3, int ip4) {
        address = (ip1 << 24) | (ip2 << 16) | (ip3 << 8) | (ip4);
    }

    public void set(int[] ip) {
        if (ip.length != 4 || !isValidIPAddress(ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3])) {
            throw invalidValue;
        }
        set(ip[0], ip[1], ip[2], ip[3]);
    }

    public void set(int ip) {
        address = ip;
    }

    public void set(String ip) {
        address = new IpAddress(ip).getAsInt();
    }

    public int getAsInt() {
        return address;
    }

    public int getOctet(int num) {
        if (num < 0 || num > 3) throw invalidValue;

        return toIntArray(address)[num];
    }

    public int[] getAsArray() {
        return toIntArray(address);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        int[] octs = toIntArray(address);
        return octs[0] + "." + octs[1] + "." + octs[2] + "." + octs[3];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.address == ((IpAddress) o).address;
    }

    @Override
    public int compareTo(IpAddress o) {
        return Integer.compareUnsigned(this.address, o.getAsInt());
    }

    public IpAddress copy() {
        return new IpAddress(address);
    }

    //----------------------------------------------------------------------------------------------------------------------

    /**
     * @param ip is the given ip
     * @return is the Array, were each octet is a value
     */
    public static int[] toIntArray(int ip) {
        return new int[]{ip >>> 24, (ip << 8) >>> 24, (ip << 16) >>> 24, (ip << 24) >>> 24};
    }

    /**
     * this methode checks if the given String is a valid IP-Address
     *
     * @param s is the String that should be an IP-Address for:
     * @return to be true, else it will be false
     * an IP-Address is valid if:
     * • it has 4 octets that are seperated b:
     * • 3 Points
     * • and all octets have a value between 0 and 255
     */
    public static boolean isValidIPAddress(String s) {
        try {
            int j = (int) IntStream.range(0, s.length()).filter(i -> s.charAt(i) == '.').count();
            String[] octs = s.split("[.]", 4);
            if (j != 3 || octs.length != 4) return false;
            for (int i = 0; i < 4; i++) {
                if (Integer.parseInt(octs[i]) < 0 || Integer.parseInt(octs[i]) > 255) return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param ip     is the given IP-Address
     * @param suffix is the given suffix
     * @return is the network-IP of the given IP-Address, that gets calculated by using the suffix
     */
    public static int getNetworkAddrAsInt(int ip, int suffix) {
        if (suffix < 0 || suffix > 32) throw invalidValue;

        return ip & ((~0) << (32 - suffix));
    }

    /**
     * @param ip     is the given IP-Address as an integer
     * @param suffix is the given suffix in decimal e.g. /24 (without the '/' of course)
     * @return is the network-IP of the given IP-Address, that gets calculated by using the suffix
     */
    public static IpAddress getNetworkAddr(int ip, int suffix) {
        if (suffix < 0 || suffix > 32) throw invalidValue;
        return new IpAddress(ip & ((~0) << (32 - suffix)));
    }

    public char getClassOfIP() {
        int oc = getOctet(0);

        if (oc < 128) return 'A';
        if (oc < 192) return 'B';
        if (oc < 224) return 'C';
        if (oc < 240) return 'D';
        return 'E';
    }

    //------------------------------------------------------------------------------------------------------------------

    public boolean isLoopbackAddr() {
        return Subnet.LOCALNET.isInNetwork(this);
    }

    public boolean isPrivateAddr() {
        return Subnet.PRIVATENET10.isInNetwork(this)
                || Subnet.PRIVATENET172.isInNetwork(this)
                || Subnet.PRIVATENET192.isInNetwork(this);
    }

    public boolean isLinkLocalAddr() {
        return Subnet.LINK_LOCAL.isInNetwork(this);
    }

}