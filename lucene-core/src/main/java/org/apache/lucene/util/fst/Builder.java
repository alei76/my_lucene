package org.apache.lucene.util.fst;

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

import java.io.IOException;

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.fst.FST.INPUT_TYPE; // javadoc
import org.apache.lucene.util.packed.PackedInts;

// TODO: could we somehow stream an FST to disk while we
// build it?

/**
 * Builds a minimal FST (maps an IntsRef term to an arbitrary
 * output) from pre-sorted terms with outputs.  The FST
 * becomes an FSA if you use NoOutputs.  The FST is written
 * on-the-fly into a compact serialized format byte array, which can
 * be saved to / loaded from a Directory or used directly
 * for traversal.  The FST is always finite (no cycles).
 *
 * <p>NOTE: The algorithm is described at
 * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.24.3698</p>
 *
 * <p>The parameterized type T is the output type.  See the
 * subclasses of {@link Outputs}.
 *
 * <p>FSTs larger than 2.1GB are now possible (as of Lucene
 * 4.2).  FSTs containing more than 2.1B nodes are also now
 * possible, however they cannot be packed.
 *
 * @lucene.experimental
 */

public class Builder<T> {
  private final NodeHash<T> dedupHash;
  private final FST<T> fst;
  private final T NO_OUTPUT;

  // private static final boolean DEBUG = true;

  // simplistic pruning: we prune node (and all following
  // nodes) if less than this number of terms go through it:
  private final int minSuffixCount1;

  // better pruning: we prune node (and all following
  // nodes) if the prior node has less than this number of
  // terms go through it:
  private final int minSuffixCount2;

  private final boolean doShareNonSingletonNodes;
  private final int shareMaxTailLength;

  private final IntsRefBuilder lastInput = new IntsRefBuilder();
  
  // for packing
  private final boolean doPackFST;
  private final float acceptableOverheadRatio;

  // NOTE: cutting this over to ArrayList instead loses ~6%
  // in build performance on 9.8M Wikipedia terms; so we
  // left this as an array:
  // current "frontier"
  private UnCompiledNode<T>[] frontier;

  /**
   * Instantiates an FST/FSA builder without any pruning. A shortcut
   * to {@link #Builder(FST.INPUT_TYPE, int, int, boolean,
   * boolean, int, Outputs, boolean, float,
   * boolean, int)} with pruning options turned off.
   */
  public Builder(FST.INPUT_TYPE inputType, Outputs<T> outputs) {
    this(inputType, 0, 0, true, true, Integer.MAX_VALUE, outputs, false, PackedInts.COMPACT, true, 15);
  }

