/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.platform;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ignite.IgniteException;
import org.apache.ignite.binary.BinaryType;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.CacheQueryExecutedEvent;
import org.apache.ignite.events.CacheQueryReadEvent;
import org.apache.ignite.events.CacheRebalancingEvent;
import org.apache.ignite.events.CheckpointEvent;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.events.Event;
import org.apache.ignite.events.EventAdapter;
import org.apache.ignite.events.EventType;
import org.apache.ignite.events.JobEvent;
import org.apache.ignite.events.TaskEvent;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.MarshallerPlatformIds;
import org.apache.ignite.internal.binary.BinaryContext;
import org.apache.ignite.internal.binary.BinaryMetadata;
import org.apache.ignite.internal.binary.BinaryReaderEx;
import org.apache.ignite.internal.binary.BinaryTypeImpl;
import org.apache.ignite.internal.binary.BinaryUtils;
import org.apache.ignite.internal.binary.BinaryWriterEx;
import org.apache.ignite.internal.binary.GridBinaryMarshaller;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.binary.CacheObjectBinaryProcessorImpl;
import org.apache.ignite.internal.processors.cache.distributed.dht.preloader.GridDhtPartitionsExchangeFuture;
import org.apache.ignite.internal.processors.cache.distributed.dht.preloader.PartitionsExchangeAware;
import org.apache.ignite.internal.processors.platform.cache.PlatformCacheEntryFilter;
import org.apache.ignite.internal.processors.platform.cache.PlatformCacheEntryFilterImpl;
import org.apache.ignite.internal.processors.platform.cache.PlatformCacheEntryProcessor;
import org.apache.ignite.internal.processors.platform.cache.PlatformCacheEntryProcessorImpl;
import org.apache.ignite.internal.processors.platform.cache.query.PlatformContinuousQuery;
import org.apache.ignite.internal.processors.platform.cache.query.PlatformContinuousQueryFilter;
import org.apache.ignite.internal.processors.platform.cache.query.PlatformContinuousQueryImpl;
import org.apache.ignite.internal.processors.platform.cache.query.PlatformContinuousQueryRemoteFilter;
import org.apache.ignite.internal.processors.platform.callback.PlatformCallbackGateway;
import org.apache.ignite.internal.processors.platform.cluster.PlatformClusterNodeFilter;
import org.apache.ignite.internal.processors.platform.cluster.PlatformClusterNodeFilterImpl;
import org.apache.ignite.internal.processors.platform.compute.PlatformAbstractTask;
import org.apache.ignite.internal.processors.platform.compute.PlatformClosureJob;
import org.apache.ignite.internal.processors.platform.compute.PlatformFullJob;
import org.apache.ignite.internal.processors.platform.compute.PlatformJob;
import org.apache.ignite.internal.processors.platform.datastreamer.PlatformStreamReceiver;
import org.apache.ignite.internal.processors.platform.datastreamer.PlatformStreamReceiverImpl;
import org.apache.ignite.internal.processors.platform.events.PlatformEventFilterListenerImpl;
import org.apache.ignite.internal.processors.platform.memory.PlatformInputStream;
import org.apache.ignite.internal.processors.platform.memory.PlatformMemory;
import org.apache.ignite.internal.processors.platform.memory.PlatformMemoryManager;
import org.apache.ignite.internal.processors.platform.memory.PlatformMemoryManagerImpl;
import org.apache.ignite.internal.processors.platform.memory.PlatformOutputStream;
import org.apache.ignite.internal.processors.platform.message.PlatformMessageFilter;
import org.apache.ignite.internal.processors.platform.messaging.PlatformMessageFilterImpl;
import org.apache.ignite.internal.processors.platform.utils.PlatformUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of platform context.
 */
@SuppressWarnings({"TypeMayBeWeakened", "rawtypes"})
public class PlatformContextImpl implements PlatformContext, PartitionsExchangeAware {
    /** Supported event types. */
    private static final Set<Integer> evtTyps;

    /** Whether to use thread-local data to update platform near cache. */
    private static final ThreadLocal<Boolean> platformCacheUpdateUseThreadLocal = new ThreadLocal<>();

    /** Kernal context. */
    private final GridKernalContext ctx;

