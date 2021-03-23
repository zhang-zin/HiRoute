package com.zj.nav_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 作用域
public @interface Destination {

    /**
     * 页面在路由中的名称
     *
     * @retur 页面名称
     */
    String pageUrl();

    /**
     * 是否默认启动的页面
     *
     * @return true 是
     */
    boolean asStarter() default false;
}
