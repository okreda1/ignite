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

package org.apache.ignite.internal.processors.cache;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import org.apache.ignite.cache.CacheMetrics;
import org.apache.ignite.internal.dto.IgniteDataTransferObject;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 * Metrics snapshot.
 */
public class CacheMetricsSnapshot extends IgniteDataTransferObject implements CacheMetrics {
    /** */
    private static final long serialVersionUID = 0L;

    /** Number of reads. */
    private long reads;

    /** Number of puts. */
    private long puts;

    /** Number of invokes caused updates. */
    private long entryProcessorPuts;

    /** Number of invokes caused no updates. */
    private long entryProcessorReadOnlyInvocations;

    /**
     * The mean time to execute cache invokes
     */
    private float entryProcessorAverageInvocationTime;

    /**
     * The total number of cache invocations.
     */
    private long entryProcessorInvocations;

    /**
     * The total number of cache invocations, caused removal.
     */
    private long entryProcessorRemovals;

    /**
     * The total number of invocations on keys, which don't exist in cache.
     */
    private long entryProcessorMisses;

    /**
     * The total number of invocations on keys, which exist in cache.
     */
    private long entryProcessorHits;

    /**
     * The percentage of invocations on keys, which don't exist in cache.
     */
    private float entryProcessorMissPercentage;

    /**
     * The percentage of invocations on keys, which exist in cache.
     */
    private float entryProcessorHitPercentage;

    /**
     * So far, the maximum time to execute cache invokes.
     */
    private float entryProcessorMaxInvocationTime;

    /**
     * So far, the minimum time to execute cache invokes.
     */
    private float entryProcessorMinInvocationTime;

    /** Number of hits. */
    private long hits;

    /** Number of misses. */
    private long misses;

    /** Number of transaction commits. */
    private long txCommits;

    /** Number of transaction rollbacks. */
    private long txRollbacks;

    /** Number of evictions. */
    private long evicts;

    /** Number of removed entries. */
    private long removes;

    /** Put time taken nanos. */
    private float putAvgTimeNanos;

    /** Get time taken nanos. */
    private float getAvgTimeNanos;

    /** Remove time taken nanos. */
    private float rmvAvgTimeNanos;

    /** Commit transaction time taken nanos. */
    private float commitAvgTimeNanos;

    /** Commit transaction time taken nanos. */
    private float rollbackAvgTimeNanos;

    /** Cache name */
    private String cacheName;

    /** Number of reads from off-heap. */
    private long offHeapGets;

    /** Number of writes to off-heap. */
    private long offHeapPuts;

    /** Number of removed entries from off-heap. */
    private long offHeapRemoves;

    /** Number of evictions from off-heap. */
    private long offHeapEvicts;

    /** Off-heap hits number. */
    private long offHeapHits;

    /** Off-heap misses number. */
    private long offHeapMisses;

    /** Number of entries stored in off-heap memory. */
    private long offHeapEntriesCnt;

    /** Number of entries stored in heap. */
    private long heapEntriesCnt;

    /** Number of primary entries stored in off-heap memory. */
    private long offHeapPrimaryEntriesCnt;

    /** Number of backup entries stored in off-heap memory. */
    private long offHeapBackupEntriesCnt;

    /** Memory size allocated in off-heap. */
    private long offHeapAllocatedSize;

    /** Number of non-{@code null} values in the cache. */
    private int size;

    /** Cache size. */
    private long cacheSize;

    /** Number of keys in the cache, possibly with {@code null} values. */
    private int keySize;

    /** Cache is empty. */
    private boolean isEmpty;

    /** Gets current size of evict queue used to batch up evictions. */
    private int dhtEvictQueueCurrSize;

    /** Transaction per-thread map size. */
    private int txThreadMapSize;

    /** Transaction per-Xid map size. */
    private int txXidMapSize;

    /** Committed transaction queue size. */
    private int txCommitQueueSize;

    /** Prepared transaction queue size. */
    private int txPrepareQueueSize;

    /** Start version counts map size. */
    private int txStartVerCountsSize;

    /** Number of cached committed transaction IDs. */
    private int txCommittedVersionsSize;

    /** Number of cached rolled back transaction IDs. */
    private int txRolledbackVersionsSize;

    /** DHT thread map size. */
    private int txDhtThreadMapSize;

    /** Transaction DHT per-Xid map size. */
    private int txDhtXidMapSize;

    /** Committed DHT transaction queue size. */
    private int txDhtCommitQueueSize;

    /** Prepared DHT transaction queue size. */
    private int txDhtPrepareQueueSize;

    /** DHT start version counts map size. */
    private int txDhtStartVerCountsSize;

    /** Number of cached committed DHT transaction IDs. */
    private int txDhtCommittedVersionsSize;