    /** Marshaller. */
    private final GridBinaryMarshaller marsh;

    /** Memory manager. */
    private final PlatformMemoryManagerImpl mem;

    /** Callback gateway. */
    private final PlatformCallbackGateway gate;

    /** Cache object processor. */
    private final CacheObjectBinaryProcessorImpl cacheObjProc;

    /** Node ids that has been sent to native platform. */
    private final Set<UUID> sentNodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Platform name. */
    private final String platform;

    static {
        Set<Integer> evtTyps0 = new HashSet<>();

        addEventTypes(evtTyps0, EventType.EVTS_CACHE);
        addEventTypes(evtTyps0, EventType.EVTS_CACHE_QUERY);
        addEventTypes(evtTyps0, EventType.EVTS_CACHE_REBALANCE);
        addEventTypes(evtTyps0, EventType.EVTS_CHECKPOINT);
        addEventTypes(evtTyps0, EventType.EVTS_DISCOVERY_ALL);
        addEventTypes(evtTyps0, EventType.EVTS_JOB_EXECUTION);
        addEventTypes(evtTyps0, EventType.EVTS_TASK_EXECUTION);

        evtTyps = Collections.unmodifiableSet(evtTyps0);
    }

    /**
     * Adds all elements to a set.
     * @param set Set.
     * @param items Items.
     */
    private static void addEventTypes(Set<Integer> set, int[] items) {
        for (int i : items)
            set.add(i);
    }

    /**
     * Constructor.
     *
     * @param ctx Kernal context.
     * @param gate Callback gateway.
     * @param mem Memory manager.
     * @param platform Platform name.
     */
    public PlatformContextImpl(GridKernalContext ctx, PlatformCallbackGateway gate, PlatformMemoryManagerImpl mem,
        String platform) {
        this.ctx = ctx;
        this.gate = gate;
        this.mem = mem;
        this.platform = platform;

        cacheObjProc = (CacheObjectBinaryProcessorImpl)ctx.cacheObjects();

        marsh = cacheObjProc.marshaller();

        ctx.cache().context().exchange().registerExchangeAwareComponent(this);
    }

    /** {@inheritDoc} */
    @Override public GridKernalContext kernalContext() {
        return ctx;
    }

    /** {@inheritDoc} */
    @Override public PlatformMemoryManager memory() {
        return mem;
    }

    /** {@inheritDoc} */
    @Override public PlatformCallbackGateway gateway() {
        return gate;
    }

    /** {@inheritDoc} */
    @Override public BinaryReaderEx reader(PlatformMemory mem) {
        return reader(mem.input());
    }

    /** {@inheritDoc} */
    @Override public BinaryReaderEx reader(PlatformInputStream in) {
        return BinaryUtils.reader(marsh.context(),
            in,
            ctx.config().getClassLoader(),
            true,
            true);
    }

    /** {@inheritDoc} */
    @Override public BinaryWriterEx writer(PlatformMemory mem) {
        return writer(mem.output());
    }

    /** {@inheritDoc} */
    @Override public BinaryWriterEx writer(PlatformOutputStream out) {
        return marsh.writer(out);
    }

    /** {@inheritDoc} */
    @Override public void addNode(ClusterNode node) {
        if (node == null || sentNodes.contains(node.id()))
            return;

        // Send node info to the native platform
        try (PlatformMemory mem0 = mem.allocate()) {
            PlatformOutputStream out = mem0.output();

            BinaryWriterEx w = writer(out);

            w.writeUuid(node.id());
            PlatformUtils.writeNodeAttributes(w, node.attributes());
            w.writeCollection(node.addresses());
            w.writeCollection(node.hostNames());
            w.writeLong(node.order());
            w.writeBoolean(node.isLocal());
            w.writeBoolean(node.isClient());
            w.writeObjectDetached(node.consistentId());
            PlatformUtils.writeNodeVersion(w, node.version());

            writeClusterMetrics(w, node.metrics());

            out.synchronize();

            gateway().nodeInfo(mem0.pointer());
        }

        sentNodes.add(node.id());
    }

    /** {@inheritDoc} */
    @Override public void writeNode(BinaryWriterEx writer, ClusterNode node) {
        if (node == null) {
            writer.writeUuid(null);

            return;
        }

        addNode(node);

        writer.writeUuid(node.id());
    }

