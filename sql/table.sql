CREATE TABLE t_order_binance_contract
(
    order_id                   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单编号',
    user_id                    BIGINT         NOT NULL COMMENT '用户id',
    account_id                 BIGINT         NOT NULL COMMENT '用户的账户id',

    symbol                     VARCHAR(20)    NOT NULL COMMENT '交易对，例如 BTCUSDT',
    side                       ENUM('BUY', 'SELL') NOT NULL COMMENT '交易方向',
    position_side              ENUM('BOTH', 'LONG', 'SHORT') DEFAULT 'BOTH' COMMENT '持仓方向，默认BOTH',
    type                       ENUM('LIMIT', 'MARKET', 'STOP', 'STOP_MARKET', 'TAKE_PROFIT', 'TAKE_PROFIT_MARKET', 'TRAILING_STOP_MARKET') NOT NULL COMMENT '订单类型',

    reduce_only                BOOLEAN        DEFAULT FALSE COMMENT '是否仅减少持仓，仅适用于双向持仓模式',
    quantity                   DECIMAL(18, 8) NOT NULL COMMENT '交易数量',
    price                      DECIMAL(18, 8) DEFAULT NULL COMMENT '订单价格，仅限限价单',
    client_order_id            VARCHAR(36)    DEFAULT NULL COMMENT '用户自定义订单ID',
    stop_price                 DECIMAL(18, 8) DEFAULT NULL COMMENT '触发价，仅限触发单',
    close_position             BOOLEAN        DEFAULT FALSE COMMENT '是否为全平仓单，仅适用于触发单',
    activation_price           DECIMAL(18, 8) DEFAULT NULL COMMENT '追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)',
    callback_rate              DECIMAL(18, 8) DEFAULT NULL COMMENT '追踪止损回调比例，可取值范围[0.1, 10],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数',

    time_in_force              ENUM('GTC', 'IOC', 'FOK') DEFAULT 'GTC' COMMENT '订单有效期类型',
    working_type               ENUM('MARK_PRICE', 'CONTRACT_PRICE') DEFAULT 'CONTRACT_PRICE' COMMENT '触发价格类型',
    price_protect              BOOLEAN        DEFAULT FALSE COMMENT '价格保护开关',

    order_resp_type            ENUM('ACK', 'RESULT') DEFAULT 'ACK' COMMENT "响应类型",
    price_match                ENUM('OPPONENT', 'OPPONENT_5', 'OPPONENT_10', 'OPPONENT_20', 'QUEUE', 'QUEUE_5', 'QUEUE_10', 'QUEUE_20', 'NONE') DEFAULT 'NONE' COMMENT '不能与price同时传',

    self_trade_prevention_mode ENUM('NONE' , 'EXPIRE_TAKER', 'EXPIRE_MAKER', 'EXPIRE_BOTH') DEFAULT 'NONE' COMMENT '防自成交模式， 默认NONE',
    good_till_date             BIGINT         DEFAULT NULL COMMENT 'TIF为GTD时订单的自动取消时间， 当timeInforce为GTD时必传；传入的时间戳仅保留秒级精度，毫秒级部分会被自动忽略，时间戳需大于当前时间+600s且小于253402300799000',

    timestamp                  BIGINT         NOT NULL COMMENT '请求时间戳',

    status                     ENUM('NEW', 'PARTIALLY_FILLED', 'FILLED', 'CANCELED', 'REJECTED', 'EXPIRED', 'EXPIRED_IN_MATCH', 'CREATED', 'WRITE_IN_DB', 'WRITE_IN_KAFKA', 'SEND_TO_CEX') DEFAULT 'NEW' COMMENT '订单状态',

    created_datetime           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    updated_datetime           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币安合约交易订单表';