    /** Number of cached rolled back DHT transaction IDs. */
    private int txDhtRolledbackVersionsSize;

    /** Write-behind is enabled. */
    private boolean isWriteBehindEnabled;

    /** Buffer size that triggers flush procedure. */
    private int writeBehindFlushSize;

    /** Count of worker threads. */
    private int writeBehindFlushThreadCnt;

    /** Flush frequency in milliseconds. */
    private long writeBehindFlushFreq;

    /** Maximum size of batch. */
    private int writeBehindStoreBatchSize;

    /** Count of cache overflow events since start. */
    private int writeBehindTotalCriticalOverflowCnt;

    /** Count of cache overflow events since start. */
    private int writeBehindCriticalOverflowCnt;

    /** Count of entries in store-retry state. */
    private int writeBehindErrorRetryCnt;

    /** Total count of entries in cache store internal buffer. */
    private int writeBehindBufSize;

    /** Total partitions count. */
    private int totalPartitionsCnt;

    /** Rebalancing partitions count. */
    private int rebalancingPartitionsCnt;

    /** Number of already rebalanced keys. */
    private long rebalancedKeys;

    /** Number estimated to rebalance keys. */
    private long estimatedRebalancingKeys;

    /** Keys to rebalance left. */
    private long keysToRebalanceLeft;

    /** Rebalancing keys rate. */
    private long rebalancingKeysRate;

    /** Get rebalancing bytes rate. */
    private long rebalancingBytesRate;

    /** Start rebalance time. */
    private long rebalanceStartTime;

    /** Estimate rebalance finish time. */
    private long rebalanceFinishTime;

    /** The number of clearing partitions need to await before rebalance. */
    private long rebalanceClearingPartitionsLeft;

    /** */
    private String keyType;

    /** */
    private String valType;

    /** */
    private boolean isStoreByVal;

    /** */
    private boolean isStatisticsEnabled;

    /** */
    private boolean isManagementEnabled;

    /** */
    private boolean isReadThrough;

    /** */
    private boolean isWriteThrough;

    /** */
    private boolean isValidForReading;

    /** */
    private boolean isValidForWriting;

    /** Tx key collisions with appropriate queue size string representation. */
    private String txKeyCollisions;

    /** Index rebuilding in progress. */
    private boolean idxRebuildInProgress;

    /** Number of keys processed during index rebuilding. */
    private long idxRebuildKeyProcessed;

    /** The number of local node partitions that remain to be processed to complete indexing. */
    private int idxBuildPartitionsLeftCount;

    /**
     * Default constructor.
     */
    public CacheMetricsSnapshot() {
        // No-op.
    }

