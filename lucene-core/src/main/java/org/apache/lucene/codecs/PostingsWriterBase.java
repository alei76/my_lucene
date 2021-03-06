package org.apache.lucene.codecs;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.codecs.blocktree.BlockTreeTermsWriter;
import org.apache.lucene.index.DocsAndPositionsEnum; // javadocs
import org.apache.lucene.index.DocsEnum; // javadocs
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;

/**
 * Class that plugs into term dictionaries, such as {@link
 * BlockTreeTermsWriter}, and handles writing postings.
 * 
 * @see PostingsReaderBase
 * @lucene.experimental
 */
// TODO: find a better name; this defines the API that the
// terms dict impls use to talk to a postings impl.
// TermsDict + PostingsReader/WriterBase == FieldsProducer/Consumer
public abstract class PostingsWriterBase implements Closeable {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected PostingsWriterBase() {
  }

  /** Called once after startup, before any terms have been
   *  added.  Implementations typically write a header to
   *  the provided {@code termsOut}. */
  public abstract void init(IndexOutput termsOut, SegmentWriteState state) throws IOException;

  /** Write all postings for one term; use the provided
   *  {@link TermsEnum} to pull a {@link DocsEnum} or {@link
   *  DocsAndPositionsEnum}.  This method should not
   *  re-position the {@code TermsEnum}!  It is already
   *  positioned on the term that should be written.  This
   *  method must set the bit in the provided {@link
   *  FixedBitSet} for every docID written.  If no docs
   *  were written, this method should return null, and the
   *  terms dict will skip the term.
   *  写所有的链表给一个term;使用提供的{@link TermsEnum}去得出一个{@link DocsEnum} 或者 {@link
   *  DocsAndPositionsEnum}.这个方法不应该重新排位给{@code TermsEnum}.它已经是term应该写的位置。这个方法必须设置已经提供的{@link
   *  FixedBitSet}对每个要写的docID.如果没有docs要被写,这个方法应该返回null。并且terms dict应该跳过这个term
   *   
   *   
   *   */
  public abstract BlockTermState writeTerm(BytesRef term, TermsEnum termsEnum, FixedBitSet docsSeen) throws IOException;

  /**
   * Encode metadata as long[] and byte[]. {@code absolute} controls whether 
   * current term is delta encoded according to latest term. 
   * Usually elements in {@code longs} are file pointers, so each one always 
   * increases when a new term is consumed. {@code out} is used to write generic
   * bytes, which are not monotonic.
   *
   * NOTE: sometimes long[] might contain "don't care" values that are unused, e.g. 
   * the pointer to postings list may not be defined for some terms but is defined
   * for others, if it is designed to inline  some postings data in term dictionary.
   * In this case, the postings writer should always use the last value, so that each
   * element in metadata long[] remains monotonic.
   */
  public abstract void encodeTerm(long[] longs, DataOutput out, FieldInfo fieldInfo, BlockTermState state, boolean absolute) throws IOException;

  /** 
   * Sets the current field for writing, and returns the
   * fixed length of long[] metadata (which is fixed per
   * field), called when the writing switches to another field. */
  // TODO: better name?
  public abstract int setField(FieldInfo fieldInfo);

  @Override
  public abstract void close() throws IOException;
}
