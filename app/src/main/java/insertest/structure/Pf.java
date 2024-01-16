package insertest.structure;

/** "PCAP"-file composed of a certain number of type01..type06 "messages" */
public class Pf {
    public final int typ01, typ02, typ03, typ04, typ05, typ06;

    public Pf(int c01, int c02, int c03, int c04, int c05, int c06) {
        typ01 = c01; typ02 = c02; typ03 = c03; typ04 = c04; typ05 = c05; typ06 = c06;
    };
}
