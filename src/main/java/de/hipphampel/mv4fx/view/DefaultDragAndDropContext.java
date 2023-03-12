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
package de.hipphampel.mv4fx.view;

import de.hipphampel.mv4fx.utils.Utils;
import de.hipphampel.mv4fx.view.DropTarget.MoveToGroup;
import de.hipphampel.mv4fx.view.DropTarget.None;
import de.hipphampel.mv4fx.view.DropTarget.SplitViewGroup;
import de.hipphampel.mv4fx.view.ViewOrGroup.DropTargetType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 * Default implementation of the {@link DragAndDropContext}.
 * <p>
 * This default implementation can also be used to customize the standard behaviour by deriving from
 * this class and later on set the new implementation via
 * {@link DragAndDropContext#setInstance(DragAndDropContext) setInstance}.
 *
 * @see DragAndDropContext
 */
public class DefaultDragAndDropContext implements DragAndDropContext {

  private static final Cursor NO_DND_CURSOR = new ImageCursor(
      new Image(
          DefaultDragAndDropContext.class.getResource("/de/hipphampel/mv4fx/no_dnd_cursor.png")
              .toString()));
  private static final Cursor DND_CURSOR = new ImageCursor(
      new Image(DefaultDragAndDropContext.class.getResource("/de/hipphampel/mv4fx/dnd_cursor.png")
          .toString()));

  private final EventHandler<KeyEvent> keyHandler;
  private ViewOrGroup dragSource;
  private DropTarget dropTarget;
  private Cursor oldCursor;

  /**
   * Constructor.
   */
  public DefaultDragAndDropContext() {
    this.keyHandler = evt -> {
      if (evt.getCode() == KeyCode.ESCAPE) {
        cancel();
      }
    };
  }

  @Override
  public ViewOrGroup getDragSource() {
    return dragSource;
  }

  @Override
  public DropTarget getDropTarget() {
    return dropTarget;
  }

  @Override
  public void start(ViewOrGroup dragSource, double screenX, double screenY) {
    if (this.dragSource != null) {
      return;
    }
    this.dragSource = dragSource;
    if (this.dragSource != null && this.dragSource.asControl() != null) {
      Control control = this.dragSource.asControl();
      this.oldCursor = control.getCursor();
      control.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
      control.requestFocus();
      this.dragSource.setDragging(true);
    }
    update(screenX, screenY);
  }

  @Override
  public void cancel() {
    end();
  }

  @Override
  public void confirm(double screenX, double screenY) {
    update(screenX, screenY);
    DropTarget target = dropTarget;
    ViewOrGroup source = dragSource;
    end();
    if (target != null && source != null) {
      ViewGroupContainer root = Utils.getRootViewGroupContainer(source.asControl()).orElse(null);
      target.drop(this, source);
      if (!target.equals(DropTarget.none())) {
        Utils.normalizeOrCloseViewGroupContainer(root);
      }
    }
  }

  private void end() {
    if (this.dragSource != null && dragSource.asControl() != null) {
      Control control = this.dragSource.asControl();
      this.dragSource.setDragging(false);
      control.setCursor(oldCursor);
      control.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
      this.dragSource = null;
    }
    update(null);
  }

  @Override
  public void update(double screenX, double screenY) {
    Optional<DropTarget> target = findDropTargetInWindows(new Point2D(screenX, screenY));
    update(target.orElse(null));
  }

  private void update(DropTarget dropTarget) {
    ViewGroup oldViewGroup = getViewGroupFromTarget(this.dropTarget);
    ViewGroup newViewGroup = getViewGroupFromTarget(dropTarget);
    if (dragSource != null && dragSource.asControl() != null) {
      dragSource.asControl().setCursor(dropTarget instanceof None ? NO_DND_CURSOR : DND_CURSOR);
    }
    if (oldViewGroup != null && oldViewGroup != newViewGroup) {
      oldViewGroup.setDropTarget(null);
    }

    this.dropTarget = dropTarget;
    if (newViewGroup != null) {
      newViewGroup.setDropTarget(this.dropTarget);
    }
  }

  private ViewGroup getViewGroupFromTarget(DropTarget dropTarget) {
    if (dropTarget instanceof MoveToGroup moveToGroup) {
      return moveToGroup.viewGroup();
    } else if (dropTarget instanceof SplitViewGroup splitViewGroup) {
      return splitViewGroup.viewGroup();
    }
    return null;
  }

  private Optional<DropTarget> findDropTargetInWindows(Point2D screenPos) {
    double x = screenPos.getX();
    double y = screenPos.getY();
    Window window = null;
    List<Window> candidateWindows = Window.getWindows().stream()
        .filter(Window::isShowing)
        .filter(w -> w.getX() <= x && x <= w.getX() + w.getWidth())
        .filter(w -> w.getY() <= y && y <= w.getY() + w.getHeight())
        .toList();
    if (candidateWindows.size() > 1) {
      candidateWindows = candidateWindows.stream()
          .filter(Window::isFocused)
          .toList();
    }
    if (candidateWindows.size() == 1) {
      window = candidateWindows.get(0);
    }
    if (window == null) {
      return findDropTargetOutsideWindow(screenPos);
    } else {
      return findDropTargetInsideWindow(window, screenPos);
    }
  }

  private Optional<DropTarget> findDropTargetOutsideWindow(Point2D screenPos) {
    Set<DropTargetType> dropTargetTypes = dragSource.getDropTargetTypes();
    if (dropTargetTypes == null || !dropTargetTypes.contains(DropTargetType.NEW_WINDOW)) {
      return Optional.empty();
    } else {
      return Optional.of(DropTarget.newWindow(screenPos));
    }
  }

  private Optional<DropTarget> findDropTargetInsideWindow(Window window, Point2D screenPos) {
    Node node = window.getScene().getRoot();
    Point2D nodePos = node.screenToLocal(screenPos);
    return findDropTargetInNode(node, nodePos);
  }

  private Optional<DropTarget> findDropTargetInNode(Node node, Point2D nodePos) {
    if (!node.getBoundsInLocal().contains(nodePos)) {
      return Optional.empty();
    }

    return getLeafNodeAtPosition(node, nodePos)
        .flatMap(pair -> getViewGroupOfNode(pair.getKey(), pair.getValue()))
        .map(pair -> pair.getKey().findDropTarget(this, pair.getValue()))
        .orElseGet(() -> Optional.of(DropTarget.none()));
  }

  private Optional<Pair<ViewGroup, Point2D>> getViewGroupOfNode(Node node, Point2D nodePos) {
    if (node instanceof ViewGroup vg) {
      return Optional.of(new Pair<>(vg, nodePos));
    }
    if (node.getParent() != null) {
      return getViewGroupOfNode(node.getParent(), node.localToParent(nodePos));
    }
    return Optional.empty();
  }

  private Optional<Pair<Node, Point2D>> getLeafNodeAtPosition(Node node, Point2D nodePos) {
    if (!node.getBoundsInLocal().contains(nodePos) || !node.isVisible() || node.isDisabled()) {
      return Optional.empty();
    }

    if (node instanceof Parent parent) {
      return parent.getChildrenUnmodifiable().stream()
          .map(child -> getLeafNodeAtPosition(child, child.parentToLocal(nodePos)))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst()
          .or(() -> Optional.of(new Pair<>(node, nodePos)));
    }
    return Optional.of(new Pair<>(node, nodePos));
  }

}
