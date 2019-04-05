/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.project.properties.ui;

import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.android.project.tasks.UserTask;
import org.netbeans.modules.android.project.tasks.UserTasksConfiguration;

/**
 *
 * @author arsi
 */
public class GradleUserTasksPanel extends javax.swing.JPanel implements ListSelectionListener, DocumentListener {

    private final List<UserTask> userTasks;
    private final UserTasksConfiguration userTasksConfiguration;
    private final TaskModel taskModel;

    /**
     * Creates new form GradleUserTasks
     */
    public GradleUserTasksPanel(UserTasksConfiguration userTasksConfiguration) {
        initComponents();
        userTasks = userTasksConfiguration.getUserTasks();
        this.userTasksConfiguration = userTasksConfiguration;
        taskModel = new TaskModel();
        taskList.setModel(taskModel);
        taskList.addListSelectionListener(this);
        if (!userTasks.isEmpty()) {
            taskList.setSelectedIndex(0);
        }
        taskName.getDocument().addDocumentListener(this);
    }

    public void store() {
        userTasksConfiguration.store();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int firstIndex = taskList.getSelectedIndex();
            if (firstIndex > -1) {
                remove.setEnabled(true);
                updateTask.setEnabled(true);
                UserTask userTask = userTasks.get(firstIndex);
                if (userTask != null) {
                    taskName.setText(userTask.getTaskName());
                    String tmp = "";
                    for (String task : userTask.getTasks()) {
                        tmp += task + "\n";
                    }
                    tasks.setText(tmp);
                    tmp = "";
                    for (String gradle : userTask.getGradleArguments()) {
                        tmp += gradle + "\n";
                    }
                    gradleArguments.setText(tmp);
                    tmp = "";
                    for (String jvm : userTask.getJvmArguments()) {
                        tmp += jvm + "\n";
                    }
                    jvmArguments.setText(tmp);
                }
            } else {
                remove.setEnabled(false);
                updateTask.setEnabled(false);
                taskName.setText("");
                gradleArguments.setText("");
                jvmArguments.setText("");
                tasks.setText("");
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateButtons();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateButtons();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateButtons();
    }

    private void updateButtons() {
        add.setEnabled(!taskName.getText().isEmpty());
        updateTask.setEnabled(!taskName.getText().isEmpty() && taskList.getSelectedValue() != null);
    }

    private class TaskModel extends AbstractListModel<UserTask> {

        @Override
        public int getSize() {
            return userTasks.size();
        }

        @Override
        public UserTask getElementAt(int index) {
            return userTasks.get(index);
        }

        @Override
        public void fireIntervalAdded(Object source, int index0, int index1) {
            super.fireIntervalAdded(source, index0, index1); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void fireIntervalRemoved(Object source, int index0, int index1) {
            super.fireIntervalRemoved(source, index0, index1); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1); //To change body of generated methods, choose Tools | Templates.
        }


    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskList = new javax.swing.JList<>();
        add = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        gradleArguments = new javax.swing.JTextPane();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jvmArguments = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        tasks = new javax.swing.JTextPane();
        jLabel5 = new javax.swing.JLabel();
        taskName = new javax.swing.JTextField();
        updateTask = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.jLabel1.text")); // NOI18N

        taskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(taskList);

        org.openide.awt.Mnemonics.setLocalizedText(add, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.add.text")); // NOI18N
        add.setEnabled(false);
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(remove, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.remove.text")); // NOI18N
        remove.setEnabled(false);
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.jLabel3.text")); // NOI18N

        jScrollPane2.setViewportView(gradleArguments);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.jLabel4.text")); // NOI18N

        jScrollPane3.setViewportView(jvmArguments);

        jScrollPane4.setViewportView(tasks);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.jLabel5.text")); // NOI18N

        taskName.setText(org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.taskName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(updateTask, org.openide.util.NbBundle.getMessage(GradleUserTasksPanel.class, "GradleUserTasksPanel.updateTask.text")); // NOI18N
        updateTask.setEnabled(false);
        updateTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateTaskActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(0, 237, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(taskName))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(add, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(remove, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(updateTask))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {add, remove, updateTask});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(add)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateTask)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(remove)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(taskName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        // TODO add your handling code here:
        UserTask selectedValue = taskList.getSelectedValue();
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedValue != null) {
            userTasks.remove(selectedValue);
            taskModel.fireIntervalRemoved(taskModel, selectedIndex, selectedIndex);
        }
    }//GEN-LAST:event_removeActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        // TODO add your handling code here:
        UserTask tmp = new UserTask();
        userTasks.add(tmp);
        refreshTask(tmp);
        taskModel.fireIntervalAdded(taskModel, userTasks.size() - 1, userTasks.size() - 1);
        taskList.setSelectedIndex(userTasks.size() - 1);
    }//GEN-LAST:event_addActionPerformed

    private void refreshTask(UserTask tmp) {
        String text = tasks.getText();
        String[] lines = text.split(System.getProperty("line.separator"));
        tmp.setTaskName(taskName.getText());
        tmp.getTasks().clear();
        tmp.getTasks().addAll(Arrays.asList(lines));
        text = gradleArguments.getText();
        lines = text.split(System.getProperty("line.separator"));
        tmp.getGradleArguments().clear();
        tmp.getGradleArguments().addAll(Arrays.asList(lines));
        text = jvmArguments.getText();
        lines = text.split(System.getProperty("line.separator"));
        tmp.getJvmArguments().clear();
        tmp.getJvmArguments().addAll(Arrays.asList(lines));
    }

    private void updateTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateTaskActionPerformed
        // TODO add your handling code here:
        UserTask selectedValue = taskList.getSelectedValue();
        int selectedIndex = taskList.getSelectedIndex();
        if(selectedValue!=null){
            refreshTask(selectedValue);
            taskModel.fireContentsChanged(taskModel, selectedIndex, selectedIndex);
        }
    }//GEN-LAST:event_updateTaskActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JTextPane gradleArguments;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextPane jvmArguments;
    private javax.swing.JButton remove;
    private javax.swing.JList<UserTask> taskList;
    private javax.swing.JTextField taskName;
    private javax.swing.JTextPane tasks;
    private javax.swing.JButton updateTask;
    // End of variables declaration//GEN-END:variables

}
