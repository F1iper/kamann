package pl.kamann.config.global;

public class Codes {

    // auth

    //todo: verify duplication
    public static final String SUCCESS = "SUCCESS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";


    //status
    public static final String INVALID_ATTENDANCE_STATUS = "INVALID_STATUS";
    public static final String INVALID_USER_STATUS = "INVALID_USER_STATUS";
    public static final String INVALID_STATUS_CHANGE = "INVALID_STATUS_CHANGE";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INVALID_ROLE = "INVALID_ROLE";
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String NO_RESULTS = "NO_RESULTS";

    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";

    // roles
    public static final String ADMIN = "ADMIN";
    public static final String INSTRUCTOR = "INSTRUCTOR";
    public static final String CLIENT = "CLIENT";

    //event
    public static final String EVENT_NOT_FOUND = "EVENT_NOT_FOUND";
    public static final String PAST_EVENT_ERROR = "PAST_EVENT_ERROR";
    public static final String EVENT_TYPE_NOT_FOUND = "EVENT_TYPE_NOT_FOUND";
    public static final String EVENT_FULL = "EVENT_FULL";
    public static final String INVALID_EVENT_TIME = "INVALID_EVENT_TIME";
    public static final String CANNOT_CANCEL_STARTED_EVENT = "CANNOT_CANCEL_STARTED_EVENT";
    public static final String EVENT_HAS_PARTICIPANTS = "EVENT_HAS_PARTICIPANTS";

    //attendance
    public static final String INVALID_ATTENDANCE_STATE = "INVALID_ATTENDANCE_STATE";
    public static final String ATTENDANCE_NOT_FOUND ="ATTENDANCE_NOT_FOUND" ;
    public static final String ALREADY_REGISTERED = "ALREADY_REGISTERED";

    //membership card
    public static final String CARD_NOT_FOUND = "CARD_NOT_FOUND";
    public static final String CARD_NOT_ACTIVE = "CARD_NOT_ACTIVE";
    public static final String NO_ENTRANCES_LEFT = "NO_ENTRANCES_LEFT";
    public static final String CARD_EXPIRED = "CARD_EXPIRED";
    public static final String CARD_ALREADY_ACTIVE = "CARD_ALREADY_ACTIVE";
    public static final String CARD_PENDING_APPROVAL = "CARD_PENDING_APPROVAL";
    public static final String INVALID_CARD_STATE = "INVALID_CARD_STATE";
    public static final String CARD_ALREADY_EXISTS = "CARD_ALREADY_EXISTS";

    //instructor
    public static final String INSTRUCTOR_BUSY = "INSTRUCTOR_BUSY";
    public static final String INSTRUCTOR_NOT_FOUND = "INSTRUCTOR_NOT_FOUND";
    public static final String REGISTRATION_NOT_FOUND = "REGISTRATION_NOT_FOUND";

}
