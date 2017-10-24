package org.netbeans.modules.android.project.ui.layout;

import com.android.ide.common.rendering.api.Result;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import static javax.swing.JLayeredPane.PALETTE_LAYER;
import javax.swing.JPanel;

/**
 *
 * @author radim
 */
class ExtLayoutView implements LayoutView {

  final ImagePanel imagePanel = new ImagePanel();
  final MyImagePanelWrapper imagePanelWrapper = new MyImagePanelWrapper(imagePanel);
  public ExtLayoutView() {
  }

  @Override
  public JComponent getComponent() {
    return imagePanelWrapper;
  }

  @Override
  public void setImage(BufferedImage image) {
    imagePanel.setImage(image);
  }

  @Override
  public void setResult(Result result) {
    imagePanelWrapper.setResult(result);
  }
  
  private static class MyImagePanelWrapper extends JLayeredPane {

    private final ImagePanel imagePanel;
    private final JPanel infoPanel;
    private final JLabel infoLabel;

    public MyImagePanelWrapper(ImagePanel imagePanel) {
      this.imagePanel = imagePanel;
      add(imagePanel);
      infoPanel = new JPanel();
      infoPanel.setLayout(new BorderLayout());
      infoLabel = new JLabel("Loading...");
      infoPanel.add(infoLabel, BorderLayout.CENTER);
      add(infoPanel, PALETTE_LAYER);

      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          MyImagePanelWrapper.this.imagePanel.updateSize();
          infoPanel.setBounds(0, 0, getWidth(), getHeight());
        }
      });
    }

    private void centerComponents() {
      Rectangle bounds = getBounds();
      if (imagePanel.getWidth() > 0) {
        Point point = imagePanel.getLocation();
        point.x = (bounds.width - imagePanel.getWidth()) / 2;
        imagePanel.setLocation(point);
      }
    }

    @Override
    public void invalidate() {
      centerComponents();
      super.invalidate();
    }

    public void setResult(Result result) {
      if (result != null && result.isSuccess()) {
        infoPanel.setVisible(false);
      } else {
        infoPanel.setVisible(true);
        String msg = result == null ? 
            "Loading ..." :
            result.getErrorMessage();
        infoLabel.setText(msg);
      }
    }
  }
}
