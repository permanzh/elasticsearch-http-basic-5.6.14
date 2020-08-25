package com.asquera.elasticsearch.plugins.http;

import com.asquera.elasticsearch.plugins.http.auth.Client;
import com.asquera.elasticsearch.plugins.http.auth.InetAddressWhitelist;
import com.asquera.elasticsearch.plugins.http.auth.ProxyChains;
import com.asquera.elasticsearch.plugins.http.auth.XForwardedFor;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import com.asquera.elasticsearch.plugins.http.common.Base64;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.RestRequest.Method;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.net.InetSocketAddress;

import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.UNAUTHORIZED;

// # possible http config
// http.basic.user: admin
// http.basic.password: password
// http.basic.ipwhitelist: ["localhost", "somemoreip"]
// http.basic.xforward: "X-Forwarded-For"
// # if you use javascript
// # EITHER $.ajaxSetup({ headers: { 'Authorization': "Basic " + credentials }});
// # OR use beforeSend in  $.ajax({
// http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
//
/**
 * @author Florian Gilcher (florian.gilcher@asquera.de)
 * @author Peter Karich
 */
public class HttpBasicServer /*implements RestHandler*/{

    /*private final String user;
    private final String password;
    private final InetAddressWhitelist whitelist;
    private final ProxyChains proxyChains;
    private final String xForwardHeader;
    private final boolean log;

    private final Logger logger ;

    @Inject
    public HttpBasicServer(Settings settings){
        super();
        this.logger = Loggers.getLogger(getClass(), settings);

//        restController.registerHandler(Method.GET, "/basic_server", this);

        *//*this.user = settings.get("http.basic.user", "admin");
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


        logger.info("using {}:{} with whitelist: {}, xforward header field: {}, trusted proxy chain: {}",
                user, password, whitelist, xForwardHeader, proxyChains);

        logger.info("HttpBasicServer Setting 111111111111111111111111111111111111111111 ... ");*//*


    }*/

   /* @Override
    public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) {
        logger.info("HttpBasicServer handleRequest ... ");
    }

    public static void dealRequest(RestRequest request, RestChannel channel,
                                   NodeClient client,RestHandler restHandler) throws Exception{

        restHandler.handleRequest(request, channel, client);

    }
*/

    /*@Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        return null;
    }*/



    /*@Override
    public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) {
        if (log) {
            logRequest(request);
        }

        System.out.println("prepareRequest...");
        if (authorized(request)) {
            // 转发es请求
            System.out.println("authorized(request)...");
        } else if (healthCheck(request)) { // display custom health check page when unauthorized (do not display too much server info)
            System.out.println("healthCheck(request)...");
            return channel -> channel.sendResponse(new BytesRestResponse(OK, "{\"OK\":{}}"));
        } else {
            System.out.println("eeelse...");
            logUnAuthorizedRequest(request);
            BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, "Authentication Required");
            response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            return channel -> channel.sendResponse(response);
        }
    }*/

    /*@Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient nodeClient) {


    }*/



    /*@Inject
    public HttpBasicServer(Settings settings, Environment environment, HttpServerTransport transport,
            RestController restController,
            NodeService nodeService) {
        super(settings, environment, transport, restController, nodeService);

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
    }*/

    /*public void internalDispatchRequest(final RestRequest request, final RestChannel channel) {
        if (log) {
            logRequest(request);
        }

        if (authorized(request)) {
            super.internalDispatchRequest(request, channel);
        } else if (healthCheck(request)) { // display custom health check page when unauthorized (do not display too much server info)
            channel.sendResponse(new BytesRestResponse(OK, "{\"OK\":{}}"));
        } else {
            logUnAuthorizedRequest(request);
            BytesRestResponse response = new BytesRestResponse(UNAUTHORIZED, "Authentication Required");
            response.addHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            channel.sendResponse(response);
        }
    }*/

    // @param an http method
    // @returns True iff the method is one of the methods used for health check
    /*private boolean isHealthCheckMethod(final RestRequest.Method method){
        final RestRequest.Method[] healthCheckMethods = { RestRequest.Method.GET, RestRequest.Method.HEAD };
        return Arrays.asList(healthCheckMethods).contains(method);
    }

    // @param an http Request
    // @returns True iff we check the root path and is a method allowed for healthCheck
    private boolean healthCheck(final RestRequest request) {
        return request.path().equals("/") && isHealthCheckMethod(request.method());
    }

  *//**
   *
   *
   * @param request
   * @return true if the request is authorized
   *//*
    private boolean authorized(final RestRequest request) {
      return allowOptionsForCORS(request) ||
        authBasic(request) || ipAuthorized(request);
    }

  *//**
   *
   *
   * @param request
   * @return true iff the client is authorized by ip
   *//*
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


  *//*
   *
   *
   * @param request
   * @return the IP adress of the direct client
    *//*
    private InetAddress getAddress(RestRequest request) {
        return ((InetSocketAddress) request.getRemoteAddress()).getAddress();
    }


    *//*
     * https://en.wikipedia.org/wiki/Cross-origin_resource_sharing the
     * specification mandates that browsers “preflight” the request, soliciting
     * supported methods from the server with an HTTP OPTIONS request
     *//*
    private boolean allowOptionsForCORS(RestRequest request) {
        // in elasticsearch.yml set
        // http.cors.allow-headers: "X-Requested-With, Content-Type, Content-Length, Authorization"
        if (request.method() == Method.OPTIONS) {
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
*/
}