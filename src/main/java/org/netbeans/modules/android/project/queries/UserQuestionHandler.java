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

package org.netbeans.modules.android.project.queries;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Deals with {@link UserQuestionException}s.
 * @see "#46089"
 * @author Jesse Glick
 */
final class UserQuestionHandler {

    private UserQuestionHandler() {}

    /**
     * Handle a user question exception later (in the event thread).
     * Displays a dialog and invokes the appropriate method on the callback.
     * The callback will be notified in the event thread.
     * Use when catching {@link UserQuestionException} during {@link FileObject#lock}.
     */
    public static void handle(final UserQuestionException e, final Callback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(
                    e.getLocalizedMessage(),
                    NbBundle.getMessage(UserQuestionHandler.class, "TITLE_CannotWriteFile"),
                    NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                    try {
                        e.confirmed();
                        callback.accepted();
                    } catch (IOException x) {
                        callback.error(x);
                    }
                } else {
                    callback.denied();
                }
            }
        });
    }

    /**
     * Intended behavior.
     */
    public interface Callback {

        /**
         * Called later if the user accepted the question.
         */
        void accepted();

        /**
         * Called later if the user denied the question.
         */
        void denied();

        /**
         * Called later if the user accepted the question but there was in fact a problem.
         */
        void error(IOException e);

    }

}
