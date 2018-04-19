package edu.rice.ephynet;

import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix1D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;

import java.util.*;

public class Enumerator {
    private int matrixSize;
    private int nReticulations;
    private int nextSubnetNumber;
    private int[] initialNetwork;

    private IntMatrix2D speciationMap;
    private IntMatrix1D reticulationMap;
    private Set<Integer> completeNetworks;

    Enumerator(final int nLeaves, final int nReticulations) {
        matrixSize = 1 << 27;

        this.nReticulations = nReticulations;
        initialNetwork = new int[nLeaves];

        speciationMap = new SparseIntMatrix2D(matrixSize, matrixSize, nLeaves, 0.8, 0.9);
        reticulationMap = new SparseIntMatrix1D(matrixSize, nLeaves, 0.8, 0.9);
        completeNetworks = new HashSet<>();

        nextSubnetNumber = 1;
        while (nextSubnetNumber < nLeaves) {
            initialNetwork[nextSubnetNumber] = nextSubnetNumber;
            nextSubnetNumber++;
        }
    }

    void enumerate() {
        recurseEnumerate(initialNetwork, 0);
        System.out.println(completeNetworks.size());
    }

    void recurseEnumerate(final int[] network, final int reticulationCount) {
        // add reticulation nodes
        if (reticulationCount < nReticulations) {
            for (int i = 0; i < network.length; i++) {
                final int child = network[i];
                int parent = reticulationMap.get(child);
                if (parent == 0) {
                    parent = nextSubnetNumber;
                    nextSubnetNumber++;
                    reticulationMap.set(child, parent);
                }

                final int[] newNetwork = new int[network.length + 1];
                System.arraycopy(network, 0, newNetwork, 0, network.length);
                newNetwork[i] = parent;
                newNetwork[network.length] = parent;

                recurseEnumerate(newNetwork, reticulationCount + 1);
            }
        }

        // add speciation nodes
        if (network.length == 2) { // special case for only two open nodes
            int childA;
            int childB;
            if (network[0] <= network[1]) {
                childA = network[0];
                childB = network[1];
            } else {
                childA = network[1];
                childB = network[0];
            }

            Integer parent = speciationMap.get(childA, childB);
            if (parent == 0) {
                parent = nextSubnetNumber;
                nextSubnetNumber++;

                speciationMap.set(childA, childB, parent);

                if (reticulationCount == nReticulations) {
                    completeNetworks.add(parent); // no deeper nodes possible
                } else {
                    final int[] newNetwork = new int[1];
                    newNetwork[0] = parent;
                    recurseEnumerate(newNetwork, reticulationCount);
                }
            } // no "else", as this path has already been traversed
        } else if (network.length > 2) {
            for (int i = 1; i < network.length; i++) {
                for (int j = 0; j < i; j++) {
                    int childA;
                    int childB;
                    if (network[i] <= network[j]) {
                        childA = network[i];
                        childB = network[j];
                    } else {
                        childA = network[j];
                        childB = network[i];
                    }

                    Integer parent = speciationMap.get(childA, childB);
                    if (parent == 0) {
                        parent = nextSubnetNumber;
                        nextSubnetNumber++;

                        speciationMap.set(childA, childB, parent);
                    }

                    // make a copy of the previous network with the new node in place of j
                    // and without i (replace i with last node if last gets chopped off)
                    final int[] newNetwork = new int[network.length - 1];
                    System.arraycopy(network, 0, newNetwork, 0, newNetwork.length);
                    newNetwork[j] = parent;
                    if (i != newNetwork.length)
                        newNetwork[i] = network[newNetwork.length];

                    recurseEnumerate(newNetwork, reticulationCount);
                }
            }
        }
    }
}