    /**
     * Create snapshot for given metrics.
     *
     * @param m Cache metrics.
     */
    public CacheMetricsSnapshot(CacheMetricsImpl m) {
        reads = m.getCacheGets();
        puts = m.getCachePuts();
        hits = m.getCacheHits();
        misses = m.getCacheMisses();
        txCommits = m.getCacheTxCommits();
        txRollbacks = m.getCacheTxRollbacks();
        evicts = m.getCacheEvictions();
        removes = m.getCacheRemovals();

        entryProcessorPuts = m.getEntryProcessorPuts();
        entryProcessorReadOnlyInvocations = m.getEntryProcessorReadOnlyInvocations();
        entryProcessorInvocations = m.getEntryProcessorInvocations();
        entryProcessorRemovals = m.getEntryProcessorRemovals();
        entryProcessorMisses = m.getEntryProcessorMisses();
        entryProcessorHits = m.getEntryProcessorHits();
        entryProcessorMissPercentage = m.getEntryProcessorMissPercentage();
        entryProcessorHitPercentage = m.getEntryProcessorHitPercentage();
        entryProcessorAverageInvocationTime = m.getEntryProcessorAverageInvocationTime();
        entryProcessorMaxInvocationTime = m.getEntryProcessorMaxInvocationTime();
        entryProcessorMinInvocationTime = m.getEntryProcessorMinInvocationTime();

        putAvgTimeNanos = m.getAveragePutTime();
        getAvgTimeNanos = m.getAverageGetTime();
        rmvAvgTimeNanos = m.getAverageRemoveTime();
        commitAvgTimeNanos = m.getAverageTxCommitTime();
        rollbackAvgTimeNanos = m.getAverageTxRollbackTime();

        cacheName = m.name();

        offHeapGets = m.getOffHeapGets();
        offHeapPuts = m.getOffHeapPuts();
        offHeapRemoves = m.getOffHeapRemovals();
        offHeapEvicts = m.getOffHeapEvictions();
        offHeapHits = m.getOffHeapHits();
        offHeapMisses = m.getOffHeapMisses();

        CacheMetricsImpl.EntriesStatMetrics entriesStat = m.getEntriesStat();

        offHeapEntriesCnt = entriesStat.offHeapEntriesCount();
        heapEntriesCnt = entriesStat.heapEntriesCount();
        offHeapPrimaryEntriesCnt = entriesStat.offHeapPrimaryEntriesCount();
        offHeapBackupEntriesCnt = entriesStat.offHeapBackupEntriesCount();

        offHeapAllocatedSize = m.getOffHeapAllocatedSize();

        cacheSize = entriesStat.cacheSize();
        keySize = entriesStat.keySize();
        size = entriesStat.size();
        isEmpty = entriesStat.isEmpty();

        dhtEvictQueueCurrSize = m.getDhtEvictQueueCurrentSize();
        txThreadMapSize = m.getTxThreadMapSize();
        txXidMapSize = m.getTxXidMapSize();
        txCommitQueueSize = m.getTxCommitQueueSize();
        txPrepareQueueSize = m.getTxPrepareQueueSize();
        txStartVerCountsSize = m.getTxStartVersionCountsSize();
        txCommittedVersionsSize = m.getTxCommittedVersionsSize();
        txRolledbackVersionsSize = m.getTxRolledbackVersionsSize();
        txDhtThreadMapSize = m.getTxDhtThreadMapSize();
        txDhtXidMapSize = m.getTxDhtXidMapSize();
        txDhtCommitQueueSize = m.getTxDhtCommitQueueSize();
        txDhtPrepareQueueSize = m.getTxDhtPrepareQueueSize();
        txDhtStartVerCountsSize = m.getTxDhtStartVersionCountsSize();
        txDhtCommittedVersionsSize = m.getTxDhtCommittedVersionsSize();
        txDhtRolledbackVersionsSize = m.getTxDhtRolledbackVersionsSize();
        isWriteBehindEnabled = m.isWriteBehindEnabled();
        writeBehindFlushSize = m.getWriteBehindFlushSize();
        writeBehindFlushThreadCnt = m.getWriteBehindFlushThreadCount();
        writeBehindFlushFreq = m.getWriteBehindFlushFrequency();
        writeBehindStoreBatchSize = m.getWriteBehindStoreBatchSize();
        writeBehindTotalCriticalOverflowCnt = m.getWriteBehindTotalCriticalOverflowCount();
        writeBehindCriticalOverflowCnt = m.getWriteBehindCriticalOverflowCount();
        writeBehindErrorRetryCnt = m.getWriteBehindErrorRetryCount();
        writeBehindBufSize = m.getWriteBehindBufferSize();

        keyType = m.getKeyType();
        valType = m.getValueType();
        isStoreByVal = m.isStoreByValue();
        isStatisticsEnabled = m.isStatisticsEnabled();
        isManagementEnabled = m.isManagementEnabled();
        isReadThrough = m.isReadThrough();
        isWriteThrough = m.isWriteThrough();
        isValidForReading = m.isValidForReading();
        isValidForWriting = m.isValidForWriting();

        totalPartitionsCnt = entriesStat.totalPartitionsCount();
        rebalancingPartitionsCnt = entriesStat.rebalancingPartitionsCount();

        rebalancedKeys = m.getRebalancedKeys();
        estimatedRebalancingKeys = m.getEstimatedRebalancingKeys();
        keysToRebalanceLeft = m.getKeysToRebalanceLeft();
        rebalancingBytesRate = m.getRebalancingBytesRate();
        rebalancingKeysRate = m.getRebalancingKeysRate();
        rebalanceStartTime = m.rebalancingStartTime();
        rebalanceFinishTime = m.estimateRebalancingFinishTime();
        rebalanceClearingPartitionsLeft = m.getRebalanceClearingPartitionsLeft();
        txKeyCollisions = m.getTxKeyCollisions();

        idxRebuildInProgress = m.isIndexRebuildInProgress();
        idxRebuildKeyProcessed = m.getIndexRebuildKeysProcessed();

        idxBuildPartitionsLeftCount = m.getIndexBuildPartitionsLeftCount();
    }

