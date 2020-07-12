package org.sunbird.models;

import java.util.Map;

public class ActivitySearchRequestConfig {

  private String apiUrl;
  private Map<String, String> requestHeader;
  private SearchRequest searchRequest;
  private String response;
  private String identifierKey;

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public Map<String, String> getRequestHeader() {
    return requestHeader;
  }

  public void setRequestHeader(Map<String, String> requestHeader) {
    this.requestHeader = requestHeader;
  }

  public SearchRequest getSearchRequest() {
    return searchRequest;
  }

  public void setSearchRequest(SearchRequest searchRequest) {
    this.searchRequest = searchRequest;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public String getIdentifierKey() {
    return identifierKey;
  }

  public void setIdentifierKey(String identifierKey) {
    this.identifierKey = identifierKey;
  }
}
