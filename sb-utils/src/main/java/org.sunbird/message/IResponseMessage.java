package org.sunbird.message;

/** This interface will hold all the response key and message */
public interface IResponseMessage {

  String INVALID_REQUESTED_DATA = "INVALID_REQUESTED_DATA";
  String INVALID_OPERATION_NAME = "INVALID_OPERATION_NAME";
  String INTERNAL_ERROR = "INTERNAL_ERROR";
  String SERVER_ERROR = "SERVER_ERROR";
  String ID_ALREADY_EXISTS = "ID_ALREADY_EXISTS";
  String MISSING_MANDATORY_PARAMS = "MISSING_MANDATORY_PARAMS";
  String DATA_TYPE_ERROR = "DATA_TYPE_ERROR";
  String EMPTY_MANDATORY_PARAM = "EMPTY_MANDATORY_PARAM";
  String INVALID_ID_PROVIDED = "INVALID_ID_PROVIDED";
  String INVALID_PROVIDED_URL = "INVALID_PROVIDED_URL";
  String INVALID_RELATED_TYPE = "INVALID_RELATED_TYPE";
  String INVALID_RECIPIENT_TYPE = "INVALID_RECIPIENT_TYPE";
  String INVALID_PROPERTY_ERROR = "INVALID_PROPERTY_ERROR";
  String DB_UPDATE_FAIL = "DB_UPDATE_FAIL";
  String DB_INSERTION_FAIL = "DB_INSERTION_FAIL";
  String INVALID_CONFIGURATION = "INVALID_CONFIGURATION";
  String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
  String SERVICE_UNAVAILABLE = "SERVICE UNAVAILABLE";
  String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";
  String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
  String GROUP_NOT_ACTIVE = "GROUP_NOT_ACTIVE";

  interface Key {
    String UNAUTHORIZED = "GS_UNAUTHORIZED";
    String KEY_CLOAK_DEFAULT_ERROR = "KEY_CLOAK_DEFAULT_ERROR";
    String UNABLE_TO_COMMUNICATE_WITH_ACTOR = "UNABLE_TO_COMMUNICATE_WITH_ACTOR";
    String EXCEEDED_MEMBER_MAX_LIMIT = "EXCEEDED_MEMBER_MAX_LIMIT";
    String EXCEEDED_GROUP_MAX_LIMIT = "EXCEEDED_GROUP_MAX_LIMIT";
    String EXCEEDED_ACTIVITY_MAX_LIMIT = "EXCEEDED_ACTIVITY_MAX_LIMIT";
    String SERVICE_UNAVAILABLE = "SERVICE UNAVAILABLE";
    String GS_CRT01 = "GS_CRT01";
    String GS_CRT02 = "GS_CRT02";
    String GS_CRT03 = "GS_CRT03";
    String GS_CRT04 = "GS_CRT04";
    String GS_CRT05 = "GS_CRT05";
    String GS_CRT06= "GS_CRT06";

    String GS_UDT01 = "GS_UDT01";
    String GS_UDT02 = "GS_UDT02";
    String GS_UDT03 = "GS_UDT03";
    String GS_UDT04 = "GS_UDT04";
    String GS_UDT05 = "GS_UDT05";
    String GS_UDT06= "GS_UDT06";
    String GS_UDT07 = "GS_UDT07";
    String GS_UDT08= "GS_UDT08";


    String GS_RED01 = "GS_RED01";
    String GS_RED02= "GS_RED02";
    String GS_RED03 = "GS_RED03";
    String GS_RED04= "GS_RED04";

    String GS_LST01 = "GS_LST01";
    String GS_LST02= "GS_LST02";
    String GS_LST03 = "GS_LST03";
    String GS_DLT01 = "GS_DLT01";
    String GS_DLT02= "GS_DLT02";
    String GS_DLT03 = "GS_DLT03";
    String GS_DLT04= "GS_DLT04";
    String GS_DLT05= "GS_DLT05";

