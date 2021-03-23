package com.zj.nav_compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.zj.nav_annotations.Destination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.zj.nav_annotations.Destination"})
public class NavProcessor extends AbstractProcessor {
    /**
     * 在  /app/main/assets/目录下 生成路由信息的json文件
     * {
     * "main/tabs/dashboard": {
     * "asStarter": false,
     * "className": "com.zj.hiroute.ui.dashboard.DashboardFragment",
     * "id": 160002873,
     * "destType": "fragment"
     * },
     * "main/tabs/home": {
     * "asStarter": true,
     * "className": "com.zj.hiroute.ui.home.HomeFragment",
     * "id": 807054821,
     * "destType": "fragment"
     * },
     * "main/tabs/notifications": {
     * "asStarter": false,
     * "className": "com.zj.hiroute.ui.notifications.NotificationsFragment",
     * "id": 872540561,
     * "destType": "fragment"
     * }
     * }
     */

    private Messager messager;
    private Filer filer;
    private static final String PAGE_TYPE_ACTIVITY = "Activity";
    private static final String PAGE_TYPE_FRAGMENT = "Fragment";
    private static final String PAGE_TYPE_DIALOG = "Dialog";
    private static final String OUTPUT_FILE_NAME = "destination.json";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE, "enter init...\n");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Destination.class);
        if (!elements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(elements, Destination.class, destMap);
            try {
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                // /app/build/intermediate/
                // /app/main/assets/
                String resourcePath = resource.toUri().getPath();
                messager.printMessage(Diagnostic.Kind.NOTE, "resourcePath " + resourcePath + "\n");

                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
                String assetsPath = appPath + "src/main/assets";
                File file = new File(assetsPath);
                if (!file.exists()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "mkdirs " + assetsPath + "\n");
                    file.mkdirs();
                }
                File outPutFile = new File(assetsPath, OUTPUT_FILE_NAME);
                if (outPutFile.exists()) {
                    outPutFile.delete();
                }
                outPutFile.createNewFile();
                String content = JSON.toJSONString(destMap);
                FileOutputStream fileOutputStream = new FileOutputStream(outPutFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(content);
                outputStreamWriter.flush();

                outputStreamWriter.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void handleDestination(Set<? extends Element> elements, Class<Destination> destinationClass, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            // 全类名
            String className = typeElement.getQualifiedName().toString();

            Destination annotation = typeElement.getAnnotation(destinationClass);
            String pageUrl = annotation.pageUrl();
            boolean asStarter = annotation.asStarter();

            int id = Math.abs(className.hashCode());

            //Activity，Dialog，Fragment
            String destType = getDestinationType(typeElement);
            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "不同的页面不允许使用相同的pageUrl：" + pageUrl);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("className", className);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("id", id);
                jsonObject.put("destType", destType);

                destMap.put(pageUrl, jsonObject);
            }
        }
    }

    /**
     * 获取被注解的类的类型
     *
     * @param typeElement 被注解的类
     * @return destType
     */
    private String getDestinationType(TypeElement typeElement) {

        TypeMirror typeMirror = typeElement.getSuperclass();
        String superClass = typeMirror.toString();
        //androidx.fragment.app.Fragment
        if (superClass.contains(PAGE_TYPE_ACTIVITY.toLowerCase())) {
            return PAGE_TYPE_ACTIVITY.toLowerCase();
        } else if (superClass.contains(PAGE_TYPE_FRAGMENT.toLowerCase())) {
            return PAGE_TYPE_FRAGMENT.toLowerCase();
        } else if (superClass.contains(PAGE_TYPE_DIALOG)) {
            return PAGE_TYPE_DIALOG.toLowerCase();
        }

        //这个父类的类型是类的类型，或者是接口的类型
        if (typeMirror instanceof DeclaredType) {
            Element element = ((DeclaredType) typeMirror).asElement();
            if (element instanceof TypeElement) {
                return getDestinationType((TypeElement) element);
            }
        }
        return null;
    }
}
