package com.lxc.partnerMatch.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class TableListener implements ReadListener<PlanetTableUserInfo> {

    /**
     * 这个方法用于在分析每一行数据时触发调用，
     * 它接收一行的数据值和分析上下文作为参数，并在方法体内执行相应的逻辑。
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(PlanetTableUserInfo data, AnalysisContext context) {
        log.info("每次读到一行数据就调用这个方法：打印解析的数据：{}", data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！");
    }


}