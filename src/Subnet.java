/*
 * @author Rand7Y9Z@gmail.com
 * @since 2024
 * https://github.com/r7d9Y/R7Calc
 */

import java.util.Arrays;
import java.util.stream.IntStream;

public class Subnet implements Comparable<Subnet> {
    private IpAddress addr;
    private IpAddress mask;

    public void setAddr(IpAddress addr) {
        this.addr = addr;
    }

    public void setMask(IpAddress mask) {
        this.mask = mask;
    }

    private static final IllegalArgumentException invalidValue = new IllegalArgumentException("Invalid Value!");
    private static final IllegalArgumentException invalidSNMValue = new IllegalArgumentException("Invalid SNM Value!");

    public Subnet(String s) {
        s = s.trim();
        try {
            String[] r = s.split("/", 2);
            if (IpAddress.isValidIPAddress(r[0]) && isValidSnm(new IpAddress(r[1]).getAsInt())) {
                mask = new IpAddress(r[1]);
                addr = getNetwork(new IpAddress(r[0]).getAsInt(), mask.getAsInt());

            } else if (IpAddress.isValidIPAddress(r[0]) && Integer.parseInt(r[1]) >= 0 && Integer.parseInt(r[1]) <= 32) {
                mask = new IpAddress((~0) << (32 - Integer.parseInt(r[1])));
                addr = getNetwork(new IpAddress(r[0]).getAsInt(), mask.getAsInt());
            } else {
                throw invalidSNMValue;
            }

        } catch (Exception e) {
            throw invalidValue;
        }

    }

    public Subnet(IpAddress ip, int suffix) {
        if (suffix < 0 || suffix > 32) {
            throw invalidSNMValue;
        }
        mask = new IpAddress((~0) << (32 - suffix));
        addr = getNetwork(ip.getAsInt(), suffix);
    }

    public Subnet(IpAddress ip, IpAddress suffix) {
        if (!isValidSnm(suffix.getAsInt())) {
            throw invalidSNMValue;
        }
        mask = suffix;
        addr = getNetwork(ip.getAsInt(), suffix.getAsInt());
    }

    public Subnet(String s, String t) {
        s = s.trim();
        t = t.trim();
        Subnet r = new Subnet(s + "/" + t);
        addr = r.addr;
        mask = r.mask;
    }

    public Subnet(IpAddress ip) {
        int[] oct = ip.getAsArray();
        int i = 3;
        int r = 1;
        while (i > 0 && oct[i] == 0) {
            i--;
            r++;
        }

        mask = new IpAddress((~0) << (8 * r));
        addr = getNetwork(ip.getAsInt(), mask.getAsInt());
    }

    public IpAddress getNetMask() {
        return mask;
    }

    public IpAddress getNetAddress() {
        return addr;
    }

    public int getNumberOfHosts() {
        return ~mask.getAsInt() - 1;
    }

    @Override
    public String toString() {
        return addr.toString() + "/" + mask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subnet subnet = (Subnet) o;
        return addr.getAsInt() == subnet.addr.getAsInt() && mask.getAsInt() == subnet.mask.getAsInt();
    }

    @Override
    public int compareTo(Subnet o) {
        return this.getNetAddress().compareTo(o.getNetAddress());
    }

    public Subnet copy() {
        return new Subnet(addr, mask);
    }

    //------------------------------------------------------------------------------------------------------------------

    protected int[] allIPs() {
        return getAllIpsInNetwork(addr.getAsInt(), intToString(mask.getAsInt()).lastIndexOf('1') + 1);
    }

    protected boolean isInNetwork(IpAddress ip) {
        int searchFor = ip.getAsInt();
        int[] allIPs = getAllIpsInNetwork(addr.getAsInt(), intToString(mask.getAsInt()).lastIndexOf('1') + 1);

        return Arrays.stream(allIPs).anyMatch(allIP -> searchFor == allIP);
    }

    protected boolean contains(IpAddress ip) {
        return isInNetwork(ip);
    }

    protected IpAddress getBroadcastAddr() {
        int[] ips = allIPs();
        return new IpAddress(ips[ips.length - 1]);
    }

    protected IpAddress getFirstHostIp() {
        return new IpAddress(addr.getAsInt() + 1);
    }

    protected IpAddress getLastHostIp() {
        int[] ips = allIPs();
        return new IpAddress(ips[ips.length - 2]);
    }

    protected IpAddress[] getAllIpsInNetwork() {
        int[] ips = allIPs();
        return Arrays.stream(ips).mapToObj(IpAddress::new).toArray(IpAddress[]::new);
    }

    protected Subnet getNextSubnet() {
        return new Subnet(new IpAddress(getBroadcastAddr().getAsInt() + 1), mask);
    }

    /**
     * reserved spaces addresses
     */
    protected static final Subnet LOCALNET = new Subnet("127.0.0.1/255.0.0.0");
    protected static final Subnet PRIVATENET10 = new Subnet("10.0.0.0/255.0.0.0");
    protected static final Subnet PRIVATENET172 = new Subnet("172.16.0.0/255.240.0.0");
    protected static final Subnet PRIVATENET192 = new Subnet("192.168.0.0/255.255.0.0");
    protected static final Subnet LINK_LOCAL = new Subnet("169.254.0.0/255.255.0.0");

    public boolean isPrivateSubnet() {
        return (PRIVATENET10.contains(this.getNetAddress()) && PRIVATENET10.contains(getBroadcastAddr()))
                || (PRIVATENET172.contains(this.getNetAddress()) && PRIVATENET172.contains(getBroadcastAddr()))
                || (PRIVATENET192.contains(this.getNetAddress()) && PRIVATENET192.contains(getBroadcastAddr()));
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * @param snm is the given SNM
     * @return is a true when the SNM is valid
     */
    private static boolean isValidSnm(int snm) {
        return !intToString(snm).substring(0, intToString(snm).lastIndexOf('1') + 1).contains("0");
    }

    /**
     * returns the int in its binary form , the length is fixed and set to 32 as that is the full possible length of an int
     *
     * @param n ist the given integer
     * @return is a String
     */
    protected static String intToString(int n) {
        StringBuilder s = new StringBuilder().append(String.format("%32s", Integer.toBinaryString(n)));
        IntStream.range(1, s.length()).filter(i -> s.charAt(i - 1) == ' ').forEach(i -> s.delete(i - 1, i).insert(i - 1, '0'));
        return s.toString();
    }

    /**
     * @param network ia the network-address
     * @param suffix  is the given suffix
     * @return is an Array with all Addresses in network
     */
    protected static int[] getAllIpsInNetwork(int network, int suffix) {
        if (suffix < 0 || suffix > 32) throw new IllegalArgumentException("Illegal SNM");
        if (suffix == 0) throw new IllegalArgumentException("Network to big to calculate all IPs!");

        network = getNetwork(network, suffix).getAsInt();

        int[] r = new int[1 << (32 - suffix)];

        for (int i = 0; i < r.length; i++) r[i] = network | i;

        return r;
    }

    /**
     * @param ip     is the given IP-Address
     * @param suffix is the given suffix
     * @return is the network-IP of the given IP-Address, that gets calculated by using the suffix
     */
    protected static IpAddress getNetwork(int ip, int suffix) {
        suffix = intToString(suffix).lastIndexOf('1') + 1;
        if (suffix > 32) throw invalidSNMValue;
        return new IpAddress(ip & ((~0) << (32 - suffix)));
    }

}
