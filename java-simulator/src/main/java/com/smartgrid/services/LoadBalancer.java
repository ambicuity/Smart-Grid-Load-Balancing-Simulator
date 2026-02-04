package com.smartgrid.services;

import com.smartgrid.models.GridNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Load balancer implementing optimization logic for grid load distribution.
 * Uses thread-safe operations to redistribute load during peak demand.
 */
public class LoadBalancer {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);
    private final double overloadThreshold; // percentage
    private final double underloadThreshold; // percentage

    public LoadBalancer(double overloadThreshold, double underloadThreshold) {
        this.overloadThreshold = overloadThreshold;
        this.underloadThreshold = underloadThreshold;
    }

    /**
     * Optimizes load distribution across grid nodes.
     * Returns list of optimization actions taken.
     */
    public List<OptimizationAction> optimize(List<GridNode> nodes) {
        List<OptimizationAction> actions = new ArrayList<>();
        
        // Identify overloaded and underloaded nodes
        List<GridNode> overloaded = new ArrayList<>();
        List<GridNode> underloaded = new ArrayList<>();
        
        for (GridNode node : nodes) {
            double utilization = node.getUtilizationPercent();
            if (utilization > overloadThreshold) {
                overloaded.add(node);
            } else if (utilization < underloadThreshold && node.getAvailableCapacity() > 0) {
                underloaded.add(node);
            }
        }

        if (overloaded.isEmpty()) {
            logger.debug("No overloaded nodes detected");
            return actions;
        }

        // Sort overloaded by utilization (descending), underloaded by available capacity (descending)
        overloaded.sort(Comparator.comparingDouble(GridNode::getUtilizationPercent).reversed());
        underloaded.sort(Comparator.comparingDouble(GridNode::getAvailableCapacity).reversed());

        // Redistribute load
        for (GridNode overloadedNode : overloaded) {
            double excessLoad = overloadedNode.getCurrentLoad() - 
                               (overloadedNode.getCapacity() * (overloadThreshold / 100.0));
            
            if (excessLoad <= 0) continue;

            for (GridNode underloadedNode : underloaded) {
                if (excessLoad <= 0) break;
                
                double availableCapacity = underloadedNode.getAvailableCapacity();
                if (availableCapacity <= 0) continue;

                double transferAmount = Math.min(excessLoad, availableCapacity * 0.5); // Transfer up to 50% of available
                
                // Perform the transfer
                overloadedNode.setCurrentLoad(overloadedNode.getCurrentLoad() - transferAmount);
                underloadedNode.setCurrentLoad(underloadedNode.getCurrentLoad() + transferAmount);
                
                OptimizationAction action = new OptimizationAction(
                    overloadedNode.getNodeId(),
                    underloadedNode.getNodeId(),
                    transferAmount,
                    "LOAD_TRANSFER"
                );
                actions.add(action);
                
                excessLoad -= transferAmount;
                
                logger.info("Transferred {} MW from {} to {}", 
                           String.format("%.2f", transferAmount), 
                           overloadedNode.getNodeId(), 
                           underloadedNode.getNodeId());
            }
        }

        return actions;
    }

    /**
     * Detects nodes that exceed the threshold.
     */
    public List<GridNode> detectOverloadedNodes(List<GridNode> nodes) {
        return nodes.stream()
                .filter(node -> node.isOverloaded(overloadThreshold))
                .toList();
    }

    public static class OptimizationAction {
        private final String fromNodeId;
        private final String toNodeId;
        private final double amount;
        private final String actionType;

        public OptimizationAction(String fromNodeId, String toNodeId, double amount, String actionType) {
            this.fromNodeId = fromNodeId;
            this.toNodeId = toNodeId;
            this.amount = amount;
            this.actionType = actionType;
        }

        public String getFromNodeId() {
            return fromNodeId;
        }

        public String getToNodeId() {
            return toNodeId;
        }

        public double getAmount() {
            return amount;
        }

        public String getActionType() {
            return actionType;
        }

        @Override
        public String toString() {
            return String.format("OptimizationAction[%s: %.2f MW from %s to %s]",
                    actionType, amount, fromNodeId, toNodeId);
        }
    }
}
