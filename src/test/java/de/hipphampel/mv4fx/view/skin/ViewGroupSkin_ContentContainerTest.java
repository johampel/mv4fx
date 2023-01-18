/*
 * The MIT License
 * Copyright © 2022 Johannes Hampel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hipphampel.mv4fx.view.skin;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.ContentContainer;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkin_ContentContainerTest {

  private ViewGroup viewGroup;
  private ContentContainer contentContainer;

  @Start
  public void start(Stage stage) {
    viewGroup = new ViewGroup();
    viewGroup.setSkin(new ViewGroupSkin(viewGroup));
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(400);
    stage.setWidth(400);
    stage.show();
    contentContainer = ((ViewGroupSkin) viewGroup.getSkin()).getContentContainer();
  }

  @Test
  public void ctor_setsCorrectStyle(FxRobot robot) {
    assertThat(contentContainer.getStyleClass()).contains("content");
  }

  @Test
  public void setView_setsTheContentToTheContentOfTheView(FxRobot robot) {
    Label content = new Label("Content");
    View view = new View();
    view.setContent(content);

    assertThat(contentContainer.getChildren()).isEmpty();

    robot.interact(() -> contentContainer.setView(view));
    assertThat(contentContainer.getChildren()).containsExactly(content);
  }

  @Test
  public void setView_replacesListenerToOldView(FxRobot robot) {
    Label oldContent1 = new Label("OldContent1");
    Label oldContent2 = new Label("OldContent2");
    Label newContant = new Label("NewContent");
    View oldView = new View();
    oldView.setContent(oldContent1);
    View newView = new View();
    newView.setContent(newContant);

    assertThat(contentContainer.getChildren()).isEmpty();

    robot.interact(() -> contentContainer.setView(oldView));
    assertThat(contentContainer.getChildren()).containsExactly(oldContent1);

    robot.interact(() -> contentContainer.setView(newView));
    assertThat(contentContainer.getChildren()).containsExactly(newContant);

    robot.interact(() -> oldView.setContent(oldContent2));
    assertThat(contentContainer.getChildren()).containsExactly(newContant);
  }

  @Test
  public void contentListener_reactsOnContentChange(FxRobot robot) {
    Label oldContent = new Label("OldContent");
    Label newContent = new Label("NewContent");
    View view = new View();
    view.setContent(oldContent);

    robot.interact(() -> contentContainer.setView(view));
    assertThat(contentContainer.getChildren()).containsExactly(oldContent);

    robot.interact(() -> view.setContent(newContent));
    assertThat(contentContainer.getChildren()).containsExactly(newContent);
  }

  @Test
  public void dispose_removesAllListeners(FxRobot robot) {
    Label oldContent = new Label("OldContent");
    Label newContent = new Label("NewContent");
    View view = new View();
    view.setContent(oldContent);

    robot.interact(() -> contentContainer.setView(view));
    assertThat(contentContainer.getChildren()).containsExactly(oldContent);

    contentContainer.dispose();

    robot.interact(() -> view.setContent(newContent));
    assertThat(contentContainer.getChildren()).containsExactly(oldContent);

  }
}
