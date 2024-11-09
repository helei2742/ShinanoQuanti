package com.helei.binanceapi.supporter;

import com.alibaba.fastjson.JSONObject;
import com.helei.constants.order.TimeInForce;
import com.helei.binanceapi.constants.strategy.StrategyOPCode;
import com.helei.binanceapi.constants.strategy.StrategyStatus;
import com.helei.binanceapi.constants.strategy.StrategyType;
import com.helei.binanceapi.dto.accountevent.*;
import com.helei.constants.trade.MarginMode;
import com.helei.constants.trade.TradeSide;
import com.helei.constants.order.*;

import java.util.ArrayList;
import java.util.List;

public interface AccountEventConverter {
    AccountEvent convertFromJsonObject(JSONObject jsonObject);


    enum Converter implements AccountEventConverter {
        LISTEN_KEY_EXPIRED {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                return new ListenKeyExpireEvent(jsonObject.getLong("E"));
            }
        },
        ACCOUNT_UPDATE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                BalancePositionUpdateEvent event = new BalancePositionUpdateEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));

                JSONObject a = jsonObject.getJSONObject("a");
                event.setReason(BalancePositionUpdateEvent.BPUpdateReason.valueOf(a.getString("m")));

                List<BalancePositionUpdateEvent.BalanceChangeInfo> bList = new ArrayList<>();
                for (int i = 0; i < a.getJSONArray("B").size(); i++) {
                    JSONObject jb = a.getJSONArray("B").getJSONObject(i);
                    BalancePositionUpdateEvent.BalanceChangeInfo changeInfo = new BalancePositionUpdateEvent.BalanceChangeInfo();
                    changeInfo.setAsset(jb.getString("a"));
                    changeInfo.setWalletBalance(jb.getDouble("wb"));
                    changeInfo.setBailRemoveWalletBalance(jb.getDouble("cw"));
                    changeInfo.setWalletBalanceChange(jb.getDouble("bc"));
                    bList.add(changeInfo);
                }
                event.setBalanceChangeInfos(bList);

                List<BalancePositionUpdateEvent.PositionChangeInfo> pList = new ArrayList<>();
                for (int i = 0; i < a.getJSONArray("P").size(); i++) {
                    JSONObject jb = a.getJSONArray("P").getJSONObject(i);
                    BalancePositionUpdateEvent.PositionChangeInfo changeInfo = new BalancePositionUpdateEvent.PositionChangeInfo();
                    changeInfo.setSymbol(jb.getString("s"));
                    changeInfo.setPosition(jb.getDouble("pa"));
                    changeInfo.setEntryPrice(jb.getDouble("ep"));
                    changeInfo.setBalanceEqualPrice(jb.getDouble("bep"));
                    changeInfo.setCountProfitOrLoss(jb.getDouble("cr"));
                    changeInfo.setUnrealizedProfitOrLoss(jb.getDouble("up"));
                    changeInfo.setMarginMode(MarginMode.valueOf(jb.getString("mt").toUpperCase()));
                    changeInfo.setBail(jb.getDouble("iw"));
                    changeInfo.setPositionSide(PositionSide.STATUS_MAP.get(jb.getString("ps")));
                    pList.add(changeInfo);
                }
                event.setPositionChangeInfos(pList);
                return event;
            }
        },

        MARGIN_CALL {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                BailNeedEvent event = new BailNeedEvent(jsonObject.getLong("E"));
                event.setBailRemoveWalletBalance(jsonObject.getDouble("cw"));

                JSONObject p = jsonObject.getJSONObject("p");

                BailNeedEvent.PositionNeedInfo positionNeedInfo = new BailNeedEvent.PositionNeedInfo();
                positionNeedInfo.setSymbol(p.getString("s"));
                positionNeedInfo.setPositionSide(PositionSide.STATUS_MAP.get(p.getString("ps")));
                positionNeedInfo.setPosition(p.getDouble("pa"));
                positionNeedInfo.setMarginMode(MarginMode.valueOf(p.getString("mt").toUpperCase()));
                positionNeedInfo.setBail(p.getDouble("iw"));
                positionNeedInfo.setMarkPrice(p.getDouble("mp"));
                positionNeedInfo.setUnrealizedProfitOrLoss(p.getDouble("up"));
                positionNeedInfo.setNeedBail(p.getDouble("mm"));

                return event;
            }
        },

        ORDER_TRADE_UPDATE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                OrderTradeUpdateEvent event = new OrderTradeUpdateEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));

                JSONObject o = jsonObject.getJSONObject("o");

                OrderTradeUpdateEvent.OrderTradeUpdateDetails details = new OrderTradeUpdateEvent.OrderTradeUpdateDetails();
                details.setSymbol(o.getString("s"));
                details.setClientOrderId(o.getString("c"));
                details.setOrderSide(TradeSide.valueOf(o.getString("S").toUpperCase()));
                details.setOrderType(OrderType.valueOf(o.getString("o").toUpperCase()));
                details.setTimeInForce(TimeInForce.valueOf(o.getString("f").toUpperCase()));
                details.setOriginalQuantity(o.getDouble("q"));
                details.setOriginalPrice(o.getDouble("p"));
                details.setAveragePrice(o.getDouble("ap"));
                details.setStopPrice(o.getString("sp"));
                details.setExecutionType(OrderExcuteType.STATUS_MAP.get(o.getString("x")));
                details.setOrderStatus(OrderStatus.valueOf(o.getString("X").toUpperCase()));
                details.setOrderId(o.getLong("i"));
                details.setLastFilledQuantity(o.getDouble("l"));
                details.setCumulativeFilledQuantity(o.getDouble("z"));
                details.setLastFilledPrice(o.getDouble("L"));
                details.setCommissionAsset(o.getString("N"));
                details.setCommissionAmount(o.getDouble("n"));
                details.setTradeTime(o.getLong("T"));
                details.setTradeId(o.getLong("t"));
                details.setBuyerNetValue(o.getDouble("b"));
                details.setSellerNetValue(o.getDouble("a"));
                details.setMaker(o.getBoolean("m"));
                details.setReduceOnly(o.getBoolean("R"));
                details.setWorkingType(WorkingType.valueOf(o.getString("wt").toUpperCase()));
                details.setOriginalOrderType(OrderType.valueOf(o.getString("ot").toUpperCase()));
                details.setPositionSide(PositionSide.STATUS_MAP.get(o.getString("ps")));
                details.setClosePosition(o.getBoolean("cp"));
                details.setActivationPrice(o.getDouble("AP"));
                details.setCallbackRate(o.getDouble("cr"));
                details.setPriceProtect(o.getBoolean("pP"));
                details.setRealizedProfit(o.getDouble("rp"));
                details.setSelfTradePreventionMode(SelfTradePreventionMode.valueOf(o.getString("V").toUpperCase()));
                details.setPriceMatch(PriceMatch.valueOf(o.getString("pm").toUpperCase()));
                details.setGtdCancelTime(o.getLong("gtd"));

                event.setOrderTradeUpdateDetails(details);
                return event;
            }
        },

        TRADE_LITE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                OrderTradeUpdateLiteEvent event = new OrderTradeUpdateLiteEvent(jsonObject.getLong("E"));
                event.setTradeTime(jsonObject.getLong("T"));
                event.setSymbol(jsonObject.getString("s"));
                event.setOriginalQuantity(jsonObject.getDouble("q"));
                event.setOriginalPrice(jsonObject.getDouble("p"));
                event.setIsMaker(jsonObject.getBoolean("m"));
                event.setClientOrderId(jsonObject.getString("c"));
                event.setOrderSide(TradeSide.valueOf(jsonObject.getString("S").toUpperCase()));
                event.setLastTradePrice(jsonObject.getDouble("L"));
                event.setLastTradeQuantity(jsonObject.getDouble("l"));
                event.setTradeId(jsonObject.getLong("t"));
                event.setOrderId(jsonObject.getLong("i"));
                return event;
            }
        },

        ACCOUNT_CONFIG_UPDATE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                AccountConfigUpdateEvent event = new AccountConfigUpdateEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));

                JSONObject ac = jsonObject.getJSONObject("ac");
                if (ac != null) {
                    AccountConfigUpdateEvent.AccountLeverConfigChangeInfo leverConfigChangeInfo = new AccountConfigUpdateEvent.AccountLeverConfigChangeInfo();
                    leverConfigChangeInfo.setSymbol(ac.getString("s"));
                    leverConfigChangeInfo.setLever(ac.getInteger("l"));
                    event.setLeverChangeInfo(leverConfigChangeInfo);
                }

                JSONObject ai = jsonObject.getJSONObject("ai");
                if (ai != null) {
                    AccountConfigUpdateEvent.AccountInfoChangeInfo accountInfoChangeInfo = new AccountConfigUpdateEvent.AccountInfoChangeInfo();
                    accountInfoChangeInfo.setUnitBailState(ai.getBoolean("j"));
                    event.setInfoChangeInfo(accountInfoChangeInfo);
                }

                return event;
            }
        },

        STRATEGY_UPDATE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                StrategyUpdateEvent event = new StrategyUpdateEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));

                StrategyUpdateEvent.StrategyUpdateInfo updateInfo = new StrategyUpdateEvent.StrategyUpdateInfo();
                JSONObject su = jsonObject.getJSONObject("su");
                updateInfo.setStrategyId(su.getLong("si"));
                updateInfo.setStrategyType(StrategyType.STATUS_MAP.get(su.getString("st")));
                updateInfo.setStrategyStatus(StrategyStatus.STATUS_MAP.get(su.getString("ss")));
                updateInfo.setSymbol(su.getString("s"));
                updateInfo.setUpdateTime(su.getLong("ut"));
                updateInfo.setStrategyOPCode(StrategyOPCode.STATUS_MAP.get(su.getInteger("c")));
                event.setStrategyUpdateInfo(updateInfo);
                return event;
            }
        },

        GRID_UPDATE {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                GridUpdateEvent event = new GridUpdateEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));

                JSONObject gu = jsonObject.getJSONObject("gu");
                GridUpdateEvent.GridUpdateInfo updateInfo = new GridUpdateEvent.GridUpdateInfo();
                updateInfo.setStrategyId(gu.getLong("si"));
                updateInfo.setStrategyType(StrategyType.STATUS_MAP.get(gu.getString("st")));
                updateInfo.setStrategyStatus(StrategyStatus.STATUS_MAP.get(gu.getString("ss")));
                updateInfo.setSymbol(gu.getString("s"));
                updateInfo.setRealizedPnl(gu.getDouble("r"));
                updateInfo.setUnpairedAverage(gu.getDouble("up"));
                updateInfo.setUnpairedQuantity(gu.getDouble("uq"));
                updateInfo.setUnpairedFee(gu.getDouble("uf"));
                updateInfo.setMatchedPnl(gu.getDouble("mp"));
                updateInfo.setUpdateTime(gu.getLong("ut"));

                event.setGridUpdateInfo(updateInfo);
                return event;
            }
        },

        CONDITIONAL_ORDER_TRIGGER_REJECT {
            @Override
            public AccountEvent convertFromJsonObject(JSONObject jsonObject) {
                ConditionalOrderTriggerRejectEvent event = new ConditionalOrderTriggerRejectEvent(jsonObject.getLong("E"));
                event.setMatchMakingTime(jsonObject.getLong("T"));


                JSONObject or = jsonObject.getJSONObject("or");
                ConditionalOrderTriggerRejectEvent.OrderRejectInfo orderRejectInfo = new ConditionalOrderTriggerRejectEvent.OrderRejectInfo();

                orderRejectInfo.setSymbol(or.getString("s"));
                orderRejectInfo.setOrderId(or.getLong("o"));
                orderRejectInfo.setRejectReason(or.getString("r"));
                event.setOrderRejectInfo(orderRejectInfo);
                return event;
            }
        }
    }
}
