package com.jiuan.it.ipc.tools;

import java.util.Comparator;
import java.util.Date;

public class StepComparator implements Comparator<String> {

    /**
     * 如果o1小于o2,返回一个负数;如果o1大于o2，返回一个正数;如果他们相等，则返回0;
     */
    @Override
    public int compare(String o1, String o2) {
        long time1 = Long.valueOf(o1);
        Date acceptTime1 = new Date();
        acceptTime1.setTime(time1);
        long time2 = Long.valueOf(o2);
        Date acceptTime2 = new Date();
        acceptTime2.setTime(time2);

        //对日期字段进行升序，如果欲降序可采用before方法
        if(acceptTime1.before(acceptTime2)) return 1;
        return -1;
    }
}
