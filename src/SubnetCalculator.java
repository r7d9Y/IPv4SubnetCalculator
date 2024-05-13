/*
 * @author Rand7Y9Z@gmail.com
 * @since 2024
 * https://github.com/r7d9Y/R7Calc
 */

import java.util.Scanner;

public class SubnetCalculator {
    private final static double version = 1.2;

    public static void main(String[] args) {
        System.out.printf("""
                Subnet Calculator (version %.1f)
                ----------------------------------------------------------
                Write 'ex' to exit the program.
                With this tool, you can calculate Ipv4 Subnets, the input should look like:
                [IP] [CIDR] OR [IP]/[CIDR] OR [IP]/[Subnet Mask]
                """, version);
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nInput: ");
            String input = scanner.nextLine().strip().trim();

            if (input.toLowerCase().contains("ex")) break;
            try {
                String[] parts = !input.contains("/") ? input.split(" ", 2) : input.split("/", 2);

                try {
                    int snm = Integer.parseInt(parts[1]);
                    if (snm >= 0 && snm <= 32) parts[1] = new IpAddress((~0) << (32 - snm)).toString();
                } catch (Exception ignored) {
                }

                input = parts[0] + "/" + parts[1];

                Subnet subnet = new Subnet(input);

                System.out.println("\nNetwork Address:   " + CYAN_BOLD + subnet.getNetAddress() + ANSI_RESET +
                        "\nSubnet Mask:       " + RED_BOLD + subnet.getNetMask() +
                        " (/" + (Subnet.intToString(subnet.getNetMask().getAsInt()).lastIndexOf("1") + 1) + ")" + ANSI_RESET +
                        "\nBroadcast Address: " + WHITE_BOLD + subnet.getBroadcastAddr() + ANSI_RESET +
                        "\nNumber of Hosts:   " + GREEN_BOLD + subnet.getNumberOfHosts() + ANSI_RESET +
                        "\nFirst Host IP:     " + WHITE_BOLD + subnet.getFirstHostIp() + ANSI_RESET +
                        "\nLast Host IP:      " + WHITE_BOLD + subnet.getLastHostIp() + ANSI_RESET +
                        "\nSubnet " + ANSI_WHITE + (subnet.isPrivateSubnet() ? "is" : "isn't") + ANSI_RESET
                        + " a private subnet" + ANSI_RESET +
                        "\nNext Subnet:       " + WHITE_BOLD + subnet.getNextSubnet().toString() + "\n" + ANSI_RESET);

            } catch (Exception e) {
                System.out.println("Invalid Input!");
            }

        } while (true);
    }

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String RED_BOLD = "\033[1;31m";
    private static final String GREEN_BOLD = "\033[1;32m";
    private static final String CYAN_BOLD = "\033[1;36m";
    private static final String WHITE_BOLD = "\033[1;37m";

}
