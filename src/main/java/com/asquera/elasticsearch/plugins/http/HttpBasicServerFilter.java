package com.asquera.elasticsearch.plugins.http;

import com.asquera.elasticsearch.plugins.http.auth.Client;
import com.asquera.elasticsearch.plugins.http.auth.InetAddressWhitelist;
import com.asquera.elasticsearch.plugins.http.auth.ProxyChains;
import com.asquera.elasticsearch.plugins.http.auth.XForwardedFor;
import com.asquera.elasticsearch.plugins.http.common.Base64;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.tasks.TaskId;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpBasicServerFilter implements ActionFilter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:SS");

    private final Logger logger ;

     private final String user;
     private final String password;
     private final InetAddressWhitelist whitelist;
     private final ProxyChains proxyChains;
     private final String xForwardHeader;
     private final boolean log;

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }



    @Inject
    public HttpBasicServerFilter(Settings settings){
        super();
        this.logger = Loggers.getLogger(getClass(), settings);

        this.user = settings.get("http.basic.user", "admin");
        this.password = settings.get("http.basic.password", "admin_pw");
        final boolean whitelistEnabled = settings.getAsBoolean("http.basic.ipwhitelist", true);
        String [] whitelisted = new String[0];
        if (whitelistEnabled) {
            whitelisted = settings.getAsArray("http.basic.ipwhitelist", new String[]{"localhost", "127.0.0.1"});
        }

        this.whitelist = new InetAddressWhitelist(whitelisted);
        this.proxyChains = new ProxyChains(
                settings.getAsArray(
                        "http.basic.trusted_proxy_chains", new String[]{""}));

        // for AWS load balancers it is X-Forwarded-For -> hmmh does not work
        this.xForwardHeader = settings.get("http.basic.xforward", "");
        this.log = settings.getAsBoolean("http.basic.log", true);
        Loggers.getLogger(getClass()).info("using {}:{} with whitelist: {}, xforward header field: {}, trusted proxy chain: {}",
                user, password, whitelist, xForwardHeader, proxyChains);
        System.out.println("HttpBasicServerFilter Setting 构造器 ... ");
    }



    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply
            (Task task, String action,
             Request request, ActionListener<Response> listener,
             ActionFilterChain<Request, Response> chain) {

//        System.out.println("request!! action="+action+",time = "+ sdf.format(new Date()));//我们加的日志

//        System.out.println(request.toString());

        String description = request.getDescription();
        TaskId parentTask = request.getParentTask();

        boolean shouldStoreResult = request.getShouldStoreResult();
//        System.out.println("description : "+ description);
//        System.out.println("parentTask : "+ parentTask);
//        System.out.println("shouldStoreResult : "+ shouldStoreResult);

        TransportAddress transportAddress = request.remoteAddress();
        if (null != transportAddress) {


//            System.out.println("transportAddress toString : " + null == transportAddress.toString() ? "" : transportAddress.toString());
//            System.out.println("transportAddress host : " + null == transportAddress.getHost() ? "" : transportAddress.getHost());
//            System.out.println("transportAddress port : " + transportAddress.getPort());
//            System.out.println("transportAddress address : " + null == transportAddress.getAddress() ? "" : transportAddress.getAddress());
        }

        if (log) {
//            logRequest(request);
        }



        if(action.equals("indices:data/read/search") || action.equals("indices:data/read/search/template")) {

//            System.out.println(Thread.currentThread().getId() + "==" + action + "===" + request.toString() + ":" + listener.toString());
//            System.out.println("===replacing Action Lisener====");
//            System.out.println(task.getId() + "," + task.getParentTaskId().getId() + "," + task.getStatus() + "," + task.getType());
//            System.out.println("remoteAddress : " + request.remoteAddress());

            chain.proceed(task, action, request, new AnotherActionListener(listener,request,task));
        } else {
//            System.out.println("===NOT replacing Action Lisener====");
            chain.proceed(task, action, request, listener);
        }

    }

    public static class MyActionListener implements ActionListener {

        private ActionListener listener;

        private Task task;

        public MyActionListener(ActionListener listener, Task task) {
            this.listener = listener;
            this.task = task;
        }

        @Override
        public void onResponse(Object o) {
            System.out.println(Thread.currentThread().getId() + "==onResponse()===" + o.toString());
            System.out.println(task.getStartTime());
            System.out.println(System.currentTimeMillis());
            listener.onResponse(o);
        }

        @Override
        public void onFailure(Exception e) {
            System.out.println(Thread.currentThread().getId() + "==onError()===");
            listener.onFailure(e);
        }
    }

    class AnotherActionListener<Response extends ActionResponse, Request extends ActionRequest> implements ActionListener<Response> {
        private ActionListener<Response> actionListener;
        private Request request;
        private long startTime;
        private Task task;

        public AnotherActionListener(ActionListener<Response> actionListener, Request request,Task task) {
            this.actionListener = actionListener;
            this.request = request;
            this.task = task;
        }

        public void onResponse(Response response) {

            /*System.out.println(Thread.currentThread().getId() + "==onResponse()===" + response.toString());
            System.out.println(task.getStartTime());
            System.out.println(System.currentTimeMillis());*/

            //对es的响应进行分类和更改，部分response不支持构造函数，需要看源码查找构造方法
            if (response instanceof GetIndexResponse) {
                /*GetIndexResponse temp= null;
                try {
                    temp = GetIndexResponse.fromXContent(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                actionListener.onResponse(response);
            }
            {
                actionListener.onResponse(response);
            }
        }

        public void onFailure(Exception e) {
            System.out.println(Thread.currentThread().getId() + "==onError()===");
            actionListener.onFailure(e);
        }

    }

    // @param an http method
    // @returns True iff the method is one of the methods used for health check
    private boolean isHealthCheckMethod(final RestRequest.Method method){
        final RestRequest.Method[] healthCheckMethods = { RestRequest.Method.GET, RestRequest.Method.HEAD };
        return Arrays.asList(healthCheckMethods).contains(method);
    }

    // @param an http Request
    // @returns True iff we check the root path and is a method allowed for healthCheck
    private boolean healthCheck(final RestRequest request) {
        return request.path().equals("/") && isHealthCheckMethod(request.method());
    }

    /**
     *
     *
     * @param request
     * @return true if the request is authorized
     */
    private boolean authorized(final RestRequest request) {
        return allowOptionsForCORS(request) ||
                authBasic(request) || ipAuthorized(request);
    }

    /**
     *
     *
     * @param request
     * @return true iff the client is authorized by ip
     */
    private boolean ipAuthorized(final RestRequest request) {
        boolean ipAuthorized = false;
        String xForwardedFor = request.header(xForwardHeader);
        Client client = new Client(getAddress(request),
                whitelist,
                new XForwardedFor(xForwardedFor),
                proxyChains);
        ipAuthorized = client.isAuthorized();
        if (ipAuthorized) {
            if (log) {
                String template = "Ip Authorized client: {}";
                Loggers.getLogger(getClass()).info(template, client);
            }
        } else {
            String template = "Ip Unauthorized client: {}";
            Loggers.getLogger(getClass()).error(template, client);
        }
        return ipAuthorized;
    }

    public String getDecoded(RestRequest request) {
        String authHeader = request.header("Authorization");
        if (authHeader == null)
            return "";

        String[] split = authHeader.split(" ", 2);
        if (split.length != 2 || !split[0].equals("Basic"))
            return "";
        try {
            return new String(Base64.decode(split[1]));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean authBasic(final RestRequest request) {
        String decoded = "";
        try {
            decoded = getDecoded(request);
            if (!decoded.isEmpty()) {
                String[] userAndPassword = decoded.split(":", 2);
                String givenUser = userAndPassword[0];
                String givenPass = userAndPassword[1];
                if (this.user.equals(givenUser) && this.password.equals(givenPass))
                    return true;
            }
        } catch (Exception e) {
            logger.warn("Retrieving of user and password failed for " + decoded + " ," + e.getMessage());
        }
        return false;
    }


    /**
     *
     *
     * @param request
     * @return the IP adress of the direct client
     */
    private InetAddress getAddress(RestRequest request) {
        return ((InetSocketAddress) request.getRemoteAddress()).getAddress();
    }


    /**
     * https://en.wikipedia.org/wiki/Cross-origin_resource_sharing the
     * specification mandates that browsers “preflight” the request, soliciting
     * supported methods from the server with an HTTP OPTIONS request
     */
    private boolean allowOptionsForCORS(RestRequest request) {
        // in elasticsearch.yml set
        // http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
        if (request.method() == RestRequest.Method.OPTIONS) {
//            Loggers.getLogger(getClass()).error("CORS type {}, address {}, path {}, request {}, content {}",
//                    request.method(), getAddress(request), request.path(), request.params(), request.content().toUtf8());
            return true;
        }
        return false;
    }

    public void logRequest(final RestRequest request) {
        String addr = getAddress(request).getHostAddress();
        String t = "Authorization:{}, type: {}, Host:{}, Path:{}, {}:{}, Request-IP:{}, " +
                "Client-IP:{}, X-Client-IP{}";
        logger.info(t,
                request.header("Authorization"),
                request.method(),
                request.header("Host"),
                request.path(),
                xForwardHeader,
                request.header(xForwardHeader),
                addr,
                request.header("X-Client-IP"),
                request.header("Client-IP"));
    }

    public void logUnAuthorizedRequest(final RestRequest request) {
        String addr = getAddress(request).getHostAddress();
        String t = "UNAUTHORIZED type:{}, address:{}, path:{}, request:{},"
                + "content:{}, credentials:{}";
        Loggers.getLogger(getClass()).error(t,
                request.method(), addr, request.path(), request.params(),
                request.content().utf8ToString(), getDecoded(request));
    }


}
