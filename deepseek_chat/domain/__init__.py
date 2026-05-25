"""
Domain module for core business entities.

This module contains the domain models that represent the core concepts
of the DeepSeek Chat application, including conversations, messages,
role cards, and world books.
"""

from .entities import (
    Message,
    MessageRole,
    RoleCard,
    Conversation,
    SpeakMode,
    WorldBook,
    WorldBookEntry,
    WorldBookEntryPosition,
    ApiKeyConfig,
)

__all__ = [
    "Message",
    "MessageRole",
    "RoleCard",
    "Conversation",
    "SpeakMode",
    "WorldBook",
    "WorldBookEntry",
    "WorldBookEntryPosition",
    "ApiKeyConfig",
]
