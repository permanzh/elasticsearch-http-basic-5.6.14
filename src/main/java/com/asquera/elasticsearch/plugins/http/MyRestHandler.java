package com.asquera.elasticsearch.plugins.http;

import com.asquera.elasticsearch.plugins.http.auth.Client;
import com.asquera.elasticsearch.plugins.http.auth.InetAddressWhitelist;
import com.asquera.elasticsearch.plugins.http.auth.ProxyChains;
import com.asquera.elasticsearch.plugins.http.auth.XForwardedFor;
import com.asquera.elasticsearch.plugins.http.common.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import static com.asquera.elasticsearch.plugins.http.util.CommonUtils.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.UNAUTHORIZED;

public class MyRestHandler implements RestHandler {

    private RestHandler restHandler;

    private final String user;
    private final String password;
    private final InetAddressWhitelist whitelist;
    private final ProxyChains proxyChains;
    private final String xForwardHeader;
    public static boolean log = false;
    private final boolean authorization;
    public static String logLevel;

    private final Logger logger ;

    private static final RestRequest.Method[] healthCheckMethods = { RestRequest.Method.GET, RestRequest.Method.HEAD };
    public static final List LOCLA_HOST_LIST = Collections.unmodifiableList(Arrays.asList("127.0.0.1","localhost","::1","0:0:0:0:0:0:0:1","0.0.0.1"));

    public static Boolean WHITE_LIST_CONTAIN_LOCAL;

    MyRestHandler(RestHandler restHandler,Properties prop) {
        this.restHandler = restHandler;
        this.logger = Loggers.getLogger(getClass());
        this.logLevel = removeQuote(prop.getProperty("http.basic.loglevel","debug"),"\"");

        Loggers.setLevel(this.logger,this.logLevel);

        this.user = removeQuote(prop.getProperty("http.basic.user","admin"),"\"");
        this.password = removeQuote(prop.getProperty("http.basic.password", "admin_pw"),"\"");

        this.authorization = getAsBoolean(prop,"http.basic.authorization",false);
        this.log = getAsBoolean(prop,"http.basic.log", true);
        final boolean whitelistEnabled = StringUtils.isNotBlank(prop.getProperty("http.basic.ipwhitelist"));
        String [] whitelisted = new String[0];
        if (whitelistEnabled) {
            whitelisted = getAsArray(prop,"http.basic.ipwhitelist", new String[]{"localhost", "127.0.0.1"},true);

            this.WHITE_LIST_CONTAIN_LOCAL = Arrays.asList(whitelisted).stream().anyMatch(item -> LOCLA_HOST_LIST.contains(item));
            if (logger.isDebugEnabled()) {
                logger.debug("WHITE_LIST_CONTAIN_LOCAL : " + WHITE_LIST_CONTAIN_LOCAL);
                logger.debug("http.basic.ipwhitelist : " + StringUtils.join(whitelisted, ","));
            }
        }
        this.whitelist = new InetAddressWhitelist(whitelisted);
        this.proxyChains = new ProxyChains(
                getAsArray(prop,"http.basic.trusted_proxy_chains", new String[]{""},true));

        // for AWS load balancers it is X-Forwarded-For -> hmmh does not work
        this.xForwardHeader = prop.getProperty("http.basic.xforward", "");

        Iterator<Object> iterator = prop.keySet().iterator();

        if (logger.isDebugEnabled()) {
            logger.debug("------------------------------------------------------ config start --------------------------------------------------------");
            while (iterator.hasNext()) {
                Object next = iterator.next();
                String key = String.valueOf(next);
                if (StringUtils.startsWith(key, "http.basic")
                        || StringUtils.startsWith(key, "http.cors")) {
                    logger.debug(key + "\t" + prop.get(key));
                }
            }
            logger.debug("------------------------------------------------------ config end  --------------------------------------------------------");

            logger.debug("using {}:{} with whitelist: {}, xforward header field: {}, trusted proxy chain: {}",
                    user, password, whitelist, xForwardHeader, proxyChains);
        }
    }

    @Override
    public void handleRequest(RestRequest request, RestChannel channel,
                              NodeClient client) throws Exception {
        //request.params().put("UUID", UUID.randomUUID().toString());
        if (logger.isDebugEnabled()) {
            logger.debug("request.params() : " + request.params());
            Map<String, List<String>> headers = request.getHeaders();
            logger.debug("------------------------------------------------------ request header start --------------------------------------------------------");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                logger.debug(entry.getKey() + "\t" + StringUtils.join(entry.getValue(), ","));
            }
            logger.debug("------------------------------------------------------ request header end --------------------------------------------------------");

            logger.debug("request.getHeaders() : " + headers);
            logger.debug("request.getRemoteAddress() : " + request.getRemoteAddress().toString());
            logger.debug("request.getLocalAddress() : " + request.getLocalAddress().toString());
            InetAddress address = getAddress(request);
            logger.debug("request getAddress " + address.toString());

            logger.debug("log : " + log);

            logger.debug("request : " + request.toString());
        }
        if (log) {
            logRequest(request);
        }


