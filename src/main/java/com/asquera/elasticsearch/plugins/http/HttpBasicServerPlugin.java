package com.asquera.elasticsearch.plugins.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import static com.asquera.elasticsearch.plugins.http.util.CommonUtils.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 */
public class HttpBasicServerPlugin extends Plugin implements ActionPlugin{



    private static String actionName = "http-basic-server-plugin";

    public Settings settings;
    public static Client client;
    private static final String HTTP_BASIC_ENABLED = "http.basic.enabled";

    private final Logger logger ;

    private boolean enabled;

    public Properties prop;

    @Inject
    public HttpBasicServerPlugin(Settings settings) {
        super();
        this.settings = settings;
        this.logger = Loggers.getLogger(getClass(), settings);


        String path = "";
        try {
            path = getPath() + File.separator + "config.properties";
            File configFile = new File(path);
            if (configFile.exists()) {
                Properties configPro = new Properties();
                configPro.load(new FileInputStream(configFile));
                this.prop = configPro;
            } else {
                throw new FileNotFoundException("Elasticsearch Http Basic config.properties not exist ! ");
            }
        } catch (Exception ex) {
            logger.error("Get Elasticsearch Http Basic config.properties error", ex);
        }
        try {
            this.enabled = Booleans.parseBooleanExact(prop.getProperty("http.basic.enabled"), false);
            if (enabled) {
                Loggers.setLevel(this.logger, removeQuote(prop.getProperty("http.basic.loglevel", "debug"), "\""));
                this.logger.info(actionName + " start ......");
                logger.info("path : -> " + path);
            }
        }catch (Exception ex){
            this.enabled = false;
            logger.error("Get Elasticsearch Http Basic config.properties error", ex);
        }
    }

    /**
     * 接口实现类可以向上转型为接口
     * 添加拦截器
     *
     * @return
     */
    /*@Override
    public List<Class<? extends ActionFilter>> getActionFilters() {
        List<Class<? extends ActionFilter>> list = new ArrayList<>();
        list.add(HttpBasicServerFilter.class);
        return list;
    }*/

    /**
     *  注入插件
     *
     * @return
     */
    /*@Override
    public Collection<Module> createGuiceModules() {
        Collection<Module> modules = new ArrayList<>();
        if (settings.getAsBoolean(HTTP_BASIC_ENABLED, enabledByDefault)) {
            modules.add(
                binder -> binder.bind(MyRestHandler.class).asEagerSingleton()
            );
        }
        return modules;
    }*/

    /**
     * 添加yml配置文件字段
     *
     * @return
     */
    @Override
    public List<Setting<?>> getSettings() {
        List<Setting<?>> settingList = new ArrayList<>();
//        settingList.addAll(super.getSettings());

        try {
            Iterator<Object> iterator = prop.keySet().iterator();
            while (iterator.hasNext()){
                Object key = iterator.next();
                Object value = prop.get(key);
                if (StringUtils.startsWith(String.valueOf(key),"http.basic")){
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.valueOf(key) + "\t" + String.valueOf(value));
                    }
                    switch (String.valueOf(key)){
                        case HTTP_BASIC_ENABLED:
                            settingList.add(Setting.boolSetting(HTTP_BASIC_ENABLED,true, Setting.Property.NodeScope,Setting.Property.Shared));
                            break;
                        default:
//                        settingList.add(Setting.simpleString(key,value, Setting.Property.NodeScope,Setting.Property.Shared));
                            settingList.add(stringSetting(String.valueOf(key),String.valueOf(value), Setting.Property.NodeScope,Setting.Property.Shared));
                            break;
                    }
                }
            }
        }catch (Exception ex){
            logger.error("Get Elasticsearch Http Basic config.properties error",ex);
        }

        /*Map<String, String> settingMap = settings.getAsMap();

        for(Map.Entry<String,String> entry : settingMap.entrySet()){
            String key = entry.getKey();
            if (key.startsWith("http.basic")) {
                String value = entry.getValue();
                logger.info(key + "\t" + value);

            }
        }*/
        return settingList;
    }


    private String getPath()
    {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if(System.getProperty("os.name").contains("dows"))
        {
            path = path.substring(1,path.length());
        }
        if(path.contains("jar"))
        {
            path = path.substring(0,path.lastIndexOf("."));
            return path.substring(0,path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }

    private Setting<String> stringSetting(String key, String defaultValue, Setting.Property... properties){
        return new Setting<>(key, (s) -> defaultValue, Function.identity(), properties);
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool, ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry) {
        this.client = client;
        return super.createComponents(client, clusterService, threadPool, resourceWatcherService, scriptService, xContentRegistry);
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:SS");

    @Override
    public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
        return (RestHandler r) ->{
//            threadContext.putHeader("request_time", sdf.format(new Date()) + "");
//            threadContext.putHeader("response_time", sdf.format(new Date()) + "");
//            System.out.println(Thread.currentThread().getId() + "===getRestHandlerWrapper ====" + "request " + sdf.format(new Date()) + " ====" + r.toString());
            //LOGGER.error(r.getClass().getClassLoader().toString());
            //LOGGER.error(PallasPlugin.class.getClassLoader().toString());
//            String className = r.getClass().getName();

            return  enabled ? new MyRestHandler(r,prop) : r;

            /*if (className.contains(".RestSearchAction") || className.contains(".RestSearchTemplateAction")) {
                return new MyRestHandler(r);
            } else {
                return r;
            }*/
        };
    }



    /*@Override
    public List<RestHandler> getRestHandlers
            (Settings settings, RestController restController,
             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings,
             SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
             Supplier<DiscoveryNodes> nodesInCluster) {

        return Collections.singletonList(new HttpBasicServer(settings));
    }*/



    /*@Override
    public Settings additionalSettings() {
        Settings.Builder builder = Settings.builder();
        return builder.build();
    }




    */


   /* public Map<String, Supplier<HttpServerTransport>> getHttpTransports(Settings settings, ThreadPool threadPool,
                                                                        BigArrays bigArrays, CircuitBreakerService circuitBreakerService,
                                                                        NamedWriteableRegistry namedWriteableRegistry,
                                                                        NamedXContentRegistry xContentRegistry,
                                                                        NetworkService networkService, HttpServerTransport.Dispatcher dispatcher) {
        return Collections.singletonMap("http-basic",() -> new HttpBasicServer(settings,dispatcher));
    }*/

    public String name() {
        return "http-basic-server-plugin";
    }

    public String description() {
        return "HTTP Basic Server Plugin";
    }

   /* @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            modules.add(HttpBasicServerModule.class);
        }
        return modules;
    }*/

    /*@Override
    public Collection<Class<? extends LifecycleComponent>> getGuiceServiceClasses() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            services.add(HttpBasicServer.class);
        }
        return services;
    }*/

   /* @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean("http.basic.enabled", enabledByDefault)) {
            services.add(HttpBasicServer.class);
        }
        return services;
    }*/


}
