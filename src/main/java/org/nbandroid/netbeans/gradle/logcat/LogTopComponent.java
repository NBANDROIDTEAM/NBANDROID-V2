/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nbandroid.netbeans.gradle.logcat;

import com.android.ddmlib.Log.LogLevel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import org.nbandroid.netbeans.gradle.logcat.logtable.LogFilter;
import org.nbandroid.netbeans.gradle.logcat.logtable.LogLineRowFilter;
import org.nbandroid.netbeans.gradle.logcat.logtable.LogTableCellRenderer;
import org.nbandroid.netbeans.gradle.logcat.logtable.LogTableManager;
import org.nbandroid.netbeans.gradle.logcat.logtable.LogTableModel;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.TabbedPaneFactory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.nyerel.logcat//Log//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "LogTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.nbandroid.netbeans.gradle.logcat.LogTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LogTopComponentAction",
        preferredID = "LogTopComponent"
)
@NbBundle.Messages({
    "CTL_LogTopComponentAction=ADB Log",
    "CTL_AndroidSdkManagerTopComponent=AndroidSdkManager Window",
    "HINT_AndroidSdkManagerTopComponent=This is a AndroidSdkManager window"
})
public final class LogTopComponent extends TopComponent {

  private static final Logger LOG = Logger.getLogger(LogTopComponent.class.getName());
  private static final String ICON_PATH = "org/nyerel/nbandroid/logcat/androidIcon.png";

  private static final String PREFERRED_ID = "LogTopComponent";
  
  private static final String SERIALIZE_VERSION = "version";
  private static final String SERIALIZE_VERSION_CURRENT = "1.0";
  private static final String SERIALIZE_FILTER_STRING = "filter.string";
  private static final String SERIALIZE_FILTER_LEVEL = "filter.level";
  private static final String SERIALIZE_FILTER_USE_REGEXP = "filter.useRegExp";
  private static final String SERIALIZE_TAB_COUNT = "tab.count";
  private static final String SERIALIZE_TAB = "tab";

  private static LogTopComponent instance;

    /** path to the icon used by the component and its open action */
    private LogReader reader;
    private Timer timer;
    private TimerTask taskUpdateFilterText;
    private boolean starting = false;
    // TODO this is never updated
    private String lastKnownPID = "";
    private JTable selectedTable;

    private List<LogTableManager> tabManagers;
    private final LogLineRowFilter rowFilter = new LogLineRowFilter();
    
    private LogDevicesComboBoxSupport cmbLogDevicesSupport;
    
    
    private final PropertyChangeListener myPropertyChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (LogReader.PROPERTY_CURRENT_DEVICE.equals(evt.getPropertyName())) {
                String newDeviceSerial = reader.getCurrentDevice();
                Collection<LogEvent> events = reader.getLogEventsForDevice(newDeviceSerial);

                if (events != null) {
                    // create a copy of this list to avoid concurrent modification exceptions
                    Collection<LogEvent> eventsToAdd = new ArrayList<>(events.size());
                    eventsToAdd.addAll(events);
                    
                    for(LogTableManager manager : tabManagers) {
                        // clear all messages
                        manager.clearLog();

                        // add all events of the new device
                        manager.addAllEvents(eventsToAdd);
                    }
                }
            }
            
