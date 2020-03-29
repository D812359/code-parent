package com.atu.handler;

import com.alibaba.fastjson.JSON;
import com.atu.ad.dump.table.AdCreativeTable;
import com.atu.ad.dump.table.AdCreativeUnitTable;
import com.atu.ad.dump.table.AdPlanTable;
import com.atu.ad.dump.table.AdUnitTable;
import com.atu.index.DataTable;
import com.atu.index.IndexAware;
import com.atu.index.adplan.AdPlanIndex;
import com.atu.index.adplan.AdPlanObject;
import com.atu.index.adunit.AdUnitIndex;
import com.atu.index.adunit.AdUnitObject;
import com.atu.index.creative.CreativeIndex;
import com.atu.index.creative.CreativeObject;
import com.atu.index.creativeunit.CreativeUnitIndex;
import com.atu.index.creativeunit.CreativeUnitObject;
import com.atu.mysql.constant.OpType;
import com.atu.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: Tom
 * @date: 2020-03-29 17:15
 * @description: 构造全量索引
 * 1、索引之间存在层级划分,也就是依赖关系的划分；
 * 2、加载全量索引其实是增量索引 "添加"的一种特殊实现
 */
@Slf4j
public class AdLevelDataHandler {


    /**
     * 第二层级缓存加载
     * 新增或修改AdPlan的索引
     *
     * @param planTable
     * @param type
     */
    public static void handleLevel2(AdPlanTable planTable, OpType type) {
        AdPlanObject planObject = new AdPlanObject(
                planTable.getId(),
                planTable.getUserId(),
                planTable.getPlanStatus(),
                planTable.getStartDate(),
                planTable.getEndDate());
        handleBinlogEvent(DataTable.of(AdPlanIndex.class),
                planObject.getPlanId(),
                planObject,
                type
        );
    }

    public static void handleLevel2(AdCreativeTable creativeTable, OpType type) {
        CreativeObject creativeObject = new CreativeObject(
                creativeTable.getAdId(),
                creativeTable.getName(),
                creativeTable.getType(),
                creativeTable.getMaterialType(),
                creativeTable.getHeight(),
                creativeTable.getWidth(),
                creativeTable.getAuditStatus(),
                creativeTable.getAdUrl()
        );
        handleBinlogEvent(
                DataTable.of(CreativeIndex.class),
                creativeObject.getAdId(),
                creativeObject,
                type
        );
    }

    /**
     * 第三层级推广单元索引对象构造
     * 第三层级——与第二层级有依赖关系
     */
    public static void handleLevel3(AdUnitTable unitTable, OpType type) {
        //对第二层级的索引做校验，如果不存在打印错误日志
        AdPlanObject adPlanObject = DataTable.of(AdPlanIndex.class).get(unitTable.getPlanId());
        if (null == adPlanObject) {
            log.error("handleLevel3 found AdPlanObject error: {}",
                    unitTable.getPlanId());
            return;
        }

        AdUnitObject unitObject = new AdUnitObject(
                unitTable.getUnitId(),
                unitTable.getUnitStatus(),
                unitTable.getPositionType(),
                unitTable.getPlanId(),
                adPlanObject
        );

        //添加索引
        handleBinlogEvent(DataTable.of(AdUnitIndex.class),
                unitTable.getUnitId(),
                unitObject,
                type);

    }

    /**
     * 创意与推广单元的关联关系表的索引对象构造
     *
     * @param creativeUnitTable
     * @param type
     */
    public static void handleLevel3(AdCreativeUnitTable creativeUnitTable, OpType type) {
        if (type == OpType.UPDATE) {
            log.error("CreativeUnitIndex not support update");
            return;
        }

        AdUnitObject unitObject = DataTable.of(AdUnitIndex.class).get(creativeUnitTable.getUnitId());
        CreativeObject creativeObject = DataTable.of(CreativeIndex.class).get(creativeUnitTable.getAdId());
        if (unitObject == null || creativeObject == null) {
            log.error("AdCreativeUnitTable index error: {}",
                    JSON.toJSONString(creativeUnitTable));
            return;
        }

        CreativeUnitObject creativeUnitObject = new CreativeUnitObject(
                creativeUnitTable.getAdId(),
                creativeUnitTable.getUnitId()
        );
        handleBinlogEvent(DataTable.of(CreativeUnitIndex.class), CommonUtils.stringConcat(creativeUnitObject.getAdId().toString(),
                creativeUnitObject.getUnitId().toString()), creativeUnitObject,
                type);
    }

    /**
     * 实现通用方法：即可以处理全量，也可以处理增量
     *
     * @param index
     * @param key
     * @param value
     * @param type
     * @param <K>
     * @param <V>
     */
    private static <K, V> void handleBinlogEvent(IndexAware<K, V> index, K key, V value, OpType type) {
        switch (type) {
            case ADD:
                index.add(key, value);
                break;
            case UPDATE:
                index.update(key, value);
                break;
            case DELETE:
                index.delete(key, value);
                break;
            default:
                break;
        }
    }

}