    /** {@inheritDoc} */
    @Override public void writeNodes(BinaryWriterEx writer, Collection<ClusterNode> nodes) {
        if (nodes == null) {
            writer.writeInt(-1);

            return;
        }

        writer.writeInt(nodes.size());

        for (ClusterNode n : nodes) {
            addNode(n);

            writer.writeUuid(n.id());
        }
    }

    /** {@inheritDoc} */
    @Override public void writeClusterMetrics(BinaryWriterEx writer, @Nullable ClusterMetrics metrics) {
        if (metrics == null)
            writer.writeBoolean(false);
        else {
            writer.writeBoolean(true);

            writer.writeLong(metrics.getLastUpdateTime());
            writer.writeTimestamp(new Timestamp(metrics.getLastUpdateTime()));
            writer.writeInt(metrics.getMaximumActiveJobs());
            writer.writeInt(metrics.getCurrentActiveJobs());
            writer.writeFloat(metrics.getAverageActiveJobs());
            writer.writeInt(metrics.getMaximumWaitingJobs());

            writer.writeInt(metrics.getCurrentWaitingJobs());
            writer.writeFloat(metrics.getAverageWaitingJobs());
            writer.writeInt(metrics.getMaximumRejectedJobs());
            writer.writeInt(metrics.getCurrentRejectedJobs());
            writer.writeFloat(metrics.getAverageRejectedJobs());

            writer.writeInt(metrics.getTotalRejectedJobs());
            writer.writeInt(metrics.getMaximumCancelledJobs());
            writer.writeInt(metrics.getCurrentCancelledJobs());
            writer.writeFloat(metrics.getAverageCancelledJobs());
            writer.writeInt(metrics.getTotalCancelledJobs());

            writer.writeInt(metrics.getTotalExecutedJobs());
            writer.writeLong(metrics.getMaximumJobWaitTime());
            writer.writeLong(metrics.getCurrentJobWaitTime());
            writer.writeDouble(metrics.getAverageJobWaitTime());
            writer.writeLong(metrics.getMaximumJobExecuteTime());

            writer.writeLong(metrics.getCurrentJobExecuteTime());
            writer.writeDouble(metrics.getAverageJobExecuteTime());
            writer.writeInt(metrics.getTotalExecutedTasks());
            writer.writeLong(metrics.getTotalIdleTime());
            writer.writeLong(metrics.getCurrentIdleTime());

            writer.writeInt(metrics.getTotalCpus());
            writer.writeDouble(metrics.getCurrentCpuLoad());
            writer.writeDouble(metrics.getAverageCpuLoad());
            writer.writeDouble(metrics.getCurrentGcCpuLoad());
            writer.writeLong(metrics.getHeapMemoryInitialized());

            writer.writeLong(metrics.getHeapMemoryUsed());
            writer.writeLong(metrics.getHeapMemoryCommitted());
            writer.writeLong(metrics.getHeapMemoryMaximum());
            writer.writeLong(metrics.getHeapMemoryTotal());
            writer.writeLong(metrics.getNonHeapMemoryInitialized());

            writer.writeLong(metrics.getNonHeapMemoryUsed());
            writer.writeLong(metrics.getNonHeapMemoryCommitted());
            writer.writeLong(metrics.getNonHeapMemoryMaximum());
            writer.writeLong(metrics.getNonHeapMemoryTotal());
            writer.writeLong(metrics.getUpTime());

            writer.writeTimestamp(new Timestamp(metrics.getStartTime()));
            writer.writeTimestamp(new Timestamp(metrics.getNodeStartTime()));
            writer.writeInt(metrics.getCurrentThreadCount());
            writer.writeInt(metrics.getMaximumThreadCount());
            writer.writeLong(metrics.getTotalStartedThreadCount());

            writer.writeInt(metrics.getCurrentDaemonThreadCount());
            writer.writeLong(metrics.getLastDataVersion());
            writer.writeInt(metrics.getSentMessagesCount());
            writer.writeLong(metrics.getSentBytesCount());
            writer.writeInt(metrics.getReceivedMessagesCount());

            writer.writeLong(metrics.getReceivedBytesCount());
            writer.writeInt(metrics.getOutboundMessagesQueueSize());

            writer.writeInt(metrics.getTotalNodes());
        }
    }

