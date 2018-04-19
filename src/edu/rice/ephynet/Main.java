package edu.rice.ephynet;

public class Main {

    public static void main(String[] args) {
        /* try {
            Thread.sleep(20000);
        } catch (Exception e) {
        } */

        int nLeaves = 3;
        int nReticulations = 1;

        if (args.length == 2) {
            nLeaves = Integer.parseInt(args[0]);
            nReticulations = Integer.parseInt(args[1]);
        }

        final Enumerator e = new Enumerator(nLeaves, nReticulations);
        e.enumerate();
    }
}
