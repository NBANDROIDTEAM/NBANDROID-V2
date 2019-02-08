/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.layout.dependency.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * It represents the node of the graph. It holds a user value that is passed
 * back to the user when a node gets the chance to be evaluated.
 *
 * @author nicolae caralicea
 *
 * @param <T>
 */
final class GraphNode<T> {

    public T value;
    private List<GraphNode<T>> comingInNodes;
    private List<GraphNode<T>> goingOutNodes;

    /**
     * Adds an incoming node to the current node
     *
     * @param node The incoming node
     */
    public void addComingInNode(GraphNode<T> node) {
        if (comingInNodes == null) {
            comingInNodes = new ArrayList<GraphNode<T>>();
        }
        comingInNodes.add(node);
    }

    /**
     * Adds an outgoing node from the current node
     *
     * @param node The outgoing node
     */
    public void addGoingOutNode(GraphNode<T> node) {
        if (goingOutNodes == null) {
            goingOutNodes = new ArrayList<GraphNode<T>>();
        }
        goingOutNodes.add(node);
    }

    /**
     * Provides all the coming in nodes
     *
     * @return The coming in nodes
     */
    public List<GraphNode<T>> getComingInNodes() {
        return comingInNodes;
    }

    /**
     * Provides all the going out nodes
     *
     * @return The going out nodes
     */
    public List<GraphNode<T>> getGoingOutNodes() {
        return goingOutNodes;
    }
}
