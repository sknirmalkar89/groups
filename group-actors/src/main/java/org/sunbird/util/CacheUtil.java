package org.sunbird.util;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.sunbird.Application;
import org.sunbird.cache.impl.RedisCache;
import org.sunbird.common.util.JsonKey;
import org.sunbird.models.ActorOperations;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.util.helper.PropertiesCache;
import scala.collection.JavaConverters;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class CacheUtil {

  private LoggerUtil logger = new LoggerUtil(CacheUtil.class);

  private Timeout timeout = new Timeout(Duration.create(10, TimeUnit.SECONDS));

  Map<String, Object> headerMap = new HashMap<>();

  public static int groupTtl;
  public static int userTtl;

  static {
    groupTtl =
        StringUtils.isNotEmpty(
                PropertiesCache.getInstance().getConfigValue(JsonKey.GROUPS_REDIS_TTL))
            ? Integer.parseInt(
                PropertiesCache.getInstance().getConfigValue(JsonKey.GROUPS_REDIS_TTL))
            : 3600000;
    userTtl =
        StringUtils.isNotEmpty(PropertiesCache.getInstance().getConfigValue(JsonKey.USER_REDIS_TTL))
            ? Integer.parseInt(PropertiesCache.getInstance().getConfigValue(JsonKey.USER_REDIS_TTL))
            : 3600000;
  }

  public CacheUtil() {
    List<String> reqIds = new ArrayList<>();
    reqIds.add(MDC.get(JsonKey.REQUEST_MESSAGE_ID));
    headerMap.put(JsonKey.REQUEST_MESSAGE_ID, reqIds);
  }
  /**
   * to call set cache
   *
   * @param key
   * @param value
   */
  public void setCache(String key, String value, int ttl) {
    Request req = new Request();
    req.setHeaders(headerMap);
    req.setOperation(ActorOperations.SET_CACHE.getValue());
    req.getRequest().put(JsonKey.KEY, key);
    req.getRequest().put(JsonKey.VALUE, value);
    req.getRequest().put(JsonKey.TTL, ttl);
    Application.getInstance()
        .getActorRef(ActorOperations.SET_CACHE.getValue())
        .tell(req, ActorRef.noSender());
  }

  /**
   * to call get cache
   *
   * @param key
   */
  public String getCache(String key, Map<String,Object> reqContext) {
    String value = null;
    Request req = new Request();
    req.setOperation(ActorOperations.GET_CACHE.getValue());
    req.getRequest().put(JsonKey.KEY, key);
    req.setHeaders(headerMap);
    try {
      Object object =
          getResponse(
              Application.getInstance().getActorRef(ActorOperations.GET_CACHE.getValue()), req, reqContext);
      if (object instanceof Response) {
        Response response = (Response) object;
        value = (String) response.get(JsonKey.VALUE);
      } else if (object instanceof Exception) {
        logger.error(reqContext, MessageFormat.format("getCache: Exception occurred with error message =  {0}", object));
      }
    } catch (Exception e) {
      logger.error(reqContext,MessageFormat.format("getCache: Exception occurred with error message =  {0}", e.getMessage()));
    }
    return value;
  }

  private Object getResponse(ActorRef actorRef, Request request, Map<String,Object> reqContext) {
    try {
      return Await.result(getFuture(actorRef, request), timeout.duration());
    } catch (Exception e) {
      logger.error(reqContext,MessageFormat.format("getResponse: Exception occurred with error message =  {0}", e.getMessage()));
    }
    return null;
  }

  private Future<Object> getFuture(ActorRef actorRef, Request request) {
    return Patterns.ask(actorRef, request, timeout);
  }

  /**
   * to call del cache
   *
   * @param key
   */
  public void delCache(String key) {
    Request req = new Request();
    req.setOperation(ActorOperations.DEL_CACHE.getValue());
    req.getRequest().put(JsonKey.KEY, key);
    req.setHeaders(headerMap);
    Application.getInstance()
        .getActorRef(ActorOperations.DEL_CACHE.getValue())
        .tell(req, ActorRef.noSender());
  }

  public void deleteCacheSync(String key, Map<String,Object> reqContext) {
    logger.info(reqContext,MessageFormat.format("delete cache in sync from redis for the id {0}", key));
    RedisCache.delete(
        JavaConverters.asScalaIteratorConverter(Arrays.asList(key).iterator()).asScala().toSeq());
  }
}
