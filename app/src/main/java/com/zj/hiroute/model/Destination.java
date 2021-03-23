package com.zj.hiroute.model;

public class Destination {

    /**
     * asStarter : false
     * className : com.zj.hiroute.ui.dashboard.DashboardFragment
     * id : 160002873
     * pageUrl: main/tabs/dashboard,
     * destType : fragment
     */
    public boolean asStarter; // 是否作为路由的第一个启动页
    public String className;  // 全类名
    public int id;            // 路由节点（页面）id
    public String pageUrl;    // 路由节点url
    public String destType;   // 路由节点类型，activity、fragment、dialog
}
