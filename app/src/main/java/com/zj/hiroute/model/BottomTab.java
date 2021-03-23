package com.zj.hiroute.model;

import java.util.List;

/**
 * 底部导航model
 *
 * @author 张锦
 */
public class BottomTab {
    /**
     * selectTab : 0
     * tabs : [{"size":24,"enable":true,"index":0,"pageUrl":"main/tabs/home","title":"Home"},{"size":24,"enable":true,"index":1,"pageUrl":"main/tabs/dashboard","title":"Dashboard"},{"size":40,"enable":true,"index":2,"pageUrl":"main/tabs/notifications","title":"Notifications"}]
     */
    public int selectTab;
    public List<Tab> tabs;

    @Override
    public String toString() {
        return "BottomTab{" +
                "selectTab=" + selectTab +
                ", tabs=" + tabs +
                '}';
    }

    public static class Tab {
        /**
         * size : 24
         * enable : true
         * index : 0
         * pageUrl : main/tabs/home
         * title : Home
         */
        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;

        @Override
        public String toString() {
            return "Tab{" +
                    "size=" + size +
                    ", enable=" + enable +
                    ", index=" + index +
                    ", pageUrl='" + pageUrl + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
