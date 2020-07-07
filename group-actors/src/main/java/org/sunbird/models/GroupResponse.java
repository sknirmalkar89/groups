package org.sunbird.models;

import java.util.List;

public class GroupResponse extends Group {

  private List<Member> members;

  private String memberRole;

  public void setMemberRole(String role) {
    this.memberRole = role;
  }

  public String getMemberRole() {
    return memberRole;
  }

  public List<Member> getMembers() {
    return members;
  }

  public void setMembers(List<Member> members) {
    this.members = members;
  }
}
