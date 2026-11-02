"""Event names shared with the frontend taxonomy in docs/analytics.md.

Properties must be coarse and non-identifying. No free text from the user,
no IP address, no district name paired with a session id.
"""

SCREEN_VIEWED = "screen_viewed"
MAP_LAYER_VIEWED = "map_layer_viewed"
DISTRICT_VIEWED = "district_viewed"
ANALYST_QUESTION_ASKED = "analyst_question_asked"

ALLOWED_EVENTS = frozenset(
    {
        SCREEN_VIEWED,
        MAP_LAYER_VIEWED,
        DISTRICT_VIEWED,
        ANALYST_QUESTION_ASKED,
    }
)

# Keys that must never be attached to an event.
BANNED_PROPERTY_KEYS = frozenset(
    {
        "email",
        "name",
        "ip",
        "user_id",
        "question",
        "prompt",
        "sql",
        "address",
    }
)
