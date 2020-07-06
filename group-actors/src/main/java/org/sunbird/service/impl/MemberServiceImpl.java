package org.sunbird.service.impl;

import java.util.List;
import org.sunbird.dao.MemberDao;
import org.sunbird.dao.impl.MemberDaoImpl;
import org.sunbird.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.response.Response;
import org.sunbird.service.MemberService;

public class MemberServiceImpl implements MemberService {

  private static MemberDao memberDao = MemberDaoImpl.getInstance();
  private static MemberService memberService = null;

  public static MemberService getInstance() {
    if (memberService == null) {
      memberService = new MemberServiceImpl();
    }
    return memberService;
  }

  @Override
  public Response addMembers(List<Member> member) throws BaseException {
    Response response = memberDao.addMembers(member);
    return response;
  }

  @Override
  public Response removeMembers(List<String> memberId, String groupId) throws BaseException {
    return null;
  }
}