        if (authorized(request)) {
            logger.info("---------------------- authorized(request) ---------------------------");
            restHandler.handleRequest(request,channel,client);
        } else if (healthCheck(request)) { // display custom health check page when unauthorized (do not display too much server info)
            logger.info("---------------------- healthCheck(request) ---------------------------");
            channel.sendResponse(new BytesRestResponse(OK, "{\"OK\":{}}"));
        } else {
           logger.info("---------------------- logUnAuthorizedRequest(request) ---------------------------");
            logUnAuthorizedRequest(request);
            BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, "Authentication Required");
            response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            channel.sendResponse(response);
        }
    }

    // @param an http method
    // @returns True iff the method is one of the methods used for health check
    private boolean isHealthCheckMethod(final RestRequest.Method method){
        if (logger.isDebugEnabled()) {
            logger.debug("isHealthCheckMethod method : " + method.name());
        }
        return Arrays.asList(healthCheckMethods).contains(method);
    }

    // @param an http Request
    // @returns True iff we check the root path and is a method allowed for healthCheck
    private boolean healthCheck(final RestRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("healthCheck request.path() : " + request.path());
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug("ipAuthorized xForwardedFor : " + xForwardedFor);
        }
        Client client = new Client(getAddress(request),
                whitelist,
                new XForwardedFor(xForwardedFor),
                proxyChains);
        ipAuthorized = client.isAuthorized();
        if (logger.isDebugEnabled()) {
            logger.debug("ipAuthorized client : " + client.toString());
            logger.debug("ipAuthorized ipAuthorized : " + ipAuthorized);
        }
        if (ipAuthorized) {
            if (log) {
                String template = "Ip Authorized client: {}";
                logger.info(template, client);
            }
        } else {
            if (log) {
                String template = "Ip Unauthorized client: {}";
                logger.error(template, client);
            }
        }
        return ipAuthorized;
    }

    private String getDecoded(RestRequest request) {
        String authHeader = request.header("Authorization");
        if (logger.isDebugEnabled()) {
            logger.debug("getDecoded authHeader Authorization : " + authHeader);
        }
        if (authHeader == null)
            return "";

        String[] split = authHeader.split(" ", 2);
        if (split.length != 2 || !split[0].equals("Basic"))
            return "";
        try {
            String base64decode = new String(Base64.decode(split[1]));
            if (logger.isDebugEnabled()) {
                logger.debug("getDecoded base64decode ->  " + base64decode);
            }
            return base64decode;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean authBasic(final RestRequest request) {

        if (!authorization){
            if (log) {
                logger.info("AuthBasic is not permitted ! ");
            }
            return false;
        }
        String decoded = "";
        try {
            decoded = getDecoded(request);
            if (logger.isDebugEnabled()) {
                logger.debug("authBasic decoded : " + decoded);
            }
            if (!decoded.isEmpty()) {
                String[] userAndPassword = decoded.split(":", 2);
                String givenUser = userAndPassword[0];
                String givenPass = userAndPassword[1];
                if (this.user.equals(givenUser) && this.password.equals(givenPass)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Retrieving of user and password failed for " + decoded + " ," + e.getMessage());
        }
        return false;
    }

    /*
     *
     *
     * @param request
     * @return the IP adress of the direct client
     */
    private InetAddress getAddress(RestRequest request) {
        return ((InetSocketAddress) request.getRemoteAddress()).getAddress();
    }

    /*
     * https://en.wikipedia.org/wiki/Cross-origin_resource_sharing the
     * specification mandates that browsers “preflight” the request, soliciting
     * supported methods from the server with an HTTP OPTIONS request
     */
    private boolean allowOptionsForCORS(RestRequest request) {
        // in elasticsearch.yml set
        // http.cors.allow-headers:
        // "X-Requested-With, Content-Type, Content-Length, Authorization"
        if (logger.isDebugEnabled()) {
            logger.debug("allowOptionsForCORS : " + request.method().name());
        }

        if (request.method() == RestRequest.Method.OPTIONS) {
            logger.error("CORS type {}, address {}, path {}, request {}, content {}",
                    request.method(), getAddress(request), request.path(), request.params(), request.content().utf8ToString());
            return true;
        }
        return false;
    }

    private void logRequest(final RestRequest request) {
        String addr = getAddress(request).getHostAddress();
        String t = "Authorization:{}, type: {}, Host:{}, Path:{}, {}:{}, Request-IP:{}, " +
                "Client-IP:{}, X-Client-IP:{}";
        logger.info(t,
                request.header("Authorization"),
                request.method(),
                request.header("Host"),
                request.path(),
                StringUtils.defaultIfBlank(xForwardHeader,""),
                StringUtils.defaultIfBlank(request.header(xForwardHeader),""),
                StringUtils.defaultIfBlank(addr,""),
                StringUtils.defaultIfBlank(request.header("Client-IP"),""),
                StringUtils.defaultIfBlank(request.header("X-Client-IP"),""));
    }

    private void logUnAuthorizedRequest(final RestRequest request) {
        String addr = getAddress(request).getHostAddress();
        String t = "UNAUTHORIZED type:{}, address:{}, path:{}, request:{},"
                + "content:{}, credentials:{}";
        logger.error(t,
                request.method(), addr, request.path(), request.params(),
                request.content().utf8ToString(), getDecoded(request));
    }



}