    /** {@inheritDoc} */
    @Override public void processMetadata(BinaryReaderEx reader) {
        Collection<BinaryMetadata> metas = PlatformUtils.readBinaryMetadataCollection(reader);

        BinaryContext binCtx = cacheObjProc.binaryContext();

        for (BinaryMetadata meta : metas)
            binCtx.updateMetadata(meta.typeId(), meta, false);
    }

    /** {@inheritDoc} */
    @Override public void writeMetadata(BinaryWriterEx writer, int typeId, boolean includeSchemas) {
        writeMetadata0(writer, cacheObjProc.metadata(typeId), includeSchemas);
    }

    /** {@inheritDoc} */
    @Override public void writeAllMetadata(BinaryWriterEx writer) {
        Collection<BinaryType> metas = cacheObjProc.metadata();

        writer.writeInt(metas.size());

        for (BinaryType m : metas)
            writeMetadata0(writer, m, false);
    }

    /** {@inheritDoc} */
    @Override public void writeSchema(BinaryWriterEx writer, int typeId, int schemaId) {
        writer.writeIntArray(BinaryUtils.getSchema(cacheObjProc, typeId, schemaId));
    }

    /**
     * Write binary metadata.
     *
     * @param writer Writer.
     * @param meta Metadata.
     */
    private void writeMetadata0(BinaryWriterEx writer, BinaryType meta, boolean includeSchemas) {
        if (meta == null)
            writer.writeBoolean(false);
        else {
            writer.writeBoolean(true);

            BinaryMetadata meta0 = ((BinaryTypeImpl)meta).metadata();

            PlatformUtils.writeBinaryMetadata(writer, meta0, includeSchemas);
        }
    }

    /** {@inheritDoc} */
    @Override public PlatformContinuousQuery createContinuousQuery(long ptr, boolean hasFilter,
        @Nullable Object filter) {
        return new PlatformContinuousQueryImpl(this, ptr, hasFilter, filter);
    }

    /** {@inheritDoc} */
    @Override public PlatformContinuousQueryFilter createContinuousQueryFilter(Object filter) {
        return new PlatformContinuousQueryRemoteFilter(filter);
    }

    /** {@inheritDoc} */
    @Override public PlatformMessageFilter createRemoteMessageFilter(Object filter, long ptr) {
        return new PlatformMessageFilterImpl(filter, ptr, this);
    }

    /** {@inheritDoc} */
    @Override public boolean isEventTypeSupported(int evtTyp) {
        return evtTyps.contains(evtTyp);
    }

