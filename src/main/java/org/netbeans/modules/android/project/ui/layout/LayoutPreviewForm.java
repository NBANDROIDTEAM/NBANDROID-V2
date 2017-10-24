package org.netbeans.modules.android.project.ui.layout;

import com.android.ide.common.rendering.api.Result;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author radim
 */
public class LayoutPreviewForm extends JPanel {

  private PreviewModel model;
  private PreviewController controller;
  
  private final DeviceConfiguratorPanel layoutDeviceSelectorPanel;
  private final JLabel lblLayoutName;
  private final LayoutView imageView;

  private static LayoutView createImageView() {
    return new ExtLayoutView();
  }
  
  public LayoutPreviewForm() {
    setLayout(new GridBagLayout());
    layoutDeviceSelectorPanel = new DeviceConfiguratorPanel();
    GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.5, 0, 
        GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
        new Insets(6, 6, 6, 6), 0, 0);
    add(layoutDeviceSelectorPanel, gbc);
    lblLayoutName = new JLabel(
        NbBundle.getMessage(LayoutPreviewForm.class, "PreviewLayoutTopComponent.lblLayoutName.text"));
    gbc = new GridBagConstraints(0, 1, 1, 1, 0.5, 0, 
        GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
        new Insets(0, 6, 6, 6), 0, 0);
    add(lblLayoutName, gbc);
    imageView = createImageView();
    gbc = new GridBagConstraints(0, 2, 1, 1, 0.5, 1, 
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
        new Insets(0, 6, 6, 6), 0, 0);
    add(imageView.getComponent(), gbc);
    revalidate();
  }
  
  void attachToController(PreviewModel model, PreviewController controller) {
    this.model = model;
    this.controller = controller;

    this.model.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() != null) {
          switch (e.getPropertyName()) {
          case PreviewModel.PROP_FILEOBJECT:
            FileObject fo = LayoutPreviewForm.this.model.getFileObject();
            lblLayoutName.setText(
                fo == null ?
                    NbBundle.getMessage(LayoutPreviewForm.class, "PreviewLayoutTopComponent.lblLayoutName.text") :
                    fo.getNameExt());
            break;
          case PreviewModel.PROP_IMAGE:
            imageView.setImage(LayoutPreviewForm.this.model.getImage());
            break;
          case PreviewModel.PROP_RESULT:
            imageView.setResult(LayoutPreviewForm.this.model.getResult());
            break;
          }
        }
      }
    });
    
    layoutDeviceSelectorPanel.attachToController(model, controller);
  }
}
