















shinano:
quantity:
telegram_bot:
run_type: # 运行环境配置
configs:
        - env: TEST_NET
trade_type:
        - CONTRACT

bots: #机器人的具体设置
        - botUsername: test1
token: 12345677
        - botUsername: test2
token: 12345677

kafka:
bootstrap_servers: 127.0.0.1:9092 # Kafka服务器地址
group_id: tg_bot_test_group
kafka_num_partitions: 1
kafka_replication_factor: 1

redis:
        # 如果需要密码，格式为 redis://:password@localhost:6379
        #        url: redis://127.0.0.1:6379
url: redis://127.0.0.1:6379

signal: # 信号设置
normal: # 运行环境
spot: # 交易类型
            - symbol: btcusdt # 交易对名称
signal_names: # 信号名list
                - test1
                - test2
                - test3
            - symbol: ethusdt
signal_names:
        - test1
                - test2
                - test3
contract:
        - symbol: btcusdt
signal_names:
        - test1
                - test2
                - test3
            - symbol: ethusdt
signal_names:
        - test1
                - test2
                - test3
test_net: { }