    /** {@inheritDoc} */
    @Override public void writeEvent(BinaryWriterEx writer, Event evt) {
        assert writer != null;

        if (evt == null) {
            writer.writeInt(-1);

            return;
        }

        EventAdapter evt0 = (EventAdapter)evt;

        if (evt0 instanceof CacheEvent) {
            writer.writeInt(2);
            writeCommonEventData(writer, evt0);

            CacheEvent event0 = (CacheEvent)evt0;

            writer.writeString(event0.cacheName());
            writer.writeInt(event0.partition());
            writer.writeBoolean(event0.isNear());
            writeNode(writer, event0.eventNode());
            writer.writeObject(event0.key());
            writer.writeObject(event0.xid());
            writer.writeObject(event0.newValue());
            writer.writeObject(event0.oldValue());
            writer.writeBoolean(event0.hasOldValue());
            writer.writeBoolean(event0.hasNewValue());
            writer.writeUuid(event0.subjectId());
            writer.writeString(event0.closureClassName());
            writer.writeString(event0.taskName());
        }
        else if (evt0 instanceof CacheQueryExecutedEvent) {
            writer.writeInt(3);
            writeCommonEventData(writer, evt0);

            CacheQueryExecutedEvent event0 = (CacheQueryExecutedEvent)evt0;

            writer.writeString(event0.queryType());
            writer.writeString(event0.cacheName());
            writer.writeString(event0.className());
            writer.writeString(event0.clause());
            writer.writeUuid(event0.subjectId());
            writer.writeString(event0.taskName());
        }
        else if (evt0 instanceof CacheQueryReadEvent) {
            writer.writeInt(4);
            writeCommonEventData(writer, evt0);

            CacheQueryReadEvent event0 = (CacheQueryReadEvent)evt0;

            writer.writeString(event0.queryType());
            writer.writeString(event0.cacheName());
            writer.writeString(event0.className());
            writer.writeString(event0.clause());
            writer.writeUuid(event0.subjectId());
            writer.writeString(event0.taskName());
            writer.writeObject(event0.key());
            writer.writeObject(event0.value());
            writer.writeObject(event0.oldValue());
            writer.writeObject(event0.row());
        }
        else if (evt0 instanceof CacheRebalancingEvent) {
            writer.writeInt(5);
            writeCommonEventData(writer, evt0);

            CacheRebalancingEvent event0 = (CacheRebalancingEvent)evt0;

            writer.writeString(event0.cacheName());
            writer.writeInt(event0.partition());
            writeNode(writer, event0.discoveryNode());
            writer.writeInt(event0.discoveryEventType());
            writer.writeString(event0.discoveryEventName());
            writer.writeLong(event0.discoveryTimestamp());
        }
        else if (evt0 instanceof CheckpointEvent) {
            writer.writeInt(6);
            writeCommonEventData(writer, evt0);

            CheckpointEvent event0 = (CheckpointEvent)evt0;

            writer.writeString(event0.key());
        }
        else if (evt0 instanceof DiscoveryEvent) {
            writer.writeInt(7);
            writeCommonEventData(writer, evt0);

            DiscoveryEvent event0 = (DiscoveryEvent)evt0;

            writeNode(writer, event0.eventNode());
            writer.writeLong(event0.topologyVersion());

            writeNodes(writer, event0.topologyNodes());
        }
        else if (evt0 instanceof JobEvent) {
            writer.writeInt(8);
            writeCommonEventData(writer, evt0);

            JobEvent event0 = (JobEvent)evt0;

            writer.writeString(event0.taskName());
            writer.writeString(event0.taskClassName());
            writer.writeObject(event0.taskSessionId());
            writer.writeObject(event0.jobId());
            writeNode(writer, event0.taskNode());
            writer.writeUuid(event0.taskSubjectId());
        }
        else if (evt0 instanceof TaskEvent) {
            writer.writeInt(10);
            writeCommonEventData(writer, evt0);

            TaskEvent event0 = (TaskEvent)evt0;

            writer.writeString(event0.taskName());
            writer.writeString(event0.taskClassName());
            writer.writeObject(event0.taskSessionId());
            writer.writeBoolean(event0.internal());
            writer.writeUuid(event0.subjectId());
        }
        else
            throw new IgniteException("Unsupported event: " + evt);
    }

    /**
     * Write common event data.
     *
     * @param writer Writer.
     * @param evt Event.
     */
    private void writeCommonEventData(BinaryWriterEx writer, EventAdapter evt) {
        writer.writeObject(evt.id());
        writer.writeLong(evt.localOrder());
        writeNode(writer, evt.node());
        writer.writeString(evt.message());
        writer.writeInt(evt.type());
        writer.writeString(evt.name());
        writer.writeTimestamp(new Timestamp(evt.timestamp()));
    }

    /** {@inheritDoc} */
    @Override public PlatformEventFilterListener createLocalEventFilter(long hnd) {
        return new PlatformEventFilterListenerImpl(hnd, this);
    }

    /** {@inheritDoc} */
    @Override public PlatformEventFilterListener createRemoteEventFilter(Object pred, int... types) {
        return new PlatformEventFilterListenerImpl(pred, types);
    }

    /** {@inheritDoc} */
    @Override public PlatformNativeException createNativeException(Object cause) {
        return new PlatformNativeException(cause);
    }

    /** {@inheritDoc} */
    @Override public PlatformJob createJob(Object task, long ptr, @Nullable Object job, String jobName) {
        return new PlatformFullJob(this, (PlatformAbstractTask)task, ptr, job, jobName);
    }

