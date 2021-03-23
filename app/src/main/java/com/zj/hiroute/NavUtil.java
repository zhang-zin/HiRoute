package com.zj.hiroute;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.FragmentNavigator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zj.hiroute.model.BottomTab;
import com.zj.hiroute.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NavUtil {

    private static HashMap<String, Destination> destinations;

    /**
     * 根据json文件生成hashMap的节点对象
     *
     * @param context 上线文
     * @return destinationHashMap
     */
    public static String getDestinations(Context context, String fileName) {
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder builder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            inputStream.close();

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建真正的路由节点，并关联
     *
     * @param activity
     * @param controller
     * @param containerId
     */
    public static void builderNavGraph(FragmentActivity activity, NavController controller, int containerId) {
        String content = getDestinations(activity, "destination.json");
        destinations = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
        }.getType());
        Iterator<Destination> iterator = destinations.values().iterator();
        NavigatorProvider provider = controller.getNavigatorProvider();
        NavGraph navGraph = new NavGraph(provider.getNavigator(NavGraphNavigator.class));
        final Fragment hostFragment = activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        FragmentManager childFragmentManager = hostFragment.getChildFragmentManager();
        HiFragmentNavigator hiFragmentNavigator =
                new HiFragmentNavigator(activity, childFragmentManager, containerId, false);
        provider.addNavigator(hiFragmentNavigator);
        while (iterator.hasNext()) {
            Destination destination = iterator.next();

            switch (destination.destType) {
                case "activity":
                    ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);
                    ActivityNavigator.Destination activityNode = activityNavigator.createDestination();
                    activityNode.setId(destination.id);
                    activityNode.setComponentName(new ComponentName(activity.getPackageName(), destination.className));
                    navGraph.addDestination(activityNode);
                    break;
                case "fragment":
                    HiFragmentNavigator.Destination fragmentNode = hiFragmentNavigator.createDestination();
                    fragmentNode.setClassName(destination.className);
                    fragmentNode.setId(destination.id);
                    navGraph.addDestination(fragmentNode);
                    break;
                case "dialog":
                    DialogFragmentNavigator dialogFragmentNavigator = provider.getNavigator(DialogFragmentNavigator.class);
                    DialogFragmentNavigator.Destination dialogNode = dialogFragmentNavigator.createDestination();
                    dialogNode.setClassName(destination.className);
                    dialogNode.setId(destination.id);
                    navGraph.addDestination(dialogNode);
                    break;
                default:
                    break;
            }
            if (destination.asStarter) {
                navGraph.setStartDestination(destination.id);
            }
        }
        controller.setGraph(navGraph);
    }

    public static void builderBottomNav(BottomNavigationView bottomNavigationView) {
        String content = getDestinations(bottomNavigationView.getContext(), "main_tabs_config.json");
        BottomTab bottomTab = JSON.parseObject(content, BottomTab.class);

        if (bottomTab == null) {
            return;
        }
        List<BottomTab.Tab> tabs = bottomTab.tabs;
        Menu menu = bottomNavigationView.getMenu();
        for (BottomTab.Tab tab : tabs) {
            if (!tab.enable) {
                continue;
            }
            if (destinations.containsKey(tab.pageUrl)) {
                MenuItem add = menu.add(0, destinations.get(tab.pageUrl).id, tab.index, tab.title);
                add.setIcon(R.drawable.ic_dashboard_black_24dp);
            }
        }
    }
}