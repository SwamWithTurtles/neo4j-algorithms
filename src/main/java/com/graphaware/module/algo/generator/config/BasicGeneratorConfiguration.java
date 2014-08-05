package com.graphaware.module.algo.generator.config;

import com.graphaware.module.algo.generator.node.NodeCreator;
import com.graphaware.module.algo.generator.relationship.RelationshipCreator;
import com.graphaware.module.algo.generator.relationship.RelationshipGenerator;

/**
 * Basic implementation of {@link GeneratorConfiguration} where everything can be configured by constructor instantiation,
 * except for batch size, which defaults to 1000.
 */
public class BasicGeneratorConfiguration implements GeneratorConfiguration {

    private final RelationshipGenerator<?> relationshipGenerator;
    private final NodeCreator nodeCreator;
    private final RelationshipCreator relationshipCreator;

    public BasicGeneratorConfiguration(RelationshipGenerator<?> relationshipGenerator, NodeCreator nodeCreator, RelationshipCreator relationshipCreator) {
        this.relationshipGenerator = relationshipGenerator;
        this.nodeCreator = nodeCreator;
        this.relationshipCreator = relationshipCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNodes() {
        return relationshipGenerator.getNumberOfNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeCreator getNodeCreator() {
        return nodeCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipCreator getRelationshipCreator() {
        return relationshipCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipGenerator<?> getRelationshipGenerator() {
        return relationshipGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatchSize() {
        return 1000;
    }
}
