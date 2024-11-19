package com.helei.solanarpc.constants;


public enum SolanaHttpRequestType {

    /**
     * 查询账号信息
     */
    getAccountInfo,
    /**
     * 查询账号余额
     */
    getBalance,
    /**
     * 查询区块数据
     */
    getBlock,
    /**
     * 查询区块高度
     */
    getBlockHeight,
    /**
     * 查询区块生产信息
     */
    getBlockProduction,
    /**
     * 查询区块提交信息
     */
    getBlockCommitment,
    /**
     * 查询区块集
     */
    getBlocks,
    /**
     * 查询指定区间内的区块
     */
    getBlocksWithLimit,
    /**
     * 查询区块时间
     */
    getBlockTime,
    /**
     * 查询集群节点
     */
    getClusterNodes,
    /**
     * 查询周期信息
     */
    getEpochInfo,
    /**
     * 查询周期计划
     */
    getEpochSchedule,
    /**
     * 查询指定区块的费率计算器
     */
    getFeeCalculatorForBlockhash,
    /**
     * 查询费率治理人
     */
    getFeeRateGovernor,
    /**
     * 查询费率
     */
    getFees,
    /**
     * 查询第一个有效区块
     */
    getFirstAvailableBlock,
    /**
     * 查询创世哈希
     */
    getGenesisHash,
    /**
     * 查询健康状态
     */
    getHealth,
    /**
     * 查询实体标识
     */
    getIdentity,
    /**
     * 查询通胀治理人
     */
    getInflationGovernor,
    /**
     * 查询通胀率
     */
    getInflationRate,
    /**
     * 查询通胀奖励
     */
    getInflationReward,
    /**
     * 查询最大账号
     */
    getLargestAccounts,
    /**
     * 查询主导人计划表
     */
    getLeaderSchedule,
    /**
     * 查询最大重发槽位
     */
    getMaxRetransmitSlot,
    /**
     * 查询最大插入槽位
     */
    getMaxShredInsertSlot,
    /**
     * 查询可豁免租金的最小余额
     */
    getMinimumBalanceForRentExemption,
    /**
     * 查询多个账号
     */
    getMultipleAccounts,
    /**
     * 查询程序账号
     */
    getProgramAccounts,
    /**
     * 查询最近的区块哈希
     */
    getLatestBlockhash,
    /**
     * 查询最近的性能样本
     */
    getRecentPerformanceSamples,
    /**
     * 获取快照槽位
     */
    getSnapshotSlot,
    /**
     * 获取地址签名
     */
    getSignaturesForAddress,
    /**
     * 获取签名状态
     */
    getSignatureStatuses,
    /**
     * 查询槽位
     */
    getSlot,
    /**
     * 查询槽位主导人
     */
    getSlotLeader,
    /**
     * 查询槽位主导人集合
     */
    getSlotLeaders,
    /**
     * 查询抵押激活信息
     */
    getStakeActivation,
    /**
     * 查询供应量
     */
    getSupply,
    /**
     * 查询通证账号余额
     */
    getTokenAccountBalance,
    /**
     * 按代表查询通证账号
     */
    getTokenAccountsByDelegate,
    /**
     * 按持有人查询通证账号
     */
    getTokenAccountsByOwner,
    /**
     * 查询通证的最大账号
     */
    getTokenLargestAccounts,
    /**
     * 查询通证供应量
     */
    getTokenSupply,
    /**
     * 查询交易
     */
    getTransaction,
    /**
     * 查询交易数量
     */
    getTransactionCount,
    /**
     * 查询版本
     */
    getVersion,
    /**
     * 查询投票账号
     */
    getVoteAccounts,
    /**
     * 最小账本槽位
     */
    minimumLedgerSlot,
    /**
     * 请求空投
     */
    requestAirdrop,
    /**
     * 发送交易
     */
    sendTransaction,
    /**
     * 模拟交易
     */
    simulateTransaction,
}

