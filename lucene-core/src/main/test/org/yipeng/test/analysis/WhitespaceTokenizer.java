package org.yipeng.test.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeFactory;

/**
 * A WhitespaceTokenizer is a tokenizer that divides text at whitespace.
 * Adjacent sequences of non-Whitespace characters form tokens.
 */
public final class WhitespaceTokenizer extends CharTokenizer {
  
  /**
   * Construct a new WhitespaceTokenizer.
   */
  public WhitespaceTokenizer() {
  }

  /**
   * Construct a new WhitespaceTokenizer using a given
   * {@link org.apache.lucene.util.AttributeFactory}.
   *
   * @param factory
   *          the attribute factory to use for this {@link Tokenizer}
   */
  public WhitespaceTokenizer(AttributeFactory factory) {
    super(factory);
  }
  
  /** Collects only characters which do not satisfy
   * {@link Character#isWhitespace(int)}.*/
  @Override
  protected boolean isTokenChar(int c) {
    return !Character.isWhitespace(c);
  }
}
