/*
 * Copyright (c) 2013-2015 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.algo.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.module.algo.generator.config.ErdosRenyiConfig;

import java.util.*;

/**
 * {@link RelationshipGenerator} implementation according to Erdos-Renyi random graphs. These are a basic class of
 * random graphs with exponential cut-off. A phase transition from many components graph to a completely connected graph
 * is present.
 * <p/>
 * The algorithm has a switch from sparse ER graph to dense ER graph generator. The sparse algorithm is based on
 * trial-correction method as suggested in the paper cited below. This is extremely inefficient for nearly-complete
 * graphs. The dense algorithm (written by GraphAware) is based on careful avoiding of edge indices in the selection.
 * <p/>
 * The switch allows to generate even complete graphs (eg. (V, E) = (20, 190) in a reasonable time. The switch is turned
 * on to dense graph generator for the case when number of edges requested is greater than half of total possible edges
 * that could be generated.
 */
public class ErdosRenyiRelationshipGenerator extends BaseRelationshipGenerator<ErdosRenyiConfig> {

    private final Random random = new Random();

    /**
     * Construct a new generator.
     *
     * @param configuration of the generator.
     */
    public ErdosRenyiRelationshipGenerator(ErdosRenyiConfig configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        long threshold = getConfiguration().getNumberOfEdges() * 4;
        long potentialEdges = getConfiguration().getNumberOfNodes() * (getConfiguration().getNumberOfNodes() - 1);

        if (threshold > potentialEdges) {
            return doGenerateEdgesWithOmitList(); // Make sure to avoid edges
        }

        return doGenerateEdgesSimpler(); // Be more heuristic (pajek implementation using HashSet).
    }

    /**
     * This algorithm is implemented as recommended in
     * <p/>
     * Efficient generation of large random networks
     * by Vladimir Batagelj and Ulrik Brandes
     * <p/>
     * PHYSICAL REVIEW E 71, 036113, 2005
     *
     * @return edge list
     */
    private List<SameTypePair<Integer>> doGenerateEdgesSimpler() {
        final int numberOfNodes = getConfiguration().getNumberOfNodes();
        final long numberOfEdges = getConfiguration().getNumberOfEdges();

        final HashSet<SameTypePair<Integer>> edges = new HashSet<>();

        while (edges.size() < numberOfEdges) {
            int origin = random.nextInt(numberOfNodes);
            int target = random.nextInt(numberOfNodes);

            if (target == origin) {
                continue;
            }

            edges.add(new UnorderedPair<>(origin, target));
        }

        return new LinkedList<>(edges);
    }

    /**
     * Improved implementation of Erdos-Renyi generator based on bijection from
     * edge labels to edge realisations. Works very well for large number of nodes,
     * but is slow with increasing number of edges. Best for denser networks, with
     * a clear giant component.
     *
     * @return edge list
     */
    protected List<SameTypePair<Integer>> doGenerateEdgesWithOmitList() {
        final int numberOfNodes = getConfiguration().getNumberOfNodes();
        final int numberOfEdges = getConfiguration().getNumberOfEdges();
        final long maxEdges = numberOfNodes * (numberOfNodes - 1) / 2;

        final List<SameTypePair<Integer>> edges = new LinkedList<>();

        for (Long index : edgeIndices(numberOfEdges, maxEdges)) {
            edges.add(indexToEdgeBijection(index));
        }

        return edges;
    }

    /**
     * Maps an index in a hypothetical list of all edges to the actual edge.
     *
     * @param index index
     * @return an edge based on its unique label
     */
    private UnorderedPair<Integer> indexToEdgeBijection(long index) {
        long i = (long) Math.ceil((Math.sqrt(1 + 8 * (index + 1)) - 1) / 2);
        long diff = index + 1 - (i * (i - 1)) / 2;

        return new UnorderedPair<>((int) i, (int) diff - 1);
    }

    private Set<Long> edgeIndices(int numberOfEdges, long maxEdges) {
        Set<Long> result = new HashSet<>(numberOfEdges);
        while (result.size() < numberOfEdges) {
            result.add(nextLong(maxEdges));
        }
        return result;
    }

    private long nextLong(long length) {
        return (long) (random.nextDouble() * length);
    }
}
