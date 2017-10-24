package org.nbandroid.netbeans.gradle.ui;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.Image;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.api.AndroidConstants;
import org.netbeans.modules.android.project.api.ui.AndroidPackagesNode;
import org.netbeans.modules.android.project.api.ui.UiUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

public class InstrumentTestNode extends AbstractNode {
  private final Project p;

  public InstrumentTestNode(String name, Project p) {
    super(new InstrumentTestChildren(p));
    setName(name);
    setDisplayName(name);
    this.p = p;
  }

  @Override
  public Image getOpenedIcon(int type) {
    Image img = UiUtils.getTreeFolderIcon(true);
    return ImageUtilities.mergeImages(img, UiUtils.TEST_BADGE, 6, 6);
  }

  @Override
  public Image getIcon(int type) {
    Image img = UiUtils.getTreeFolderIcon(false);
    return ImageUtilities.mergeImages(img, UiUtils.TEST_BADGE, 6, 6);
  }

  private static enum Key {
    SOURCES,
    RESOURCES,
    RES,
    GENERATED,
    DEPENDENCIES
  }
  
  private static class InstrumentTestChildren extends Children.Keys<Key> {
    private final Project p;
    

    public InstrumentTestChildren(Project p) {
      this.p = p;
      setKeys(Key.values());
    }

    @Override
    protected Node[] createNodes(Key key) {
      Node[] result = null;
      Sources sources = ProjectUtils.getSources(p);
      switch (key) {
        case SOURCES:
          result = Lists.newArrayList(
              Iterables.transform(
                  Arrays.asList(sources.getSourceGroups(AndroidConstants.SOURCES_TYPE_INSTRUMENT_TEST_JAVA)), 
                  new Function<SourceGroup, Node>() {
                    @Override
                    public Node apply(SourceGroup f) {
                      return new AndroidPackagesNode(f, p);
                    }
                  })).toArray(new Node[0]);
          break;
        case GENERATED:
          result = new Node[] {
            new GeneratedSourcesNode("Generated Instrument Test Sources", p, Lists.newArrayList(
                AndroidConstants.SOURCES_TYPE_INSTRUMENT_TEST_GENERATED_JAVA))
          };
          break;
        case RESOURCES:
          // result = new Node[]{AndroidNodes.createProjectNode(project)};
          break;
        case RES:
          break;
        case DEPENDENCIES:
          result = new Node[] {
            DependenciesNode.createTestDependenciesNode("Instrument Test Dependencies", p)
          };
          break;
      }
      if (result == null) {
        result = new Node[0];
      }
      return result;
    }
  }
  
}
