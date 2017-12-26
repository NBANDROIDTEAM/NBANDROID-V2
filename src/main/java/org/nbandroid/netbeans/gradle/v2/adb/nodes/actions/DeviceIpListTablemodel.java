/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nbandroid.netbeans.gradle.v2.adb.nodes.actions;

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.nbandroid.netbeans.gradle.v2.adb.AdbTools;

/**
 *
 * @author arsi
 */
public class DeviceIpListTablemodel implements TableModel {

    private final List<AdbTools.IpRecord> ips;

    public DeviceIpListTablemodel(List<AdbTools.IpRecord> deviceIps) {
        this.ips = deviceIps;
    }

    @Override
    public int getRowCount() {
        return ips.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Adapter name";
        } else {
            return "IP address";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return ips.get(rowIndex).getName();
        } else {
            return ips.get(rowIndex).getIp();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

}
