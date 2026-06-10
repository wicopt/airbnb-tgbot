package mariia.sofiia.auth.infrastructure.config;

public final class ErrorCode {
    public static final String INVITE_NOT_FOUND = "INVITE_NOT_FOUND";
    public static final String INVITE_EXPIRED = "INVITE_EXPIRED";
    public static final String INVITE_ALREADY_USED = "INVITE_ALREADY_USED";
    public static final String ALREADY_IN_GROUP = "ALREADY_IN_GROUP";
    public static final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String USER_NOT_IN_GROUP = "USER_NOT_IN_GROUP";

    private ErrorCode() {
    }
}