  /**
   * Instantiates an FST/FSA builder with all the possible tuning and construction
   * tweaks. Read parameter documentation carefully.
   * 
   * @param inputType 
   *    The input type (transition labels). Can be anything from {@link INPUT_TYPE}
   *    enumeration. Shorter types will consume less memory. Strings (character sequences) are 
   *    represented as {@link INPUT_TYPE#BYTE4} (full unicode codepoints). 
   *    输入的type。能够使{@link INPUT_TYPE}枚举中的任意一个，越小的types消耗的内存就越少。Strings类型的是{@link INPUT_TYPE#BYTE4}(unicode编码)
   *    
   * @param minSuffixCount1
   *    If pruning the input graph during construction, this threshold is used for telling
   *    if a node is kept or pruned. If transition_count(node) &gt;= minSuffixCount1, the node
   *    is kept. 
   *    在构造时，是否减少输入图,这个阀值用来判断一个节点是否保存还是删除,如果transition_count(node)大于minSuffixCount1，这个节点就保留
   * @param minSuffixCount2
   *    (Note: only Mike McCandless knows what this one is really doing...) 
   * 
   * @param doShareSuffix 
   *    If <code>true</code>, the shared suffixes will be compacted into unique paths.
   *    This requires an additional RAM-intensive hash map for lookups in memory. Setting this parameter to
   *    <code>false</code> creates a single suffix path for all input sequences. This will result in a larger
   *    FST, but requires substantially less memory and CPU during building.  
   *	如果为true,这些共享的前缀将被压缩到一个唯一的路径。这需要额外的密集的hashmap为在内存中查找。
   *	如果为false，创造一个单独的前缀路径对所有的输入序列。这个会导致一个更大的FSt,但是在构建的时候，基本上需要更少的内存和cpu
   *
   * @param doShareNonSingletonNodes
   *    Only used if doShareSuffix is true.  Set this to
   *    true to ensure FST is fully minimal, at cost of more
   *    CPU and more RAM during building.
   *    只在doShareSuffix是true时使用。设置这个为true确保FST是最小的,在构建的时候花费更多的cpu和内存。
   *
   * @param shareMaxTailLength
   *    Only used if doShareSuffix is true.  Set this to
   *    Integer.MAX_VALUE to ensure FST is fully minimal, at cost of more
   *    CPU and more RAM during building.
   *
   * @param outputs The output type for each input sequence. Applies only if building an FST. For
   *    FSA, use {@link NoOutputs#getSingleton()} and {@link NoOutputs#getNoOutput()} as the
   *    singleton output object.
   *
   * @param doPackFST Pass true to create a packed FST.
   * 
   * @param acceptableOverheadRatio How to trade speed for space when building the FST. This option
   *    is only relevant when doPackFST is true. @see PackedInts#getMutable(int, int, float)
   *
   * @param allowArrayArcs Pass false to disable the array arc optimization
   *    while building the FST; this will make the resulting
   *    FST smaller but slower to traverse.
   *
   * @param bytesPageBits How many bits wide to make each
   *    byte[] block in the BytesStore; if you know the FST
   *    will be large then make this larger.  For example 15
   *    bits = 32768 byte pages.
   */
  public Builder(FST.INPUT_TYPE inputType, int minSuffixCount1, int minSuffixCount2, boolean doShareSuffix,
                 boolean doShareNonSingletonNodes, int shareMaxTailLength, Outputs<T> outputs,
                 boolean doPackFST, float acceptableOverheadRatio, boolean allowArrayArcs,
                 int bytesPageBits) {
    this.minSuffixCount1 = minSuffixCount1;
    this.minSuffixCount2 = minSuffixCount2;
    this.doShareNonSingletonNodes = doShareNonSingletonNodes;
    this.shareMaxTailLength = shareMaxTailLength;
    this.doPackFST = doPackFST;
    this.acceptableOverheadRatio = acceptableOverheadRatio;
    fst = new FST<>(inputType, outputs, doPackFST, acceptableOverheadRatio, allowArrayArcs, bytesPageBits);
    if (doShareSuffix) {
      dedupHash = new NodeHash<>(fst, fst.bytes.getReverseReader(false));
    } else {
      dedupHash = null;
    }
    NO_OUTPUT = outputs.getNoOutput();

    @SuppressWarnings({"rawtypes","unchecked"})
    final UnCompiledNode<T>[] f = (UnCompiledNode<T>[]) new UnCompiledNode[10];
    frontier = f;
    for(int idx=0;idx<frontier.length;idx++) {
      frontier[idx] = new UnCompiledNode<>(this, idx);
    }
  }

  public long getTotStateCount() {
    return fst.nodeCount;
  }

  public long getTermCount() {
    return frontier[0].inputCount;
  }

  public long getMappedStateCount() {
    return dedupHash == null ? 0 : fst.nodeCount;
  }

  private CompiledNode compileNode(UnCompiledNode<T> nodeIn, int tailLength) throws IOException {
    final long node;
    if (dedupHash != null && (doShareNonSingletonNodes || nodeIn.numArcs <= 1) && tailLength <= shareMaxTailLength) {
      if (nodeIn.numArcs == 0) {
        node = fst.addNode(nodeIn);
      } else {
        node = dedupHash.add(nodeIn);
      }
    } else {
      node = fst.addNode(nodeIn);
    }
    assert node != -2;

    nodeIn.clear();                //当节点UnCompiledNode变成CompiledNode时，都clear()掉。

    final CompiledNode fn = new CompiledNode();
    fn.node = node;
    return fn;
  }

