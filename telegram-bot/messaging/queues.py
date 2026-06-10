class Queues:
    # Bot → Auth
    USE_INVITE_REQUEST = "auth.use-invite.request"
    GENERATE_INVITE_REQUEST = "auth.generate-invite.request"
    GET_MEMBERS_REQUEST = "auth.get-members.request"
    KICK_MEMBER_REQUEST = "auth.kick-member.request"

    # Auth → Bot
    USE_INVITE_RESPONSE = "auth.use-invite.response"
    GENERATE_INVITE_RESPONSE = "auth.generate-invite.response"
    GET_MEMBERS_RESPONSE = "auth.get-members.response"
    KICK_MEMBER_RESPONSE = "auth.kick-member.response"
    MEMBER_JOINED_EVENT = "auth.member-joined.event"
    
    # Bot → Statistics
    GET_STATISTICS_REQUEST = "statistics.request"
    GET_ROI_REQUEST = "roi.request"

    # Statistics → Bot
    GET_STATISTICS_RESPONSE = "statistics.response"
    GET_ROI_RESPONSE = "roi.response"
    
    
    # Bot → Payment
    CREATE_PAYMENT_REQUEST = "payment.request"

    # Payment → Bot
    CREATE_PAYMENT_RESPONSE = "payment.response"