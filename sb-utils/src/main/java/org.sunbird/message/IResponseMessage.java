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
    String UNAUTHORIZED = "UNAUTHORIZED";
    String KEY_CLOAK_DEFAULT_ERROR = "KEY_CLOAK_DEFAULT_ERROR";
    String UNABLE_TO_COMMUNICATE_WITH_ACTOR = "UNABLE_TO_COMMUNICATE_WITH_ACTOR";
    String EXCEEDED_MEMBER_MAX_LIMIT = "EXCEEDED_MEMBER_MAX_LIMIT";
    String EXCEEDED_GROUP_MAX_LIMIT = "EXCEEDED_GROUP_MAX_LIMIT";
    String EXCEEDED_ACTIVITY_MAX_LIMIT = "EXCEEDED_ACTIVITY_MAX_LIMIT";
    String SERVICE_UNAVAILABLE = "SERVICE UNAVAILABLE";
    String GS_CRT_01 = "GS_CRT_01";
    String GS_CRT_02 = "GS_CRT_02";
    String GS_CRT_03 = "GS_CRT_03";
    String GS_CRT_04 = "GS_CRT_04";
    String GS_CRT_05 = "GS_CRT_05";
    String GS_CRT_06= "GS_CRT_06";

    String GS_UDT_01 = "GS_UDT_01";
    String GS_UDT_02 = "GS_UDT_02";
    String GS_UDT_03 = "GS_UDT_03";
    String GS_UDT_04 = "GS_UDT_04";
    String GS_UDT_05 = "GS_UDT_05";
    String GS_UDT_06= "GS_UDT_06";
    String GS_UDT_07 = "GS_UDT_07";
    String GS_UDT_08= "GS_UDT_08";


    String GS_RED_01 = "GS_RED_01";
    String GS_RED_02= "GS_RED_02";
    String GS_RED_03 = "GS_RED_03";
    String GS_RED_04= "GS_RED_04";

    String GS_LST_01 = "GS_LST_01";
    String GS_LST_02= "GS_LST_02";
    String GS_LST_03 = "GS_LST_03";
    String GS_DLT_01 = "GS_DLT_01";
    String GS_DLT_02= "GS_DLT_02";
    String GS_DLT_03 = "GS_DLT_03";
    String GS_DLT_04= "GS_DLT_04";
    String GS_DLT_05= "GS_DLT_05";

    String GS_MBRSHP_UDT_01= "GS_MBRSHP_UDT_01";
    String GS_MBRSHP_UDT_02 = "GS_MBRSHP_UDT_02";
    String GS_MBRSHP_UDT_03="GS_MBRSHP_UDT_03";

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
    String UNAUTHORIZED = "You are not authorized.";

    String GS_CRT_01 = "Failed to create group, unauthorised user. Contact your system administrator.";
    String GS_CRT_02 = "Failed to create group, fields are missing in the request. Enter the required values and resend the request.";
    String GS_CRT_03 = "Failed to create group, exceeded number of permissible groups.";
    String GS_CRT_04 = "Failed to add member, group, exceeded number of permissible members.";
    String GS_CRT_05 = "Failed to add activity in a group, exceeded the number of permissible activity in the group.";
    String GS_CRT_06 = "Failed to create group, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_UDT_01 = "Failed to update group, unauthorised user. Contact your system administrator.";
    String GS_UDT_02 = "Failed to update group, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_UDT_03 = "Failed to update, group inactive. Resend required values in request.";
    String GS_UDT_04 = "Failed to update, group does not exist. Contact your system administrator.";
    String GS_UDT_05 = "Failed to update group, administrator rights required. Contact your system administrator.";
    String GS_UDT_06= "Failed to update the group,  exceeded permissible members count.";
    String GS_UDT_07 = "Failed to update group activity, exceeded permissible activities in a group.";
    String GS_UDT_08= "Failed to update group, due to database error or there are too many concurrent calls to the server. Try again later.";


    String GS_RED_01 = "Failed to read group details, unauthorised user. Contact your system administrator.";
    String GS_RED_02= "Failed to read group details, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_RED_03 = "Failed to read details, group not found. Contact your system administrator.";
    String GS_RED_04= "Failed to read group details due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_LST_01 = "Failed to fetch group list, unauthorised user. Contact your system administrator.";
    String GS_LST_02= "Failed to fetch group list, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_LST_03 = "Failed to fetch group list, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_DLT_01 = "Failed to delete group, unauthorised user. Contact your system administrator.";
    String GS_DLT_02= "Failed to delete group, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_DLT_03 = "Failed to delete, group not found. Contact your system administrator.";
    String GS_DLT_04= "Failed to delete, group  creators can delete the group. Contact your system administrator.";
    String GS_DLT_05 = "Failed to delete group, due to database error or there are too many concurrent calls to the server. Try again later.";

    String GS_MBRSHP_UDT_01= "Failed to update group member details, unauthorised user. Contact your system administrator.";
    String GS_MBRSHP_UDT_02 = "Failed to update group member details, mandatory fields are missing in the request. Enter the required values and resend the request.";
    String GS_MBRSHP_UDT_03= "Failed to create group, due to database error or there are too many concurrent calls to the server. Try again later.";





  }

}