  private void freezeTail(int prefixLenPlus1) throws IOException {
    //System.out.println("  compileTail " + prefixLenPlus1);
    final int downTo = Math.max(1, prefixLenPlus1);
    for(int idx=lastInput.length(); idx >= downTo; idx--) {

      boolean doPrune = false;
      boolean doCompile = false;

      final UnCompiledNode<T> node = frontier[idx];
      final UnCompiledNode<T> parent = frontier[idx-1];

      if (node.inputCount < minSuffixCount1) {
        doPrune = true;
        doCompile = true;
      } else if (idx > prefixLenPlus1) {
        // prune if parent's inputCount is less than suffixMinCount2
        if (parent.inputCount < minSuffixCount2 || (minSuffixCount2 == 1 && parent.inputCount == 1 && idx > 1)) {
          // my parent, about to be compiled, doesn't make the cut, so
          // I'm definitely pruned 

          // if minSuffixCount2 is 1, we keep only up
          // until the 'distinguished edge', ie we keep only the
          // 'divergent' part of the FST. if my parent, about to be
          // compiled, has inputCount 1 then we are already past the
          // distinguished edge.  NOTE: this only works if
          // the FST outputs are not "compressible" (simple
          // ords ARE compressible).
          doPrune = true;
        } else {
          // my parent, about to be compiled, does make the cut, so
          // I'm definitely not pruned 
          doPrune = false;
        }
        doCompile = true;
      } else {
        // if pruning is disabled (count is 0) we can always
        // compile current node
        doCompile = minSuffixCount2 == 0;
      }

      //System.out.println("    label=" + ((char) lastInput.ints[lastInput.offset+idx-1]) + " idx=" + idx + " inputCount=" + frontier[idx].inputCount + " doCompile=" + doCompile + " doPrune=" + doPrune);

      if (node.inputCount < minSuffixCount2 || (minSuffixCount2 == 1 && node.inputCount == 1 && idx > 1)) {
        // drop all arcs
        for(int arcIdx=0;arcIdx<node.numArcs;arcIdx++) {
          @SuppressWarnings({"rawtypes","unchecked"}) 
          final UnCompiledNode<T> target = (UnCompiledNode<T>) node.arcs[arcIdx].target;
          target.clear();
        }
        node.numArcs = 0;
      }

      if (doPrune) {
        // this node doesn't make it -- deref it
        node.clear();
        parent.deleteLast(lastInput.intAt(idx-1), node);
      } else {

        if (minSuffixCount2 != 0) {
          compileAllTargets(node, lastInput.length()-idx);
        }
        final T nextFinalOutput = node.output;

        // We "fake" the node as being final if it has no
        // outgoing arcs; in theory we could leave it
        // as non-final (the FST can represent this), but
        // FSTEnum, Util, etc., have trouble w/ non-final
        // dead-end states:
        final boolean isFinal = node.isFinal || node.numArcs == 0;

        if (doCompile) {
          // this node makes it and we now compile it.  first,
          // compile any targets that were previously
          // undecided:
          parent.replaceLast(lastInput.intAt(idx-1),
                             compileNode(node, 1+lastInput.length()-idx),
                             nextFinalOutput,
                             isFinal);
        } else {
          // replaceLast just to install
          // nextFinalOutput/isFinal onto the arc
          parent.replaceLast(lastInput.intAt(idx-1),
                             node,
                             nextFinalOutput,
                             isFinal);
          // this node will stay in play for now, since we are
          // undecided on whether to prune it.  later, it
          // will be either compiled or pruned, so we must
          // allocate a new node:
          frontier[idx] = new UnCompiledNode<>(this, idx);
        }
      }
    }
  }

  // for debugging
  /*
  private String toString(BytesRef b) {
    try {
      return b.utf8ToString() + " " + b;
    } catch (Throwable t) {
      return b.toString();
    }
  }
  */

