/*
 * Copyright (c) 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.amrjlg.stream.pipeline;

import io.github.amrjlg.stream.BaseStream;
import io.github.amrjlg.stream.Sink;
import io.github.amrjlg.stream.StreamOpFlag;
import io.github.amrjlg.stream.StreamShape;
import io.github.amrjlg.stream.Streams;
import io.github.amrjlg.stream.TerminalOp;
import io.github.amrjlg.stream.node.Node;
import io.github.amrjlg.stream.node.NodeBuilder;
import io.github.amrjlg.stream.spliterator.Spliterator;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;


/**
 * @author amrjlg
 **/
public abstract class AbstractPipeline<Input, Output, Stream extends BaseStream<Output, Stream>>
        implements PipelineHelper<Output>, BaseStream<Output, Stream> {

    private static final String MSG_STREAM_LINKED = "stream has already been operated upon or closed";
    private static final String MSG_CONSUMED = "source already consumed or closed";

    protected static final int NOT_SORTED_AND_NOT_DISTINCT = StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT;

    private final AbstractPipeline sourceStage;

    private final AbstractPipeline previousStage;

    protected final int sourceOrOpFlags;

    private AbstractPipeline nextStage;

    private int depth;

    private int combinedFlags;

    private Spliterator sourceSpliterator;

    private Supplier<? extends Spliterator<?>> sourceSupplier;

    private boolean linkedOrConsumed;

    private boolean sourceAnyStateful;
    private Runnable sourceCloseAction;

    private boolean parallel;

    public AbstractPipeline(Supplier<? extends Spliterator<?>> source,
                            int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSupplier = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // The following is an optimization of:
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    public AbstractPipeline(Spliterator<?> source,
                            int sourceFlags, boolean parallel) {
        this.previousStage = null;
        this.sourceSpliterator = source;
        this.sourceStage = this;
        this.sourceOrOpFlags = sourceFlags & StreamOpFlag.STREAM_MASK;
        // The following is an optimization of:
        // StreamOpFlag.combineOpFlags(sourceOrOpFlags, StreamOpFlag.INITIAL_OPS_VALUE);
        this.combinedFlags = (~(sourceOrOpFlags << 1)) & StreamOpFlag.INITIAL_OPS_VALUE;
        this.depth = 0;
        this.parallel = parallel;
    }

    AbstractPipeline(AbstractPipeline<?, Input, ?> previousStage, int opFlags) {
        if (previousStage.linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        previousStage.linkedOrConsumed = true;
        previousStage.nextStage = this;

        this.previousStage = previousStage;
        this.sourceOrOpFlags = opFlags & StreamOpFlag.OP_MASK;
        this.combinedFlags = StreamOpFlag.combineOpFlags(opFlags, previousStage.combinedFlags);
        this.sourceStage = previousStage.sourceStage;
        if (opIsStateful())
            sourceStage.sourceAnyStateful = true;
        this.depth = previousStage.depth + 1;
    }


    final <R> R evaluate(TerminalOp<Output, R> terminalOp) {
        assert getOutputShape() == terminalOp.inputShape();
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        return isParallel()
                ? terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()))
                : terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));
    }

    @SuppressWarnings("unchecked")
    private Spliterator<?> sourceSpliterator(int terminalFlags) {
        Spliterator<?> spliterator;
        if (sourceStage.sourceSpliterator != null) {
            spliterator = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
        } else if (sourceStage.sourceSupplier != null) {
            spliterator = (Spliterator<?>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
        } else {
            throw new IllegalStateException(MSG_CONSUMED);
        }

        if (isParallel() && sourceStage.sourceAnyStateful) {
            // Adapt the source spliterator, evaluating each stateful op
            // in the pipeline up to and including this pipeline stage.
            // The depth and flags of each pipeline stage are adjusted accordingly.
            int depth = 1;
            for (@SuppressWarnings("rawtypes") AbstractPipeline u = sourceStage, p = sourceStage.nextStage, e = this;
                 u != e;
                 u = p, p = p.nextStage) {

                int thisOpFlags = p.sourceOrOpFlags;
                if (p.opIsStateful()) {
                    depth = 0;

                    if (StreamOpFlag.SHORT_CIRCUIT.isKnown(thisOpFlags)) {
                        // Clear the short circuit flag for next pipeline stage
                        // This stage encapsulates short-circuiting, the next
                        // stage may not have any short-circuit operations, and
                        // if so spliterator.forEachRemaining should be used
                        // for traversal
                        thisOpFlags = thisOpFlags & ~StreamOpFlag.IS_SHORT_CIRCUIT;
                    }

                    spliterator = p.opEvaluateParallelLazy(u, spliterator);

                    // Inject or clear SIZED on the source pipeline stage
                    // based on the stage's spliterator
                    thisOpFlags = spliterator.hasCharacteristics(Spliterator.SIZED)
                            ? (thisOpFlags & ~StreamOpFlag.NOT_SIZED) | StreamOpFlag.IS_SIZED
                            : (thisOpFlags & ~StreamOpFlag.IS_SIZED) | StreamOpFlag.NOT_SIZED;
                }
                p.depth = depth++;
                p.combinedFlags = StreamOpFlag.combineOpFlags(thisOpFlags, u.combinedFlags);
            }
        }

        if (terminalFlags != 0) {
            // Apply flags from the terminal operation to last pipeline stage
            combinedFlags = StreamOpFlag.combineOpFlags(terminalFlags, combinedFlags);
        }
        return spliterator;
    }

    public static void positive(long number) {
        if (number < 0) {
            throw new IllegalArgumentException(Long.toString(number));
        }
    }

    @Override
    public final StreamShape getSourceShape() {
        @SuppressWarnings("rawtypes")
        AbstractPipeline p = AbstractPipeline.this;
        while (p.depth > 0) {
            p = p.previousStage;
        }
        return p.getOutputShape();
    }

    @Override
    public <Input> long exactOutputSizeIfKnown(Spliterator<Input> spliterator) {
        return StreamOpFlag.SIZED.isKnown(getStreamAndOpFlags()) ? spliterator.getExactSizeIfKnown() : -1;
    }

    @Override
    public <Input, S extends Sink<Output>> S wrapAndCopyInto(S sink, Spliterator<Input> spliterator) {
        copyInto(wrapSink(Objects.requireNonNull(sink)), spliterator);
        return sink;
    }

    @Override
    public <Input> void copyInto(Sink<Input> wrappedSink, Spliterator<Input> spliterator) {
        Objects.requireNonNull(wrappedSink);
        if (StreamOpFlag.SHORT_CIRCUIT.isKnown(getStreamAndOpFlags())) {
            copyIntoWithCancel(wrappedSink, spliterator);
        } else {
            wrappedSink.begin(spliterator.getExactSizeIfKnown());
            spliterator.forEachRemaining(wrappedSink);
            wrappedSink.end();
        }
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator) {
        AbstractPipeline pipeline = AbstractPipeline.this;
        while (pipeline.depth > 0) {
            pipeline = pipeline.previousStage;
        }
        wrappedSink.begin(spliterator.getExactSizeIfKnown());
        pipeline.forEachWithCancel(spliterator, wrappedSink);
        wrappedSink.end();
    }

    @Override
    public int getStreamAndOpFlags() {
        return combinedFlags;
    }

    public final boolean isOrdered() {
        return StreamOpFlag.ORDERED.isKnown(combinedFlags);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <P_IN> Sink<P_IN> wrapSink(Sink<Output> sink) {
        Objects.requireNonNull(sink);
        for (AbstractPipeline pipeline = AbstractPipeline.this; pipeline.depth > 0; pipeline = pipeline.previousStage) {
            sink = pipeline.opWrapSink(pipeline.previousStage.combinedFlags, sink);
        }

        return (Sink<P_IN>) sink;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <P_IN> Spliterator<Output> wrapSpliterator(Spliterator<P_IN> spliterator) {
        if (depth == 0) {
            return (Spliterator<Output>) spliterator;
        } else {
            return wrap(this, () -> spliterator, isParallel());
        }
    }

    @Override
    public <P_IN> Node<Output> evaluate(Spliterator<P_IN> spliterator, boolean flatten, IntFunction<Output[]> generator) {
        if (isParallel()) {
            return evaluateToNode(this, spliterator, flatten, generator);
        } else {
            NodeBuilder<Output> nb = makeNodeBuilder(
                    exactOutputSizeIfKnown(spliterator), generator);
            return wrapAndCopyInto(nb, spliterator).build();
        }

    }

    abstract <P_IN> Node<Output> evaluateToNode(PipelineHelper<Output> helper,
                                                Spliterator<P_IN> spliterator,
                                                boolean flattenTree,
                                                IntFunction<Output[]> generator);

    abstract <P_IN> Spliterator<Output> wrap(PipelineHelper<Output> ph,
                                             Supplier<Spliterator<P_IN>> supplier,
                                             boolean isParallel);


    abstract Spliterator<Output> lazySpliterator(Supplier<? extends Spliterator<Output>> supplier);


    abstract void forEachWithCancel(Spliterator<Output> spliterator, Sink<Output> sink);


    @Override
    public abstract NodeBuilder<Output> makeNodeBuilder(long exactSizeIfKnown,
                                                        IntFunction<Output[]> generator);


    // Op-specific abstract methods, implemented by the operation class


    abstract boolean opIsStateful();

    abstract StreamShape getOutputShape();

    public abstract Sink<Input> opWrapSink(int flags, Sink<Output> sink);


    protected <P_IN> Spliterator<Output> opEvaluateParallelLazy(PipelineHelper<Output> helper,
                                                                Spliterator<P_IN> spliterator) {
        return opEvaluateParallel(helper, spliterator, i -> (Output[]) new Object[i]).spliterator();
    }

    protected <P_IN> Node<Output> opEvaluateParallel(PipelineHelper<Output> helper,
                                                     Spliterator<P_IN> spliterator,
                                                     IntFunction<Output[]> generator) {
        throw new UnsupportedOperationException("Parallel evaluation is not supported");
    }

    final Node<Output> evaluateToArrayNode(IntFunction<Output[]> generator) {
        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        // If the last intermediate operation is stateful then
        // evaluate directly to avoid an extra collection step
        if (isParallel() && previousStage != null && opIsStateful()) {
            // Set the depth of this, last, pipeline stage to zero to slice the
            // pipeline such that this operation will not be included in the
            // upstream slice and upstream operations will not be included
            // in this slice
            depth = 0;
            return opEvaluateParallel(previousStage, previousStage.sourceSpliterator(0), generator);
        } else {
            return evaluate(sourceSpliterator(0), true, generator);
        }
    }

    final Spliterator<Output> sourceStageSpliterator() {
        if (this != sourceStage)
            throw new IllegalStateException();

        if (linkedOrConsumed)
            throw new IllegalStateException(MSG_STREAM_LINKED);
        linkedOrConsumed = true;

        if (sourceStage.sourceSpliterator != null) {
            @SuppressWarnings("unchecked")
            Spliterator<Output> s = sourceStage.sourceSpliterator;
            sourceStage.sourceSpliterator = null;
            return s;
        } else if (sourceStage.sourceSupplier != null) {
            @SuppressWarnings("unchecked")
            Spliterator<Output> s = (Spliterator<Output>) sourceStage.sourceSupplier.get();
            sourceStage.sourceSupplier = null;
            return s;
        } else {
            throw new IllegalStateException(MSG_CONSUMED);
        }
    }

    @Override
    public Stream sequential() {
        sourceStage.parallel = false;
        return (Stream) this;
    }

    @Override
    public Stream parallel() {
        sourceStage.parallel = true;
        return (Stream) this;
    }

    @Override
    public void close() {
        linkedOrConsumed = true;
        sourceSupplier = null;
        sourceSpliterator = null;
        if (sourceStage.sourceCloseAction != null) {
            Runnable closeAction = sourceStage.sourceCloseAction;
            sourceStage.sourceCloseAction = null;
            closeAction.run();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream onClose(Runnable closeHandler) {
        Objects.requireNonNull(closeHandler);

        Runnable sourceCloseAction = sourceStage.sourceCloseAction;

        sourceStage.sourceCloseAction = sourceCloseAction == null ? closeHandler : Streams.composeWithExceptions(sourceCloseAction, closeHandler);

        return (Stream) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Spliterator<Output> spliterator() {
        if (linkedOrConsumed) {
            throw new IllegalStateException(MSG_STREAM_LINKED);
        }

        linkedOrConsumed = true;
        if (this == sourceStage) {
            if (sourceStage.sourceSpliterator != null) {
                Spliterator<Output> spliterator = (Spliterator<Output>) sourceStage.sourceSpliterator;
                sourceStage.sourceSpliterator = null;
                return spliterator;
            } else if (sourceStage.sourceSupplier != null) {
                Supplier<Spliterator<Output>> supplier = (Supplier<Spliterator<Output>>) sourceStage.sourceSupplier;
                sourceStage.sourceSupplier = null;
                return lazySpliterator(supplier);
            } else {
                throw new IllegalStateException(MSG_STREAM_LINKED);
            }

        } else {
            return wrap(this, () -> sourceSpliterator(0), isParallel());
        }

    }

    @Override
    public boolean isParallel() {
        return sourceStage.parallel;
    }
}