    /**
     * Constructs merged cache metrics.
     *
     * @param loc Metrics for cache on local node.
     * @param metrics Metrics for merge.
     */
    public CacheMetricsSnapshot(CacheMetrics loc, Collection<CacheMetrics> metrics) {
        cacheName = loc.name();
        isEmpty = loc.isEmpty();
        isWriteBehindEnabled = loc.isWriteBehindEnabled();
        writeBehindFlushSize = loc.getWriteBehindFlushSize();
        writeBehindFlushThreadCnt = loc.getWriteBehindFlushThreadCount();
        writeBehindFlushFreq = loc.getWriteBehindFlushFrequency();
        writeBehindStoreBatchSize = loc.getWriteBehindStoreBatchSize();
        writeBehindBufSize = loc.getWriteBehindBufferSize();
        size = 0;
        cacheSize = 0;
        keySize = 0;

        keyType = loc.getKeyType();
        valType = loc.getValueType();
        isStoreByVal = loc.isStoreByValue();
        isStatisticsEnabled = loc.isStatisticsEnabled();
        isManagementEnabled = loc.isManagementEnabled();
        isReadThrough = loc.isReadThrough();
        isWriteThrough = loc.isWriteThrough();
        isValidForReading = loc.isValidForReading();
        isValidForWriting = loc.isValidForWriting();

        for (CacheMetrics e : metrics) {
            reads += e.getCacheGets();
            puts += e.getCachePuts();
            size += e.getSize();
            keySize += e.getKeySize();
            cacheSize += e.getCacheSize();
            isEmpty &= e.isEmpty();
            hits += e.getCacheHits();
            misses += e.getCacheMisses();
            txCommits += e.getCacheTxCommits();
            txRollbacks += e.getCacheTxRollbacks();
            evicts += e.getCacheEvictions();
            removes += e.getCacheRemovals();

            entryProcessorPuts = e.getEntryProcessorPuts();
            entryProcessorReadOnlyInvocations = e.getEntryProcessorReadOnlyInvocations();
            entryProcessorInvocations = e.getEntryProcessorInvocations();
            entryProcessorRemovals = e.getEntryProcessorRemovals();
            entryProcessorMisses = e.getEntryProcessorMisses();
            entryProcessorHits = e.getEntryProcessorHits();
            entryProcessorMissPercentage = e.getEntryProcessorMissPercentage();
            entryProcessorHitPercentage = e.getEntryProcessorHitPercentage();
            entryProcessorAverageInvocationTime = e.getEntryProcessorAverageInvocationTime();
            entryProcessorMaxInvocationTime = e.getEntryProcessorMaxInvocationTime();
            entryProcessorMinInvocationTime = e.getEntryProcessorMinInvocationTime();

            putAvgTimeNanos += e.getAveragePutTime();
            getAvgTimeNanos += e.getAverageGetTime();
            rmvAvgTimeNanos += e.getAverageRemoveTime();
            commitAvgTimeNanos += e.getAverageTxCommitTime();
            rollbackAvgTimeNanos += e.getAverageTxRollbackTime();

            offHeapGets += e.getOffHeapGets();
            offHeapPuts += e.getOffHeapPuts();
            offHeapRemoves += e.getOffHeapRemovals();
            offHeapEvicts += e.getOffHeapEvictions();
            offHeapHits += e.getOffHeapHits();
            offHeapMisses += e.getOffHeapMisses();
            offHeapEntriesCnt += e.getOffHeapEntriesCount();
            heapEntriesCnt += e.getHeapEntriesCount();
            offHeapPrimaryEntriesCnt += e.getOffHeapPrimaryEntriesCount();
            offHeapBackupEntriesCnt += e.getOffHeapBackupEntriesCount();
            offHeapAllocatedSize += e.getOffHeapAllocatedSize();

            if (e.getDhtEvictQueueCurrentSize() > -1)
                dhtEvictQueueCurrSize += e.getDhtEvictQueueCurrentSize();
            else
                dhtEvictQueueCurrSize = -1;

            txThreadMapSize += e.getTxThreadMapSize();
            txXidMapSize += e.getTxXidMapSize();
            txCommitQueueSize += e.getTxCommitQueueSize();
            txPrepareQueueSize += e.getTxPrepareQueueSize();
            txStartVerCountsSize += e.getTxStartVersionCountsSize();
            txCommittedVersionsSize += e.getTxCommittedVersionsSize();
            txRolledbackVersionsSize += e.getTxRolledbackVersionsSize();

            if (e.getTxDhtThreadMapSize() > -1)
                txDhtThreadMapSize += e.getTxDhtThreadMapSize();
            else
                txDhtThreadMapSize = -1;

            if (e.getTxDhtXidMapSize() > -1)
                txDhtXidMapSize += e.getTxDhtXidMapSize();
            else
                txDhtXidMapSize = -1;

            if (e.getTxDhtCommitQueueSize() > -1)
                txDhtCommitQueueSize += e.getTxDhtCommitQueueSize();
            else
                txDhtCommitQueueSize = -1;

            if (e.getTxDhtPrepareQueueSize() > -1)
                txDhtPrepareQueueSize += e.getTxDhtPrepareQueueSize();
            else
                txDhtPrepareQueueSize = -1;

            if (e.getTxDhtStartVersionCountsSize() > -1)
                txDhtStartVerCountsSize += e.getTxDhtStartVersionCountsSize();
            else
                txDhtStartVerCountsSize = -1;

            if (e.getTxDhtCommittedVersionsSize() > -1)
                txDhtCommittedVersionsSize += e.getTxDhtCommittedVersionsSize();
            else
                txDhtCommittedVersionsSize = -1;

            if (e.getTxDhtRolledbackVersionsSize() > -1)
                txDhtRolledbackVersionsSize += e.getTxDhtRolledbackVersionsSize();
            else
                txDhtRolledbackVersionsSize = -1;

            if (e.getWriteBehindTotalCriticalOverflowCount() > -1)
                writeBehindTotalCriticalOverflowCnt += e.getWriteBehindTotalCriticalOverflowCount();
            else
                writeBehindTotalCriticalOverflowCnt = -1;

            if (e.getWriteBehindCriticalOverflowCount() > -1)
                writeBehindCriticalOverflowCnt += e.getWriteBehindCriticalOverflowCount();
            else
                writeBehindCriticalOverflowCnt = -1;

            if (e.getWriteBehindErrorRetryCount() > -1)
                writeBehindErrorRetryCnt += e.getWriteBehindErrorRetryCount();
            else
                writeBehindErrorRetryCnt = -1;

            rebalancedKeys += e.getRebalancedKeys();
            estimatedRebalancingKeys += e.getEstimatedRebalancingKeys();
            totalPartitionsCnt += e.getTotalPartitionsCount();
            rebalancingPartitionsCnt += e.getRebalancingPartitionsCount();
            keysToRebalanceLeft += e.getKeysToRebalanceLeft();
            rebalancingBytesRate += e.getRebalancingBytesRate();
            rebalancingKeysRate += e.getRebalancingKeysRate();
            idxBuildPartitionsLeftCount += e.getIndexBuildPartitionsLeftCount();
        }

        int size = metrics.size();

        if (size > 1) {
            putAvgTimeNanos /= size;
            getAvgTimeNanos /= size;
            rmvAvgTimeNanos /= size;
            commitAvgTimeNanos /= size;
            rollbackAvgTimeNanos /= size;
        }
    }