  /** It's OK to add the same input twice in a row with
   *  different outputs, as long as outputs impls the merge
   *  method. Note that input is fully consumed after this
   *  method is returned (so caller is free to reuse), but
   *  output is not.  So if your outputs are changeable (eg
   *  {@link ByteSequenceOutputs} or {@link
   *  IntSequenceOutputs}) then you cannot reuse across
   *  calls. */
  public void add(IntsRef input, T output) throws IOException {
    /*
    if (DEBUG) {
      BytesRef b = new BytesRef(input.length);
      for(int x=0;x<input.length;x++) {
        b.bytes[x] = (byte) input.ints[x];
      }
      b.length = input.length;
      if (output == NO_OUTPUT) {
        System.out.println("\nFST ADD: input=" + toString(b) + " " + b);
      } else {
        System.out.println("\nFST ADD: input=" + toString(b) + " " + b + " output=" + fst.outputs.outputToString(output));
      }
    }
    */

    // De-dup NO_OUTPUT since it must be a singleton:保证NO_OUTPUT必须是单例的.
    if (output.equals(NO_OUTPUT)) {
      output = NO_OUTPUT;
    }

    assert lastInput.length() == 0 || input.compareTo(lastInput.get()) >= 0: "inputs are added out of order lastInput=" + lastInput.get() + " vs input=" + input;
    assert validOutput(output);

    //System.out.println("\nadd: " + input);
    if (input.length == 0) {
      // empty input: only allowed as first input.  we have
      // to special case this because the packed FST
      // format cannot represent the empty input since
      // 'finalness' is stored on the incoming arc, not on
      // the node
      frontier[0].inputCount++;
      frontier[0].isFinal = true;
      fst.setEmptyOutput(output);
      return;
    }

    // 计算公共前缀的长度
    int pos1 = 0;
    int pos2 = input.offset;
    final int pos1Stop = Math.min(lastInput.length(), input.length);
    while(true) {
      frontier[pos1].inputCount++;
      //System.out.println("  incr " + pos1 + " ct=" + frontier[pos1].inputCount + " n=" + frontier[pos1]);
      if (pos1 >= pos1Stop || lastInput.intAt(pos1) != input.ints[pos2]) {
        break;
      }
      pos1++;
      pos2++;
    }
    final int prefixLenPlus1 = pos1+1;
      
    if (frontier.length < input.length+1) {
      @SuppressWarnings({"rawtypes","unchecked"}) 
      final UnCompiledNode<T>[] next = new UnCompiledNode[ArrayUtil.oversize(input.length+1, RamUsageEstimator.NUM_BYTES_OBJECT_REF)];
      System.arraycopy(frontier, 0, next, 0, frontier.length);
      for(int idx=frontier.length;idx<next.length;idx++) {
        next[idx] = new UnCompiledNode<>(this, idx);
      }
      frontier = next;
    }

    //从尾部一直到公共前缀的节点，将已经确定的状态节点冰封
    freezeTail(prefixLenPlus1);

    // 将当前输入字符串的后缀形成状态节点加入到FST树中,由frontier维护。
    for(int idx=prefixLenPlus1;idx<=input.length;idx++) {
      frontier[idx-1].addArc(input.ints[input.offset + idx - 1],frontier[idx]);
      frontier[idx].inputCount++;
    }

    final UnCompiledNode<T> lastNode = frontier[input.length];
    //判断是否为一个输入最后的节点。
    if (lastInput.length() != input.length || prefixLenPlus1 != input.length + 1) {
      lastNode.isFinal = true;
      lastNode.output = NO_OUTPUT;
    }

    // push conflicting outputs forward, only as far as
    // needed。将输出的冲突向前推。
    for(int idx=1;idx<prefixLenPlus1;idx++) {
      final UnCompiledNode<T> node = frontier[idx];
      final UnCompiledNode<T> parentNode = frontier[idx-1];

      final T lastOutput = parentNode.getLastOutput(input.ints[input.offset + idx - 1]);
      assert validOutput(lastOutput);

      final T commonOutputPrefix;
      final T wordSuffix;

      if (lastOutput != NO_OUTPUT) {
        commonOutputPrefix = fst.outputs.common(output, lastOutput);//计算这次输出和上次输出公共的前缀。
        assert validOutput(commonOutputPrefix);
        wordSuffix = fst.outputs.subtract(lastOutput, commonOutputPrefix);
        assert validOutput(wordSuffix);
        parentNode.setLastOutput(input.ints[input.offset + idx - 1], commonOutputPrefix);//前一个节点的输出修改为公共前缀的输出。
        node.prependOutput(wordSuffix);
      } else {
        commonOutputPrefix = wordSuffix = NO_OUTPUT;
      }

      output = fst.outputs.subtract(output, commonOutputPrefix);
      assert validOutput(output);
    }

    if (lastInput.length() == input.length && prefixLenPlus1 == 1+input.length) {
      // same input more than 1 time in a row, mapping to
      // multiple outputs不止一次相同的输入,对应到不同的输出。
      lastNode.output = fst.outputs.merge(lastNode.output, output);
    } else {
      // this new arc is private to this new input; set its
      // arc output to the leftover output:
      //这个新的弧对新的输入是私有的，设置这个弧的输出所剩余输出。
      frontier[prefixLenPlus1-1].setLastOutput(input.ints[input.offset + prefixLenPlus1-1], output);
    }

    // save last input
    lastInput.copyInts(input);

    //System.out.println("  count[0]=" + frontier[0].inputCount);
  }

