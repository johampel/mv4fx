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
package de.hipphampel.mv4fx.utils;

import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import static javafx.geometry.Side.TOP;

import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import de.hipphampel.mv4fx.view.ViewStage;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Collection of utility methods.
 */
public class Utils {

  /**
   * Ensures that {@code val} is between {@code min} and {@code max}.
   * <p>
   * Basically, the method returns {@code min}, if {@code val} is less than {@code min}, {@code max}
   * if {@code val} is greater than {@code max}, otherwise it returns {@code val}
   * <p>
   * Note that this method deals only with positive values. Negative values are translated as
   * follows: for {@code min} and {@code val} the negative value becomes 0 and for {@code max}  the
   * biggest possible double value. This behaviour allows to pass negative value to {@code min} and
   * {@code max} indicating that it is not defined.
   * <p>
   * A final special rule applies, if {@code min} is larger than {@code max}. In this case
   * {@code min} is set to {@code max}
   *
   * @param val The value
   * @param min The minimum allowed value, negative if no limit is specified
   * @param max The maximum allowed value, negative, if no limit is specified
   * @return Either {@code val} if it is between the limits, or one of the limits.
   */
  public static double ensureValueIsBetween(double val, double min, double max) {
    if (min < 0) {
      min = 0;
    }
    if (max < 0) {
      max = Double.MAX_VALUE;
    }
    if (min > max) {
      min = max;
    }
    return Math.max(min, Math.min(max, val));
  }

  /**
   * Calculates the sum of the given {@code values}
   *
   * @param values The values
   * @return The sum.
   */
  public static double sum(double... values) {
    double sum = 0;
    for (double v : values) {
      sum += v;
    }
    return sum;
  }

  /**
   * Checks, whether the drag tags match the drop tags.
   * <p>
   * This is the case, if the drop tags is a super set or equal to the drag tags.
   *
   * @param dragTags The drop tags
   * @param dropTags The drag tags
   * @return {@code true} is matching
   */
  public static boolean isDragAndDropTagMatch(Set<String> dragTags, Set<String> dropTags) {
    if (dragTags == null || dragTags.isEmpty()) {
      return true;
    }
    if (dropTags == null || dropTags.isEmpty()) {
      return false;
    }
    return dropTags.containsAll(dragTags);
  }

  /**
   * Gets the opposite of {@code side}
   *
   * @param side The {@link Side}
   * @return The opposite
   */
  public static Side getOppositeSide(Side side) {
    if (side == null) {
      return null;
    }
    return switch (side) {
      case TOP -> BOTTOM;
      case BOTTOM -> TOP;
      case LEFT -> RIGHT;
      case RIGHT -> LEFT;
    };
  }

  /**
   * Gets the parent {@link ViewGroupContainer} of the given {@code node}
   *
   * @param node The reference {@link Node}
   * @return Maybe a {@code ViewGroupContainer}
   */
  public static Optional<ViewGroupContainer> getParentViewGroupContainer(Node node) {
    while (node != null) {
      node = node.getParent();
      if (node instanceof ViewGroupContainer container) {
        return Optional.of(container);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the root {@link ViewGroupContainer} of the given{@code node}
   *
   * @param node The reference {@link Node}
   * @return Maybe a {@code ViewGroupContainer}
   */
  public static Optional<ViewGroupContainer> getRootViewGroupContainer(Node node) {
    ViewGroupContainer container =
        node instanceof ViewGroupContainer c ? c : getParentViewGroupContainer(node).orElse(null);
    while (container != null) {
      ViewGroupContainer parent = getParentViewGroupContainer(container).orElse(null);
      if (parent == null) {
        return Optional.of(container);
      }
      container = parent;
    }
    return Optional.empty();
  }

  /**
   * Normalizes or closes the given {@link ViewGroupContainer}.
   * <p>
   * This basically means the {@link ViewGroupContainer#normalize() normalize} is called for
   * {@code container} itself. If {@code container} is empty (has no children) and it is the only
   * child of a {@link ViewStage}, the stage is closed as well.
   *
   * @param container The {@link ViewGroupContainer} to normalize
   */
  public static void normalizeOrCloseViewGroupContainer(ViewGroupContainer container) {
    if (container == null) {
      return;
    }
    container.normalize();
    if (container.getLeftTop() == null && container.getRightBottom() == null
        && container.getParent() == null) {
      if (container.getScene().getWindow() instanceof ViewStage stage && stage.isAutoClose()) {
        stage.close();
      }
    }
  }

  /**
   * Gets all {@link View Views} that are direct or indirect children of {@code start}.
   * <p>
   * If {@code start} itself is a {@code View}, it returns {@code start} as well
   *
   * @param start The start {@code Node}
   * @return {@link Stream} of {@code Views}
   */
  public static Stream<View> getAllViewsIn(Node start) {
    return getAllMatchingNodes(start, n -> n instanceof ViewGroup)
        .map(group -> (ViewGroup) group)
        .flatMap(group -> group.getViews().stream());
  }

  /**
   * Finds all nodes that match the given {@code predicate}.
   * <p>
   * The method applies {@code predicate} to {@code start} and all direct and indirect children and
   * returns those {@link Node Nodes} that match the {@code predicate}
   *
   * @param start     The start {@code Node}
   * @param predicate The {@link Predicate}
   * @return {@link Stream} of matching {@code Nodes}
   */
  public static Stream<Node> getAllMatchingNodes(Node start, Predicate<Node> predicate) {
    if (start instanceof Parent parent) {
      return Stream.concat(
          parent.getChildrenUnmodifiable()
              .stream()
              .flatMap(node -> getAllMatchingNodes(node, predicate)),
          Stream.of(start).filter(predicate));
    } else {
      return Optional.ofNullable(start).stream()
          .filter(predicate);
    }
  }
}
