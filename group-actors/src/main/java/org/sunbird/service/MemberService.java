package org.sunbird.service;

import java.util.List;
import org.sunbird.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.response.Response;

public interface MemberService {

  Response addMembers(List<Member> member) throws BaseException;

  Response removeMembers(List<String> memberId, String groupId) throws BaseException;
}