  private boolean validOutput(T output) {
    return output == NO_OUTPUT || !output.equals(NO_OUTPUT);
  }

  /** Returns final FST.  NOTE: this will return null if
   *  nothing is accepted by the FST. */
  public FST<T> finish() throws IOException {

    final UnCompiledNode<T> root = frontier[0];

    // minimize nodes in the last word's suffix
    freezeTail(0);
    if (root.inputCount < minSuffixCount1 || root.inputCount < minSuffixCount2 || root.numArcs == 0) {
      if (fst.emptyOutput == null) {
        return null;
      } else if (minSuffixCount1 > 0 || minSuffixCount2 > 0) {
        // empty string got pruned
        return null;
      }
    } else {
      if (minSuffixCount2 != 0) {
        compileAllTargets(root, lastInput.length());
      }
    }
    //if (DEBUG) System.out.println("  builder.finish root.isFinal=" + root.isFinal + " root.output=" + root.output);
    fst.finish(compileNode(root, lastInput.length()).node);

    if (doPackFST) {
      return fst.pack(3, Math.max(10, (int) (fst.getNodeCount()/4)), acceptableOverheadRatio);
    } else {
      return fst;
    }
  }

  private void compileAllTargets(UnCompiledNode<T> node, int tailLength) throws IOException {
    for(int arcIdx=0;arcIdx<node.numArcs;arcIdx++) {
      final Arc<T> arc = node.arcs[arcIdx];
      if (!arc.target.isCompiled()) {
        // not yet compiled
        @SuppressWarnings({"rawtypes","unchecked"}) final UnCompiledNode<T> n = (UnCompiledNode<T>) arc.target;
        if (n.numArcs == 0) {
          //System.out.println("seg=" + segment + "        FORCE final arc=" + (char) arc.label);
          arc.isFinal = n.isFinal = true;
        }
        arc.target = compileNode(n, tailLength-1);
      }
    }
  }

  /** Expert: holds a pending (seen but not yet serialized) arc. */
  public static class Arc<T> {
    public int label;                             // really an "unsigned" byte
    public Node target;
    public boolean isFinal;
    public T output;
    public T nextFinalOutput;
  }

  // NOTE: not many instances of Node or CompiledNode are in
  // memory while the FST is being built; it's only the
  // current "frontier":

  static interface Node {
    boolean isCompiled();
  }

  public long fstRamBytesUsed() {
    return fst.ramBytesUsed();
  }

  static final class CompiledNode implements Node {
    long node;
    @Override
    public boolean isCompiled() {
      return true;
    }
  }