    String GS_MBRSHP_UDT01= "GS_MBRSHP_UDT01";
    String GS_MBRSHP_UDT02 = "GS_MBRSHP_UDT02";
    String GS_MBRSHP_UDT03="GS_MBRSHP_UDT03";

  }

  interface Message {
    String KEY_CLOAK_DEFAULT_ERROR = "server error at sso.";
    String UNABLE_TO_COMMUNICATE_WITH_ACTOR = "Unable to communicate with actor.";
    String EXCEEDED_MEMBER_MAX_LIMIT = "Exceeded the member max size limit";
    String EXCEEDED_ACTIVITY_MAX_LIMIT = "Exceeded the activity max size limit";
    String EXCEEDED_GROUP_MAX_LIMIT = "Exceeded the group max size limit";
    String INVALID_REQUESTED_DATA = "Requested data for this operation is not valid.";
    String DATA_TYPE_ERROR = "Data type of {0} should be {1}.";
    String GROUP_NOT_FOUND = "group does not exist with this group Id {0}.";
    String GROUP_NOT_ACTIVE = "group not active with this group Id {0}.";
    String INVALID_PARAMETER_VALUE =
        "Invalid value {0} for parameter {1}. Please provide a valid value.";
    String MISSING_MANDATORY_PARAMS = "MANDATORY PARAM {0}.{1} IS MISSING";
    String INVALID_PROPERTY_ERROR = "Invalid property {0}.";
    String DB_INSERTION_FAIL = "DB insert operation failed.";
    String DB_UPDATE_FAIL = "Db update operation failed.";
    String INTERNAL_ERROR = "Process failed,please try again later.";
    String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    String UNAUTHORIZED = "You are an unauthorized.Contact your system administrator";

    String GS_CRT01 = "Failed to create group, unauthorised user. Contact your system administrator.";
    String GS_CRT02 = "Failed to create group, fields are missing in the request. Enter the required values and resend the request.";
    String GS_CRT03 = "Failed to create group, exceeded number of permissible groups.";
    String GS_CRT04 = "Failed to add member, group, exceeded number of permissible members.";
    String GS_CRT05 = "Failed to add activity in a group, exceeded the number of permissible activity in the group.";
    String GS_CRT06 = "Failed to create group, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_UDT01 = "Failed to update group, unauthorised user. Contact your system administrator.";
    String GS_UDT02 = "Failed to update group, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_UDT03 = "Failed to update, group inactive. Resend required values in request.";
    String GS_UDT04 = "Failed to update, group does not exist. Contact your system administrator.";
    String GS_UDT05 = "Failed to update group, administrator rights required. Contact your system administrator.";
    String GS_UDT06= "Failed to update the group,  exceeded permissible members count.";
    String GS_UDT07 = "Failed to update group activity, exceeded permissible activities in a group.";
    String GS_UDT08= "Failed to update group, due to database error or there are too many concurrent calls to the server. Try again later.";


    String GS_RED01 = "Failed to read group details, unauthorised user. Contact your system administrator.";
    String GS_RED02= "Failed to read group details, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_RED03 = "Failed to read details, group not found. Contact your system administrator.";
    String GS_RED04= "Failed to read group details due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_LST01 = "Failed to fetch group list, unauthorised user. Contact your system administrator.";
    String GS_LST02= "Failed to fetch group list, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_LST03 = "Failed to fetch group list, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_DLT01 = "Failed to delete group, unauthorised user. Contact your system administrator.";
    String GS_DLT02= "Failed to delete group, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_DLT03 = "Failed to delete, group not found. Contact your system administrator.";
    String GS_DLT04= "Failed to delete, group  creators can delete the group. Contact your system administrator.";
    String GS_DLT05 = "Failed to delete group, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_MBRSHP_UDT01= "Failed to update group member details, unauthorised user. Contact your system administrator.";
    String GS_MBRSHP_UDT02 = "Failed to update group member details, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_MBRSHP_UDT03= "Failed to create group, due to database error or there are too many concurrent calls to the server. Try again later.";





  }

}
