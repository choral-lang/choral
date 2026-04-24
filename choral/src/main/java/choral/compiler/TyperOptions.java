package choral.compiler;

import choral.ast.Position;
import choral.utils.VerbosityLevel;
import java.util.function.BiConsumer;

/** Options for configuring {@link choral.compiler.Typer}. */
public class TyperOptions {
  private VerbosityLevel verbosity;
  private boolean relaxed;
  private BiConsumer<Position, String> infoChannel;

  public TyperOptions(VerbosityLevel verbosity) {
    // Default to "normal" mode, where communications are written manually
    this(verbosity, false, TyperOptions::stdInfo);
  }

  private TyperOptions(
      VerbosityLevel verbosity, boolean relaxed, BiConsumer<Position, String> infoChannel) {
    this.verbosity = verbosity;
    this.relaxed = relaxed;
    this.infoChannel = infoChannel;
  }

  /** The verbosity level to use when debugging. */
  public VerbosityLevel verbosity() {
    return verbosity;
  }

  /**
   * Whether the typer should run in "relaxed mode", allowing programs where the roles don't match
   * like {@code int@A x = 1@A; int@B y = x + 1@B}. Used for communication inference.
   */
  public boolean relaxed() {
    return relaxed;
  }

  /**
   * Log an info message if verbosity is at least INFO.
   *
   * @param position The position in the source code where the message is relevant (may be null)
   * @param msg The info message
   */
  public void info(Position position, String msg) {
    if (verbosity.compareTo(VerbosityLevel.INFO) >= 0) infoChannel.accept(position, msg);
  }

  /** The default info channel, which prints messages to standard error. */
  private static void stdInfo(Position position, String msg) {
    if (position != null) {
      System.err.println(msg + " at " + position.formattedPosition());
    } else {
      System.err.println(msg);
    }
  }

  /**
   * @return A copy of TyperOptions with {@link #relaxed} mode enabled.
   */
  public TyperOptions relaxedMode() {
    return new TyperOptions(verbosity, true, infoChannel);
  }

  /**
   * @return A copy of TyperOptions with {@link #relaxed} mode disabled.
   */
  public TyperOptions normalMode() {
    return new TyperOptions(verbosity, false, infoChannel);
  }

  /**
   * @return A copy of TyperOptions with the given info channel.
   */
  public TyperOptions withInfoChannel(BiConsumer<Position, String> infoChannel) {
    return new TyperOptions(verbosity, relaxed, infoChannel);
  }
}
