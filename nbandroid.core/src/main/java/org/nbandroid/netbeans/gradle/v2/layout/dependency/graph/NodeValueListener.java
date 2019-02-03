/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbandroid.netbeans.gradle.v2.layout.dependency.graph;

/**
 * The main mechanism used for notifying the outside of the fact that a node
 * just got its evaluation
 *
 * @author nicolae caralicea
 *
 * @param <T>
 */
public interface NodeValueListener<T> {

    /**
     *
     * The callback method used to notify the fact that a node that has assigned
     * the nodeValue value just got its evaluation
     *
     * @param nodeValue The user set value of the node that just got the
     * evaluation
     */
    void evaluating(T nodeValue);
}
