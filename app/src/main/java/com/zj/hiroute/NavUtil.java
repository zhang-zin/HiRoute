package com.zj.hiroute;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.FragmentNavigator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zj.hiroute.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class NavUtil {

    /**
     * 根据json文件生成hashMap的节点对象
     *
     * @param context 上线文
     * @return destinationHashMap
     */
    public static HashMap<String, Destination> getDestinations(Context context) {
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open("destination.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder builder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            inputStream.close();

            HashMap<String, Destination> destinationHashMap = JSON.parseObject(builder.toString(), new TypeReference<HashMap<String, Destination>>() {
            }.getType());
            return destinationHashMap;
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
        HashMap<String, Destination> destinations = getDestinations(activity);
        Iterator<Destination> iterator = destinations.values().iterator();
        NavigatorProvider provider = controller.getNavigatorProvider();
        NavGraph navGraph = new NavGraph(provider.getNavigator(NavGraphNavigator.class));

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
                    FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
                    FragmentNavigator.Destination fragmentNode = fragmentNavigator.createDestination();
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
            }

            if (destination.asStarter) {
                navGraph.setStartDestination(destination.id);
            }

            controller.setGraph(navGraph);
        }
    }
}