            if (LogReader.PROPERTY_CURRENT_DEVICE_STATE.equals(evt.getPropertyName())) {
                String tooltip = NbBundle.getMessage(this.getClass(), "DeviceStatus." + evt.getNewValue());
                String res;
                
                switch((LogReader.CurrentDeviceState)evt.getNewValue()) {
                case ATTACHED_AND_LOGGING:
                    res = "/org/nbandroid/netbeans/gradle/logcat/resources/device_status_logging.png";
                    break;

                case DETACHED:
                    res = "/org/nbandroid/netbeans/gradle/logcat/resources/device_status_detached.png";
                    break;

                case ATTACHED:
                    res = "/org/nbandroid/netbeans/gradle/logcat/resources/device_status_attached.png";
                    break;

                default:
                    res = "/org/nbandroid/netbeans/gradle/logcat/resources/device_status_unknown.png";
                    break;
                }
                
                lDeviceStatusIcon.setIcon(new ImageIcon(getClass().getResource(res)));
                lDeviceStatusIcon.setToolTipText(tooltip);
            }
        }
    };
    
    
    public LogTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(LogTopComponent.class, "CTL_LogTopComponent"));
        setToolTipText(NbBundle.getMessage(LogTopComponent.class, "HINT_LogTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
//        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        myInit();
//        selectedTable = jTable1;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized LogTopComponent getDefault() {
        if (instance == null) {
            instance = new LogTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the LogTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized LogTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(LogTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof LogTopComponent) {
            return (LogTopComponent) win;
        }
        Logger.getLogger(LogTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
//compo
    @Override
    public void componentOpened() {
      super.componentOpened();
      startReading();
    }

    @Override
    public void componentClosed() {
      if (reader != null) {
        stopReading();
      }
      
      if (cmbLogDevicesSupport != null) {
          cmbLogDevicesSupport.detach();
      }
      
      super.componentClosed();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty(SERIALIZE_VERSION, SERIALIZE_VERSION_CURRENT);

        try{
            // write current filter settings
            p.setProperty(SERIALIZE_FILTER_STRING, txtFilterText.getText());
            p.setProperty(SERIALIZE_FILTER_LEVEL, rowFilter.getLogLevel().toString());
            p.setProperty(SERIALIZE_FILTER_USE_REGEXP, Boolean.toString(rowFilter.isUseRegexp()));

            // export tab filter count
            p.setProperty(SERIALIZE_TAB_COUNT, Integer.toString(tabManagers.size()));

            // export all filters
            for(int i=0; i<tabManagers.size(); i++) {
                String prefix = SERIALIZE_TAB + "." + i;
                LogFilter filter = tabManagers.get(i).getModel().getFilter();

                if (filter != null) {
                    filter.serialize(p, prefix);
                }
            }
        }
        catch(Exception e) {
            LOG.log(Level.INFO, "error on saving LogCat TopComponent", e);
        }
    }

    Object readProperties(java.util.Properties p) {
        LogTopComponent singleton = LogTopComponent.getDefault();
        
        try {
            singleton.readPropertiesImpl(p);
        }
        catch(Exception e) {
            LOG.log(Level.INFO, "error on restore LogCat TopComponent", e);
        }
        
        return singleton;
    }
    
    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty(SERIALIZE_VERSION);
        
        if (SERIALIZE_VERSION_CURRENT.equals(version)) {
            String filterString = p.getProperty(SERIALIZE_FILTER_STRING, "");
            String filterLevel = p.getProperty(SERIALIZE_FILTER_LEVEL, LogLevel.VERBOSE.toString());
            boolean filterByRegExp = Boolean.parseBoolean(p.getProperty(SERIALIZE_FILTER_USE_REGEXP, "false"));
            
            // apply filter string
            if (filterString != null) {
                txtFilterText.setText(filterString);
                rowFilter.setFilterString(filterString);
            }
            cbxFilterType.setSelected(filterByRegExp);
            rowFilter.setUseRegExp(filterByRegExp);
            
            // apply filter level
            if (filterLevel != null) {
                // find matching button to select
                for(Enumeration<AbstractButton> buttons = btGrpLogLevel.getElements(); buttons.hasMoreElements();) {
                    AbstractButton bt = buttons.nextElement();
                    
                    if (filterLevel.equals(bt.getActionCommand())) {
                        bt.setSelected(true);
                        break;
                    }
                }
                
                // convert LogLevel name into enum and configure filter
                try {
                    rowFilter.setLogLevel(LogLevel.valueOf(filterLevel));
                }
                catch(IllegalArgumentException e) {
                }
            }
        }
        
        // try to restore filter tabs
        try {
            int count = Integer.parseInt(p.getProperty(SERIALIZE_TAB_COUNT, "0"));

            for(int i=1; i<count; i++) {
                String prefix = SERIALIZE_TAB + "." + i;
                LogFilter filter = LogFilter.deserialize(p, prefix);
                
                if (filter != null) {
                    addTable(filter);
                }
            }
        }
        catch(IllegalArgumentException e) {
        }
        
        // re-apply filter on current tab
        if (selectedTable != null) {
            ((TableRowSorter)selectedTable.getRowSorter()).sort();
        }
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private void startReading() {
        if (starting) {
            return;
        }
        starting = true;
        if (reader == null || !reader.isReading()) {

            if (reader == null) {
                reader = new LogReader();
                for (LogTableManager manager : tabManagers) {
                    reader.addLogListener(manager);
                }
                
                reader.addPropertyChangeListener(WeakListeners.propertyChange(myPropertyChangeListener, reader));
                
                cmbLogDevicesSupport = new LogDevicesComboBoxSupport(reader);
                cmbLogDevicesSupport.attach(cmbLogDevices);
            }

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    reader.startReading();
                    starting = false;
                }
            }, 1000);
        } else {
            starting = false;
        }
    }

    private void stopReading() {
        if (reader == null) {
            throw new IllegalStateException("Wanted to stop reading, but reader was null");
        }
        if (reader.isReading()) {
            reader.stopReading();
            reader.removeAllLogListeners();
        }

        timer.cancel();
    }

    private void refreshContent() {
        if (taskUpdateFilterText != null) {
            taskUpdateFilterText.cancel();
        }
        
        taskUpdateFilterText = new TimerTask() {
            @Override
            public void run() {
                try {
                    rowFilter.setFilterString(txtFilterText.getText());

                    if (selectedTable != null) {
                        ((AbstractTableModel)selectedTable.getModel()).fireTableDataChanged();
                    }
                }
                catch(Throwable t) {
                    LOG.log(Level.SEVERE, "unexpected exception when updating LogCat text filter", t);
                }
            }
        };
        
        if (timer != null) {
            timer.schedule(taskUpdateFilterText, 250);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        btGrpLogLevel = new javax.swing.ButtonGroup();
        panCenter = new javax.swing.JPanel();
        tabPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        tbLeft = new javax.swing.JToolBar();
        addFilterButton = new javax.swing.JButton();
        removeTabButton = new javax.swing.JButton();
        autoScrollToggleButton = new javax.swing.JToggleButton();
        clearButton = new javax.swing.JButton();
        panFilterTools = new javax.swing.JPanel();
        lDeviceStatusIcon = new javax.swing.JLabel();
        cmbLogDevices = new javax.swing.JComboBox();
        txtFilterText = new javax.swing.JTextField();
        cbxFilterType = new javax.swing.JCheckBox();
        tbLogLevelSelect = new javax.swing.JToolBar();
        btLogLevelVerbose = new javax.swing.JToggleButton();
        btLogLevelDebug = new javax.swing.JToggleButton();
        btLogLevelInfo = new javax.swing.JToggleButton();
        btLogLevelWarn = new javax.swing.JToggleButton();
        btLogLevelError = new javax.swing.JToggleButton();
        btLogLevelAssert = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        panCenter.setLayout(new java.awt.BorderLayout());

        tabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPaneStateChanged(evt);
            }
        });
        tabPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tabPanePropertyChange(evt);
            }
        });
        panCenter.add(tabPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panCenter, gridBagConstraints);

        tbLeft.setBorder(null);
        tbLeft.setFloatable(false);
        tbLeft.setOrientation(javax.swing.SwingConstants.VERTICAL);
        tbLeft.setRollover(true);

        addFilterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/bt_new_tab.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addFilterButton, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.addFilterButton.text")); // NOI18N
        addFilterButton.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.addFilterButton.toolTipText")); // NOI18N
        addFilterButton.setFocusable(false);
        addFilterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addFilterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFilterButtonActionPerformed(evt);
            }
        });
        tbLeft.add(addFilterButton);

        removeTabButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/bt_close_tab.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeTabButton, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.removeTabButton.text")); // NOI18N
        removeTabButton.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.removeTabButton.toolTipText")); // NOI18N
        removeTabButton.setEnabled(false);
        removeTabButton.setFocusable(false);
        removeTabButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeTabButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeTabButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTabButtonActionPerformed(evt);
            }
        });
        tbLeft.add(removeTabButton);

        autoScrollToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/bt_anchor.png"))); // NOI18N
        autoScrollToggleButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoScrollToggleButton, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.autoScrollToggleButton.text")); // NOI18N
        autoScrollToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.autoScrollToggleButton.toolTipText")); // NOI18N
        autoScrollToggleButton.setFocusable(false);
        autoScrollToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        autoScrollToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbLeft.add(autoScrollToggleButton);

        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/bt_clear_log.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.clearButton.text")); // NOI18N
        clearButton.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.clearButton.toolTipText")); // NOI18N
        clearButton.setFocusable(false);
        clearButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        tbLeft.add(clearButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        add(tbLeft, gridBagConstraints);

        panFilterTools.setLayout(new java.awt.GridBagLayout());

        lDeviceStatusIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/device_status_unknown.png"))); // NOI18N
        lDeviceStatusIcon.setLabelFor(cmbLogDevices);
        org.openide.awt.Mnemonics.setLocalizedText(lDeviceStatusIcon, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.lDeviceStatusIcon.text_1")); // NOI18N
        panFilterTools.add(lDeviceStatusIcon, new java.awt.GridBagConstraints());
        panFilterTools.add(cmbLogDevices, new java.awt.GridBagConstraints());

        txtFilterText.setText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.txtFilterText.text")); // NOI18N
        txtFilterText.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.txtFilterText.toolTipText")); // NOI18N
        txtFilterText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFilterTextKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panFilterTools.add(txtFilterText, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbxFilterType, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.cbxFilterType.text")); // NOI18N
        cbxFilterType.setToolTipText(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.cbxFilterType.toolTipText")); // NOI18N
        cbxFilterType.setFocusable(false);
        cbxFilterType.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        cbxFilterType.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cbxFilterType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxFilterTypeActionPerformed(evt);
            }
        });
        panFilterTools.add(cbxFilterType, new java.awt.GridBagConstraints());

        tbLogLevelSelect.setFloatable(false);
        tbLogLevelSelect.setRollover(true);

        btGrpLogLevel.add(btLogLevelVerbose);
        btLogLevelVerbose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_verbose.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelVerbose, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelVerbose.text")); // NOI18N
        btLogLevelVerbose.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelVerbose.actionCommand")); // NOI18N
        btLogLevelVerbose.setFocusable(false);
        btLogLevelVerbose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelVerbose.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelVerbose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelVerbose);

        btGrpLogLevel.add(btLogLevelDebug);
        btLogLevelDebug.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_debug.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelDebug, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelDebug.text")); // NOI18N
        btLogLevelDebug.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelDebug.actionCommand")); // NOI18N
        btLogLevelDebug.setFocusable(false);
        btLogLevelDebug.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelDebug.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelDebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelDebug);

        btGrpLogLevel.add(btLogLevelInfo);
        btLogLevelInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelInfo, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelInfo.text")); // NOI18N
        btLogLevelInfo.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelInfo.actionCommand")); // NOI18N
        btLogLevelInfo.setFocusable(false);
        btLogLevelInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelInfo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelInfo);

        btGrpLogLevel.add(btLogLevelWarn);
        btLogLevelWarn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_warn.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelWarn, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelWarn.text")); // NOI18N
        btLogLevelWarn.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelWarn.actionCommand")); // NOI18N
        btLogLevelWarn.setFocusable(false);
        btLogLevelWarn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelWarn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelWarn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelWarn);

        btGrpLogLevel.add(btLogLevelError);
        btLogLevelError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelError, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelError.text")); // NOI18N
        btLogLevelError.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelError.actionCommand")); // NOI18N
        btLogLevelError.setFocusable(false);
        btLogLevelError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelError);

        btGrpLogLevel.add(btLogLevelAssert);
        btLogLevelAssert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/nbandroid/netbeans/gradle/logcat/resources/log_level_assert.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btLogLevelAssert, org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelAssert.text")); // NOI18N
        btLogLevelAssert.setActionCommand(org.openide.util.NbBundle.getMessage(LogTopComponent.class, "LogTopComponent.btLogLevelAssert.actionCommand")); // NOI18N
        btLogLevelAssert.setFocusable(false);
        btLogLevelAssert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLogLevelAssert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLogLevelAssert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLogLevelActionPerformed(evt);
            }
        });
        tbLogLevelSelect.add(btLogLevelAssert);

        panFilterTools.add(tbLogLevelSelect, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panFilterTools, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFilterButtonActionPerformed

        LogFilter filter = LogFilterDialog.showDialog();
        if(filter != null) {
            addTable(filter);
        }
    }//GEN-LAST:event_addFilterButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed

        LogTableManager manager = tabManagers.get(tabPane.getSelectedIndex());
        if(manager != null) {
            manager.clearLog();
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    private void tabPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabPaneStateChanged

        JScrollPane sPane = (JScrollPane) tabPane.getSelectedComponent();
        if(sPane != null) {

            JViewport view = (JViewport) sPane.getComponent(0);
            selectedTable  = (JTable) view.getView();

            // re-apply the filter to the current tab
            if (selectedTable.getRowSorter() != null) {
                ((TableRowSorter)selectedTable.getRowSorter()).sort();
            }
            
            if(tabManagers != null) {

               // Remove all toggle button listeners
                for (ChangeListener l : autoScrollToggleButton.getChangeListeners()) {

                    autoScrollToggleButton.removeChangeListener(l);
                }

                LogTableManager manager = tabManagers.get(tabPane.getSelectedIndex());

               // Add current listener
                autoScrollToggleButton.addChangeListener(manager);

               // Refresh buttons
                autoScrollToggleButton.setSelected(manager.isAutoFollowScroll());
            }            
        }
    }//GEN-LAST:event_tabPaneStateChanged

    private void tabPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tabPanePropertyChange
        if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName()) && evt.getSource() == tabPane) {
            // find the index of the clicked component
            for(int index=tabPane.getTabCount(); --index>=0;) {
                // if it's the same component as in the property value, we know which tab should be closed
                if (tabPane.getComponentAt(index) == evt.getNewValue()) {
                    LogTableManager manager = tabManagers.get(index);
                    LogFilter filter = manager.getModel().getFilter();

                    // ask the user... just to be sure
                    int result = JOptionPane.showConfirmDialog(
                                                this,
                                                NbBundle.getMessage(this.getClass(), "LogFilterDialog.ConfirmRemove.message", filter.getName(), filter.getDescription()),
                                                NbBundle.getMessage(this.getClass(), "LogFilterDialog.ConfirmRemove.title", filter.getName()),
                                                JOptionPane.YES_NO_OPTION
                    );

                    if (result == JOptionPane.YES_OPTION) {
                        tabManagers.remove(index);
                        reader.removeLogListener(manager);
                        tabPane.remove(index);
                    }
                }
            }
        }
    }//GEN-LAST:event_tabPanePropertyChange

    private void removeTabButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTabButtonActionPerformed

        LogTableManager manager = tabManagers.remove(tabPane.getSelectedIndex());
        reader.removeLogListener(manager);

        tabPane.remove(tabPane.getSelectedIndex());
    }//GEN-LAST:event_removeTabButtonActionPerformed

	private void btLogLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLogLevelActionPerformed
        LogLevel level = LogLevel.valueOf(evt.getActionCommand());
        if (level != null) {
            rowFilter.setLogLevel(level);
            refreshContent();
        }
        
	}//GEN-LAST:event_btLogLevelActionPerformed

	private void txtFilterTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFilterTextKeyTyped
        rowFilter.setFilterString(txtFilterText.getText());
        refreshContent();
	}//GEN-LAST:event_txtFilterTextKeyTyped

    private void cbxFilterTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxFilterTypeActionPerformed
        rowFilter.setUseRegExp(cbxFilterType.isSelected());
        refreshContent();
    }//GEN-LAST:event_cbxFilterTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFilterButton;
    private javax.swing.JToggleButton autoScrollToggleButton;
    private javax.swing.ButtonGroup btGrpLogLevel;
    private javax.swing.JToggleButton btLogLevelAssert;
    private javax.swing.JToggleButton btLogLevelDebug;
    private javax.swing.JToggleButton btLogLevelError;
    private javax.swing.JToggleButton btLogLevelInfo;
    private javax.swing.JToggleButton btLogLevelVerbose;
    private javax.swing.JToggleButton btLogLevelWarn;
    private javax.swing.JCheckBox cbxFilterType;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox cmbLogDevices;
    private javax.swing.JLabel lDeviceStatusIcon;
    private javax.swing.JPanel panCenter;
    private javax.swing.JPanel panFilterTools;
    private javax.swing.JButton removeTabButton;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JToolBar tbLeft;
    private javax.swing.JToolBar tbLogLevelSelect;
    private javax.swing.JTextField txtFilterText;
    // End of variables declaration//GEN-END:variables

    private void myInit() {
        tabManagers = new ArrayList<>();
        
        // create the first tab
        addTable(LogFilter.createDefaultTab());
        
        // activate the selected button
        for(Enumeration<AbstractButton> buttons = btGrpLogLevel.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            
            if (button.getActionCommand().equals(rowFilter.getLogLevel().toString())) {
                button.setSelected(true);
            }
        }
    }


    private void addTable(LogFilter filter) {
        LogTableModel model = new LogTableModel(filter);
        JTable table = new JTable(model);

        // Prepare table for listening
        LogTableManager manager = prepareTable(table);
        
        if (reader != null) {
            reader.addLogListener(manager);
        }

        JScrollPane scroll = new JScrollPane(table);

        // the first tab should not be closeable
        if (tabPane.getTabCount() == 0) {
            scroll.putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, Boolean.TRUE);
        }

        String tooltip = filter.getDescription();
        tabPane.addTab(filter.getName(), null, scroll, tooltip);
        
        tabPane.validate();
    }
    
    
    private LogTableManager prepareTable(final JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setShowHorizontalLines(false);

        LogTableManager manager = new LogTableManager(table);
        tabManagers.add(manager);

        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {

            TableColumn column = columns.nextElement();
            column.setCellRenderer(new LogTableCellRenderer());
        }        

        TableRowSorter<LogTableModel> rowsorter = new TableRowSorter<LogTableModel>((LogTableModel)table.getModel()) {
              @Override
              public void rowsInserted(int firstRow, int endRow) {
                try {
                  super.rowsInserted(firstRow, endRow);
                } catch (NullPointerException | IndexOutOfBoundsException ex) {
                  LOG.log(Level.INFO, 
                      "Ignoring exception caused by http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6582564", ex);
                }
              }

              @Override
              public int convertRowIndexToModel(int index) {
                try {
                  return super.convertRowIndexToModel(index);
                } catch (NullPointerException ex) {
                  LOG.log(Level.INFO, 
                      "Ignoring exception reported in http://netbeans.org/bugzilla/show_bug.cgi?id=197503", ex);
                  return 0;
                }
                catch(ArrayIndexOutOfBoundsException e) {
                    // may happen when RowSorter.sort was called 2 times
                    return index;
                }
              }
        };
        
        rowsorter.setRowFilter(rowFilter);
        
        table.setRowSorter(rowsorter);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                try {
                    LogTableModel model = (LogTableModel)table.getModel();
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();

                    // if possible, try to translate the visual row number to the model's row number
                    if (table.getRowSorter() != null) {
                        row = table.getRowSorter().convertRowIndexToModel(row);
                    }

                    // check, if the computed index is in the model
                    if (row < 0 || row >= model.getRowCount()) {
                        return;
                    }

                    LogEvent event = model.getValueAt(row);
                    StackTraceElement ste = event.getStackTraceElement();

                    if (col == LogTableModel.COL_MESSAGE && ste != null) {
                        FileObject fo = GlobalPathRegistry.getDefault().findResource(ste.getFileName());

                        if (fo != null) {
                            DataObject data = DataObject.find(fo);
                            EditorCookie ec = data.getLookup().lookup(EditorCookie.class);
                            LineCookie lc = data.getLookup().lookup(LineCookie.class);

                            if ((ec != null) && (lc != null)) {
                                Document doc = ec.openDocument();

                                if (doc != null) {
                                    int line = ste.getLineNumber();

                                    if (line < 1) {
                                        line = 1;
                                    }

                                    // XXX .size() call is super-slow for large files, see issue
                                    // #126531. So we fallback to catching IOOBE
                //                    int nOfLines = lines.getLines().size();
                //                    if (line > nOfLines) {
                //                        line = nOfLines;
                //                    }
                                    try {
                                        Line.Set lines = lc.getLineSet();
                                        Line l = lines.getCurrent(line - 1);
                                        if (l != null) {
                                            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                                            return;
                                        }
                                    } catch (IndexOutOfBoundsException ioobe) {
                                        // OK, since .size() cannot be used, see above
                                    }
                                }
                            }

                            OpenCookie oc = data.getLookup().lookup(OpenCookie.class);
                            if (oc != null) {
                                oc.open();
                            }
                        }
                    }
                }
                catch(IOException e) {
                    
                }
            }
        });

        return manager;
    }
}