  /** Expert: holds a pending (seen but not yet serialized) Node. */
  public static final class UnCompiledNode<T> implements Node {
    final Builder<T> owner;
    public int numArcs;
    public Arc<T>[] arcs;
    // TODO: instead of recording isFinal/output on the
    // node, maybe we should use -1 arc to mean "end" (like
    // we do when reading the FST).  Would simplify much
    // code here...
    public T output;
    public boolean isFinal;
    public long inputCount;

    /** This node's depth, starting from the automaton root. */
    public final int depth;

    /**
     * @param depth
     *          The node's depth starting from the automaton root. Needed for
     *          LUCENE-2934 (node expansion based on conditions other than the
     *          fanout size).
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    public UnCompiledNode(Builder<T> owner, int depth) {
      this.owner = owner;
      arcs = (Arc<T>[]) new Arc[1];
      arcs[0] = new Arc<>();
      output = owner.NO_OUTPUT;
      this.depth = depth;
    }

    @Override
    public boolean isCompiled() {
      return false;
    }

    public void clear() {
      numArcs = 0;
      isFinal = false;
      output = owner.NO_OUTPUT;
      inputCount = 0;

      // We don't clear the depth here because it never changes 
      // for nodes on the frontier (even when reused).
    }

    public T getLastOutput(int labelToMatch) {
      assert numArcs > 0;
      assert arcs[numArcs-1].label == labelToMatch;
      return arcs[numArcs-1].output;
    }

    public void addArc(int label, Node target) {
      assert label >= 0;
      assert numArcs == 0 || label > arcs[numArcs-1].label: "arc[-1].label=" + arcs[numArcs-1].label + " new label=" + label + " numArcs=" + numArcs;
      //如果节点的出度等于现在弧数组的长度，就需要扩容了。
      if (numArcs == arcs.length) {
        @SuppressWarnings({"rawtypes","unchecked"}) 
        final Arc<T>[] newArcs = new Arc[ArrayUtil.oversize(numArcs+1, RamUsageEstimator.NUM_BYTES_OBJECT_REF)];
        System.arraycopy(arcs, 0, newArcs, 0, arcs.length);
        for(int arcIdx=numArcs;arcIdx<newArcs.length;arcIdx++) {
          newArcs[arcIdx] = new Arc<>();
        }
        arcs = newArcs;
      }
      final Arc<T> arc = arcs[numArcs++];       //如果之前的弧被freeze到complieNode。则会被新的覆盖。
      arc.label = label;
      arc.target = target;
      arc.output = arc.nextFinalOutput = owner.NO_OUTPUT;
      arc.isFinal = false;
    }

    public void replaceLast(int labelToMatch, Node target, T nextFinalOutput, boolean isFinal) {
      assert numArcs > 0;
      final Arc<T> arc = arcs[numArcs-1];
      assert arc.label == labelToMatch: "arc.label=" + arc.label + " vs " + labelToMatch;
      arc.target = target;
      //assert target.node != -2;
      arc.nextFinalOutput = nextFinalOutput;
      arc.isFinal = isFinal;
    }

    public void deleteLast(int label, Node target) {
      assert numArcs > 0;
      assert label == arcs[numArcs-1].label;
      assert target == arcs[numArcs-1].target;
      numArcs--;
    }

    public void setLastOutput(int labelToMatch, T newOutput) {
      assert owner.validOutput(newOutput);
      assert numArcs > 0;
      final Arc<T> arc = arcs[numArcs-1];
      assert arc.label == labelToMatch;
      arc.output = newOutput;
    }

    // pushes an output prefix forward onto all arcs
    public void prependOutput(T outputPrefix) {
      assert owner.validOutput(outputPrefix);

      for(int arcIdx=0;arcIdx<numArcs;arcIdx++) {
        arcs[arcIdx].output = owner.fst.outputs.add(outputPrefix, arcs[arcIdx].output);
        assert owner.validOutput(arcs[arcIdx].output);
      }

      if (isFinal) {
        output = owner.fst.outputs.add(outputPrefix, output);
        assert owner.validOutput(output);
      }
    }
  }
}