    /** {@inheritDoc} */
    @Override public long getCacheHits() {
        return hits;
    }

    /** {@inheritDoc} */
    @Override public float getCacheHitPercentage() {
        if (hits == 0 || reads == 0)
            return 0;

        return (float)hits / reads * 100.0f;
    }

    /** {@inheritDoc} */
    @Override public long getCacheMisses() {
        return misses;
    }

    /** {@inheritDoc} */
    @Override public float getCacheMissPercentage() {
        if (misses == 0 || reads == 0)
            return 0;

        return (float)misses / reads * 100.0f;
    }

    /** {@inheritDoc} */
    @Override public long getCacheGets() {
        return reads;
    }

    /** {@inheritDoc} */
    @Override public long getCachePuts() {
        return puts;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorPuts() {
        return entryProcessorPuts;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorReadOnlyInvocations() {
        return entryProcessorReadOnlyInvocations;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorInvocations() {
        return entryProcessorInvocations;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorHits() {
        return entryProcessorHits;
    }

    /** {@inheritDoc} */
    @Override public float getEntryProcessorHitPercentage() {
        return entryProcessorHitPercentage;
    }

    /** {@inheritDoc} */
    @Override public float getEntryProcessorMissPercentage() {
        return entryProcessorMissPercentage;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorMisses() {
        return entryProcessorMisses;
    }

    /** {@inheritDoc} */
    @Override public long getEntryProcessorRemovals() {
        return entryProcessorRemovals;
    }

    /** {@inheritDoc} */
    @Override public float getEntryProcessorAverageInvocationTime() {
        return entryProcessorAverageInvocationTime;
    }

    /** {@inheritDoc} */
    @Override public float getEntryProcessorMinInvocationTime() {
        return entryProcessorMinInvocationTime;
    }

    /** {@inheritDoc} */
    @Override public float getEntryProcessorMaxInvocationTime() {
        return entryProcessorMaxInvocationTime;
    }

    /** {@inheritDoc} */
    @Override public long getCacheRemovals() {
        return removes;
    }

    /** {@inheritDoc} */
    @Override public long getCacheEvictions() {
        return evicts;
    }

    /** {@inheritDoc} */
    @Override public float getAverageGetTime() {
        return getAvgTimeNanos;
    }

    /** {@inheritDoc} */
    @Override public float getAveragePutTime() {
        return putAvgTimeNanos;
    }

    /** {@inheritDoc} */
    @Override public float getAverageRemoveTime() {
        return rmvAvgTimeNanos;
    }

    /** {@inheritDoc} */
    @Override public float getAverageTxCommitTime() {
        return commitAvgTimeNanos;
    }

    /** {@inheritDoc} */
    @Override public float getAverageTxRollbackTime() {
        return rollbackAvgTimeNanos;
    }

    /** {@inheritDoc} */
    @Override public long getCacheTxCommits() {
        return txCommits;
    }

    /** {@inheritDoc} */
    @Override public long getCacheTxRollbacks() {
        return txRollbacks;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return cacheName;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapGets() {
        return offHeapGets;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapPuts() {
        return offHeapPuts;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapRemovals() {
        return offHeapRemoves;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapEvictions() {
        return offHeapEvicts;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapHits() {
        return offHeapHits;
    }

    /** {@inheritDoc} */
    @Override public float getOffHeapHitPercentage() {
        if (offHeapHits == 0 || offHeapGets == 0)
            return 0;

        return (float)offHeapHits / offHeapGets * 100.0f;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapMisses() {
        return offHeapMisses;
    }

    /** {@inheritDoc} */
    @Override public float getOffHeapMissPercentage() {
        if (offHeapMisses == 0 || offHeapGets == 0)
            return 0;

        return (float)offHeapMisses / offHeapGets * 100.0f;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapEntriesCount() {
        return offHeapEntriesCnt;
    }

    /** {@inheritDoc} */
    @Override public long getHeapEntriesCount() {
        return heapEntriesCnt;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapPrimaryEntriesCount() {
        return offHeapPrimaryEntriesCnt;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapBackupEntriesCount() {
        return offHeapBackupEntriesCnt;
    }

    /** {@inheritDoc} */
    @Override public long getOffHeapAllocatedSize() {
        return offHeapAllocatedSize;
    }

    /** {@inheritDoc} */
    @Override public int getSize() {
        return size;
    }

    /** {@inheritDoc} */
    @Override public long getCacheSize() {
        return cacheSize;
    }

    /** {@inheritDoc} */
    @Override public int getKeySize() {
        return keySize;
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return isEmpty;
    }

    /** {@inheritDoc} */
    @Override public int getDhtEvictQueueCurrentSize() {
        return dhtEvictQueueCurrSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxThreadMapSize() {
        return txThreadMapSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxXidMapSize() {
        return txXidMapSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxCommitQueueSize() {
        return txCommitQueueSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxPrepareQueueSize() {
        return txPrepareQueueSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxStartVersionCountsSize() {
        return txStartVerCountsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxCommittedVersionsSize() {
        return txCommittedVersionsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxRolledbackVersionsSize() {
        return txRolledbackVersionsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtThreadMapSize() {
        return txDhtThreadMapSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtXidMapSize() {
        return txDhtXidMapSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtCommitQueueSize() {
        return txDhtCommitQueueSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtPrepareQueueSize() {
        return txDhtPrepareQueueSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtStartVersionCountsSize() {
        return txDhtStartVerCountsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtCommittedVersionsSize() {
        return txDhtCommittedVersionsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTxDhtRolledbackVersionsSize() {
        return txDhtRolledbackVersionsSize;
    }

    /** {@inheritDoc} */
    @Override public int getTotalPartitionsCount() {
        return totalPartitionsCnt;
    }

    /** {@inheritDoc} */
    @Override public long getRebalancedKeys() {
        return rebalancedKeys;
    }

    /** {@inheritDoc} */
    @Override public long getEstimatedRebalancingKeys() {
        return estimatedRebalancingKeys;
    }

    /** {@inheritDoc} */
    @Override public int getRebalancingPartitionsCount() {
        return rebalancingPartitionsCnt;
    }

    /** {@inheritDoc} */
    @Override public long getKeysToRebalanceLeft() {
        return keysToRebalanceLeft;
    }

    /** {@inheritDoc} */
    @Override public long getRebalancingKeysRate() {
        return rebalancingKeysRate;
    }

    /** {@inheritDoc} */
    @Override public long getRebalancingBytesRate() {
        return rebalancingBytesRate;
    }

    /** {@inheritDoc} */
    @Override public long estimateRebalancingFinishTime() {
        return rebalanceFinishTime;
    }

    /** {@inheritDoc} */
    @Override public long rebalancingStartTime() {
        return rebalanceStartTime;
    }

    /** {@inheritDoc} */
    @Override public long getEstimatedRebalancingFinishTime() {
        return rebalanceFinishTime;
    }

    /** {@inheritDoc} */
    @Override public long getRebalancingStartTime() {
        return rebalanceStartTime;
    }

    /** {@inheritDoc} */
    @Override public long getRebalanceClearingPartitionsLeft() {
        return rebalanceClearingPartitionsLeft;
    }

    /** {@inheritDoc} */
    @Override public boolean isWriteBehindEnabled() {
        return isWriteBehindEnabled;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindFlushSize() {
        return writeBehindFlushSize;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindFlushThreadCount() {
        return writeBehindFlushThreadCnt;
    }

    /** {@inheritDoc} */
    @Override public long getWriteBehindFlushFrequency() {
        return writeBehindFlushFreq;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindStoreBatchSize() {
        return writeBehindStoreBatchSize;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindTotalCriticalOverflowCount() {
        return writeBehindTotalCriticalOverflowCnt;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindCriticalOverflowCount() {
        return writeBehindCriticalOverflowCnt;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindErrorRetryCount() {
        return writeBehindErrorRetryCnt;
    }

    /** {@inheritDoc} */
    @Override public int getWriteBehindBufferSize() {
        return writeBehindBufSize;
    }

    /** {@inheritDoc} */
    @Override public String getKeyType() {
        return keyType;
    }

    /** {@inheritDoc} */
    @Override public String getValueType() {
        return valType;
    }

    /** {@inheritDoc} */
    @Override public boolean isStoreByValue() {
        return isStoreByVal;
    }

    /** {@inheritDoc} */
    @Override public boolean isStatisticsEnabled() {
        return isStatisticsEnabled;
    }

    /** {@inheritDoc} */
    @Override public boolean isManagementEnabled() {
        return isManagementEnabled;
    }

    /** {@inheritDoc} */
    @Override public boolean isReadThrough() {
        return isReadThrough;
    }

    /** {@inheritDoc} */
    @Override public boolean isWriteThrough() {
        return isWriteThrough;
    }

    /** {@inheritDoc} */
    @Override public boolean isValidForReading() {
        return isValidForReading;
    }

    /** {@inheritDoc} */
    @Override public boolean isValidForWriting() {
        return isValidForWriting;
    }

    /** {@inheritDoc} */
    @Override public String getTxKeyCollisions() {
        return txKeyCollisions;
    }

    /** {@inheritDoc} */
    @Override public boolean isIndexRebuildInProgress() {
        return idxRebuildInProgress;
    }

    /** {@inheritDoc} */
    @Override public long getIndexRebuildKeysProcessed() {
        return idxRebuildKeyProcessed;
    }

    /** {@inheritDoc} */
    @Override public int getIndexBuildPartitionsLeftCount() {
        return idxBuildPartitionsLeftCount;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(CacheMetricsSnapshot.class, this);
    }

    /** {@inheritDoc} */
    @Override public void writeExternalData(ObjectOutput out) throws IOException {
        out.writeLong(reads);
        out.writeLong(puts);
        out.writeLong(hits);
        out.writeLong(misses);
        out.writeLong(txCommits);
        out.writeLong(txRollbacks);
        out.writeLong(evicts);
        out.writeLong(removes);

        out.writeFloat(putAvgTimeNanos);
        out.writeFloat(getAvgTimeNanos);
        out.writeFloat(rmvAvgTimeNanos);
        out.writeFloat(commitAvgTimeNanos);
        out.writeFloat(rollbackAvgTimeNanos);

        out.writeLong(offHeapGets);
        out.writeLong(offHeapPuts);
        out.writeLong(offHeapRemoves);
        out.writeLong(offHeapEvicts);
        out.writeLong(offHeapHits);
        out.writeLong(offHeapMisses);
        out.writeLong(offHeapEntriesCnt);
        out.writeLong(heapEntriesCnt);
        out.writeLong(offHeapPrimaryEntriesCnt);
        out.writeLong(offHeapBackupEntriesCnt);
        out.writeLong(offHeapAllocatedSize);

        out.writeInt(dhtEvictQueueCurrSize);
        out.writeInt(txThreadMapSize);
        out.writeInt(txXidMapSize);
        out.writeInt(txCommitQueueSize);
        out.writeInt(txPrepareQueueSize);
        out.writeInt(txStartVerCountsSize);
        out.writeInt(txCommittedVersionsSize);
        out.writeInt(txRolledbackVersionsSize);
        out.writeInt(txDhtThreadMapSize);
        out.writeInt(txDhtXidMapSize);
        out.writeInt(txDhtCommitQueueSize);
        out.writeInt(txDhtPrepareQueueSize);
        out.writeInt(txDhtStartVerCountsSize);
        out.writeInt(txDhtCommittedVersionsSize);
        out.writeInt(txDhtRolledbackVersionsSize);
        out.writeInt(writeBehindTotalCriticalOverflowCnt);
        out.writeInt(writeBehindCriticalOverflowCnt);
        out.writeInt(writeBehindErrorRetryCnt);

        out.writeInt(totalPartitionsCnt);
        out.writeInt(rebalancingPartitionsCnt);
        out.writeLong(keysToRebalanceLeft);
        out.writeLong(rebalancingBytesRate);
        out.writeLong(rebalancingKeysRate);

        out.writeLong(rebalancedKeys);
        out.writeLong(estimatedRebalancingKeys);
        out.writeLong(rebalanceStartTime);
        out.writeLong(rebalanceFinishTime);
        out.writeLong(rebalanceClearingPartitionsLeft);

        out.writeLong(entryProcessorPuts);
        out.writeFloat(entryProcessorAverageInvocationTime);
        out.writeLong(entryProcessorInvocations);
        out.writeFloat(entryProcessorMaxInvocationTime);
        out.writeFloat(entryProcessorMinInvocationTime);
        out.writeLong(entryProcessorReadOnlyInvocations);
        out.writeFloat(entryProcessorHitPercentage);
        out.writeLong(entryProcessorHits);
        out.writeLong(entryProcessorMisses);
        out.writeFloat(entryProcessorMissPercentage);
        out.writeLong(entryProcessorRemovals);

        out.writeLong(cacheSize);
        out.writeBoolean(isEmpty);
        out.writeInt(size);
        out.writeInt(keySize);
        U.writeLongString(out, txKeyCollisions);
        out.writeInt(idxBuildPartitionsLeftCount);
    }

    /** {@inheritDoc} */
    @Override public void readExternalData(ObjectInput in) throws IOException, ClassNotFoundException {
        reads = in.readLong();
        puts = in.readLong();
        hits = in.readLong();
        misses = in.readLong();
        txCommits = in.readLong();
        txRollbacks = in.readLong();
        evicts = in.readLong();
        removes = in.readLong();

        putAvgTimeNanos = in.readFloat();
        getAvgTimeNanos = in.readFloat();
        rmvAvgTimeNanos = in.readFloat();
        commitAvgTimeNanos = in.readFloat();
        rollbackAvgTimeNanos = in.readFloat();

        offHeapGets = in.readLong();
        offHeapPuts = in.readLong();
        offHeapRemoves = in.readLong();
        offHeapEvicts = in.readLong();
        offHeapHits = in.readLong();
        offHeapMisses = in.readLong();
        offHeapEntriesCnt = in.readLong();
        heapEntriesCnt = in.readLong();
        offHeapPrimaryEntriesCnt = in.readLong();
        offHeapBackupEntriesCnt = in.readLong();
        offHeapAllocatedSize = in.readLong();

        dhtEvictQueueCurrSize = in.readInt();
        txThreadMapSize = in.readInt();
        txXidMapSize = in.readInt();
        txCommitQueueSize = in.readInt();
        txPrepareQueueSize = in.readInt();
        txStartVerCountsSize = in.readInt();
        txCommittedVersionsSize = in.readInt();
        txRolledbackVersionsSize = in.readInt();
        txDhtThreadMapSize = in.readInt();
        txDhtXidMapSize = in.readInt();
        txDhtCommitQueueSize = in.readInt();
        txDhtPrepareQueueSize = in.readInt();
        txDhtStartVerCountsSize = in.readInt();
        txDhtCommittedVersionsSize = in.readInt();
        txDhtRolledbackVersionsSize = in.readInt();
        writeBehindTotalCriticalOverflowCnt = in.readInt();
        writeBehindCriticalOverflowCnt = in.readInt();
        writeBehindErrorRetryCnt = in.readInt();

        totalPartitionsCnt = in.readInt();
        rebalancingPartitionsCnt = in.readInt();
        keysToRebalanceLeft = in.readLong();
        rebalancingBytesRate = in.readLong();
        rebalancingKeysRate = in.readLong();

        rebalancedKeys = in.readLong();
        estimatedRebalancingKeys = in.readLong();
        rebalanceStartTime = in.readLong();
        rebalanceFinishTime = in.readLong();
        rebalanceClearingPartitionsLeft = in.readLong();

        entryProcessorPuts = in.readLong();
        entryProcessorAverageInvocationTime = in.readFloat();
        entryProcessorInvocations = in.readLong();
        entryProcessorMaxInvocationTime = in.readFloat();
        entryProcessorMinInvocationTime = in.readFloat();
        entryProcessorReadOnlyInvocations = in.readLong();
        entryProcessorHitPercentage = in.readFloat();
        entryProcessorHits = in.readLong();
        entryProcessorMisses = in.readLong();
        entryProcessorMissPercentage = in.readFloat();
        entryProcessorRemovals = in.readLong();

        cacheSize = in.readLong();
        isEmpty = in.readBoolean();
        size = in.readInt();
        keySize = in.readInt();
        txKeyCollisions = U.readLongString(in);
        idxBuildPartitionsLeftCount = in.readInt();
    }
}