    /** {@inheritDoc} */
    @Override public PlatformJob createClosureJob(Object task, long ptr, Object job, String jobName) {
        return new PlatformClosureJob((PlatformAbstractTask)task, ptr, job, jobName);
    }

    /** {@inheritDoc} */
    @Override public PlatformCacheEntryProcessor createCacheEntryProcessor(Object proc, long ptr) {
        return new PlatformCacheEntryProcessorImpl(proc, ptr);
    }

    /** {@inheritDoc} */
    @Override public PlatformCacheEntryFilter createCacheEntryFilter(Object filter, long ptr) {
        return new PlatformCacheEntryFilterImpl(filter, ptr, this);
    }

    /** {@inheritDoc} */
    @Override public PlatformStreamReceiver createStreamReceiver(Object rcv, long ptr, boolean keepBinary) {
        return new PlatformStreamReceiverImpl(rcv, ptr, keepBinary, this);
    }

    /** {@inheritDoc} */
    @Override public PlatformClusterNodeFilter createClusterNodeFilter(Object filter) {
        return new PlatformClusterNodeFilterImpl(filter, this);
    }

    /** {@inheritDoc} */
    @Override public String platform() {
        return platform;
    }

    /** {@inheritDoc} */
    @Override public boolean isPlatformCacheSupported() {
        return platform.equals(PlatformUtils.PLATFORM_DOTNET);
    }

    /** {@inheritDoc} */
    @Override public void updatePlatformCache(int cacheId, byte[] keyBytes, byte[] valBytes,
                                              int part, AffinityTopologyVersion ver) {
        if (!isPlatformCacheSupported())
            return;

        Boolean useTls = platformCacheUpdateUseThreadLocal.get();
        if (useTls != null && useTls) {
            long cacheIdAndPartition = ((long)part << 32) | (0xFFFFFFFFL & cacheId);

            gateway().platformCacheUpdateFromThreadLocal(
                    cacheIdAndPartition, ver.topologyVersion(), ver.minorTopologyVersion());

            return;
        }

        assert keyBytes != null;
        assert part >= 0;

        try (PlatformMemory mem0 = mem.allocate()) {
            PlatformOutputStream out = mem0.output();

            out.writeInt(cacheId);
            out.writeByteArray(keyBytes);

            if (valBytes != null) {
                out.writeBoolean(true);
                out.writeByteArray(valBytes);

                assert ver != null;

                out.writeInt(part);
                out.writeLong(ver.topologyVersion());
                out.writeInt(ver.minorTopologyVersion());
            }
            else {
                out.writeBoolean(false);
            }

            out.synchronize();

            gateway().platformCacheUpdate(mem0.pointer());
        }
    }

    /** {@inheritDoc} */
    @Override public void enableThreadLocalForPlatformCacheUpdate() {
        platformCacheUpdateUseThreadLocal.set(true);
    }

    /** {@inheritDoc} */
    @Override public void disableThreadLocalForPlatformCacheUpdate() {
        platformCacheUpdateUseThreadLocal.set(false);
    }

    /** {@inheritDoc} */
    @Override public @Nullable BinaryMetadata getBinaryType(String typeName) {
        try (PlatformMemory mem0 = mem.allocate()) {
            PlatformOutputStream out = mem0.output();
            BinaryWriterEx writer = writer(out);

            writer.writeString(typeName);
            out.synchronize();

            if (gateway().binaryTypeGet(mem0.pointer()) == 0)
                return null;

            PlatformInputStream in = mem0.input();
            in.synchronize();

            return PlatformUtils.readBinaryMetadata(reader(in));
        }
    }

    /** {@inheritDoc} */
    @Override public byte getMarshallerPlatformId() {
        // Only .NET has a specific marshaller ID, C++ does not have it.
        return platform.equals(PlatformUtils.PLATFORM_DOTNET)
                ? MarshallerPlatformIds.DOTNET_ID
                : MarshallerPlatformIds.JAVA_ID;
    }

    /** {@inheritDoc} */
    @Override public void onDoneBeforeTopologyUnlock(GridDhtPartitionsExchangeFuture fut) {
        AffinityTopologyVersion ver = fut.topologyVersion();

        if (ver != null) {
            gateway().onAffinityTopologyVersionChanged(ver);
        }
    }
}
