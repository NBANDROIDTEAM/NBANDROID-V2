/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.adb.nodes.actions;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.nbandroid.netbeans.gradle.v2.adb.nodes.DevicesNode;
import org.nbandroid.netbeans.gradle.v2.sdk.AndroidSdkProvider;
import org.netbeans.modules.dlight.terminal.action.TerminalSupportImpl;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 *
 * @author arsi
 */
@ActionID(
        category = "ADB/MobileDevice",
        id = "org.nbandroid.netbeans.gradle.v2.adb.nodes.actions.AndroidShellAdbAction"
)
@ActionRegistration(
        displayName = "", lazy = false
)
@ActionReferences({
    @ActionReference(path = "Android/ADB/MobileDevice", position = 9970),})
public class AndroidShellAdbAction extends NodeAction {

    public static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N
    private static final ClassLoader CLASS_LOADER = Lookup.getDefault().lookup(ClassLoader.class);

    @Override
    protected void performAction(Node[] activatedNodes) {

        Runnable runnable = new Runnable() {
            public void run() {
                for (Node activatedNode : activatedNodes) {
                    DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class);
                    final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
                    instance.open();
                    instance.requestActive();
                    final IOContainer ioContainer = instance.getIOContainer();
                    final IOProvider term = IOProvider.get("Terminal"); // NOI18N
                    if (term != null) {
                        final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
                        if (env != null) {
                            openTerminalImpl(ioContainer, env, null, false, false, 0, holder);
                            Component[] components = instance.getComponents();
                            System.out.println(".run()");
                        }
                    }
                }
            }
        };
        WindowManager.getDefault().invokeWhenUIReady(runnable);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            DevicesNode.MobileDeviceHolder holder = activatedNode.getLookup().lookup(DevicesNode.MobileDeviceHolder.class
            );
            if (holder == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Shell";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    /**
     * Hack of TerminalSupportImpl.openTerminalImpl to open adb
     *
     * @param ioContainer
     * @param env
     * @param dir
     * @param silentMode
     * @param pwdFlag
     * @param termId
     * @param holder
     */
    public static void openTerminalImpl(
            final IOContainer ioContainer,
            final ExecutionEnvironment env,
            final String dir,
            final boolean silentMode,
            final boolean pwdFlag,
            final long termId,
            final DevicesNode.MobileDeviceHolder holder) {
        final IOProvider ioProvider = IOProvider.get("Terminal"); // NOI18N
        if (ioProvider != null) {
            final AtomicReference<InputOutput> ioRef = new AtomicReference<InputOutput>();
            InputOutput io = ioProvider.getIO(holder.getSerialNumber(), null, ioContainer);
            ioRef.set(io);
            final AtomicBoolean destroyed = new AtomicBoolean(false);

            final Runnable runnable = new Runnable() {
                private final Runnable delegate = new Runnable() {
                    @Override
                    public void run() {
                        if (SwingUtilities.isEventDispatchThread()) {
                            ioContainer.requestActive();
                        } else {
                            doWork();
                        }
                    }
                };

                @Override
                public void run() {
                    delegate.run();
                }

                private final HyperlinkAdapter retryLink = new HyperlinkAdapter() {
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        RP.post(delegate);
                    }
                };

                private void doWork() {
                    boolean verbose = env.isRemote(); // can use silentMode instead
                    OutputWriter out = ioRef.get().getOut();

                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        try {
                            if (verbose) {
                                out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ConnectingTo", env.getDisplayName()));
                            }
                            ConnectionManager.getInstance().connectTo(env);
                        } catch (IOException ex) {
                            if (!destroyed.get()) {
                                if (verbose) {
                                    try {
                                        out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ConnectionFailed"));
                                        out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Retry"), retryLink);
                                    } catch (IOException ignored) {
                                    }
                                }
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalSupportImpl.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            }
                            return;
                        } catch (ConnectionManager.CancellationException ex) {
                            if (verbose) {
                                try {
                                    out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Canceled"));
                                    out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Retry"), retryLink);
                                } catch (IOException ignored) {
                                }
                            }
                            return;
                        }
                    }

                    final HostInfo hostInfo;
                    try {
                        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                            return;
                        }

                        try {
                            if (dir != null && !HostInfoUtils.directoryExists(env, dir)) {
                                out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_DirNotExist", dir, env.getDisplayName()));
                                return;
                            }
                        } catch (ConnectException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        hostInfo = HostInfoUtils.getHostInfo(env);
                        boolean isSupported = PtySupport.isSupportedFor(env);
                        if (!isSupported && !(hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS)) {
                            //it might work under windows ;)
                            if (!silentMode) {
                                String message;

                                if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                                    message = NbBundle.getMessage(TerminalSupportImpl.class, "LocalTerminalNotSupported.error.nocygwin"); // NOI18N
                                } else {
                                    message = NbBundle.getMessage(TerminalSupportImpl.class, "LocalTerminalNotSupported.error"); // NOI18N
                                }

                                NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(nd);
                            }
                            return;
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    } catch (ConnectionManager.CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    }
                    try {
                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                        npb.getEnvironment().put("LD_LIBRARY_PATH", "");// NOI18N
                        npb.getEnvironment().put("DYLD_LIBRARY_PATH", "");// NOI18N
                        npb.addNativeProcessListener(new NativeProcessListener(ioRef.get(), destroyed));
                        if (dir != null) {
                            npb.setWorkingDirectory(dir);
                        }
                        //override to call adb executable
                        npb.setExecutable(AndroidSdkProvider.getAdbPath());
                        npb.setArguments("-s", holder.getMasterDevice().getSerialNumber(), "shell");
                        NativeExecutionDescriptor descr;
                        descr = new NativeExecutionDescriptor().controllable(true).frontWindow(true).inputVisible(true).inputOutput(ioRef.get());
                        descr.postExecution(new Runnable() {
                            @Override
                            public void run() {
                                ioRef.get().closeInputOutput();
                            }
                        });
                        NativeExecutionService es = NativeExecutionService.newService(npb, descr, "Terminal Emulator"); // NOI18N
                        Future<Integer> result = es.run();
                        // ask terminal to become active
                        SwingUtilities.invokeLater(this);

                        try {
                            Integer rc = result.get(10, TimeUnit.SECONDS);
                            if (rc != 0) {
                                Logger.getLogger(TerminalSupportImpl.class.getName())
                                        .log(Level.INFO, "{0}{1}", new Object[]{NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ReturnCode"), rc});
                            }
                        } catch (java.util.concurrent.TimeoutException ex) {
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (ExecutionException ex) {
                            if (!destroyed.get()) {
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalSupportImpl.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            }
                        }
                    } catch (java.util.concurrent.CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                        reportInIO(ioRef.get(), ex);
                    }
                }

                private void reportInIO(InputOutput io, Exception ex) {
                    if (io != null && ex != null) {
                        io.getErr().print(ex.getLocalizedMessage());
                    }
                }
            };
            RP.post(runnable);
        }
    }

    private final static class NativeProcessListener implements ChangeListener, PropertyChangeListener {

        private final AtomicReference<NativeProcess> processRef;
        private final AtomicBoolean destroyed;

        public NativeProcessListener(InputOutput io, AtomicBoolean destroyed) {
            assert destroyed != null;
            this.destroyed = destroyed;
            this.processRef = new AtomicReference<NativeProcess>();
            try {
                Class<?> iONotifier = CLASS_LOADER.loadClass("org.netbeans.modules.terminal.api.IONotifier");
                Method m = iONotifier.getDeclaredMethod("addPropertyChangeListener", InputOutput.class, PropertyChangeListener.class);
                m.invoke(null, io, WeakListeners.propertyChange(NativeProcessListener.this, io));
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            NativeProcess process = processRef.get();
            if (process == null && e.getSource() instanceof NativeProcess) {
                processRef.compareAndSet(null, (NativeProcess) e.getSource());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("IOVisibility.PROP_VISIBILITY".equals(evt.getPropertyName()) && Boolean.FALSE.equals(evt.getNewValue())) {
                if (destroyed.compareAndSet(false, true)) {
                    final NativeProcess proc = processRef.get();
                    if (proc != null) {
                        RP.submit(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    proc.destroy();
                                } catch (Throwable th) {
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private static class HyperlinkAdapter implements OutputListener {

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }

}
