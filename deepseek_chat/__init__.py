"""
DeepSeek AI Chat - Python Refactored Version

A desktop AI chat application with role-playing features, connecting to the DeepSeek API.
This is a Python refactoring of the original Java project, maintaining all business logic
and interface behavior while improving code quality, readability, and maintainability.

Author: Refactored from Java to Python
Version: 1.0.0
"""

__version__ = "1.0.0"
__author__ = "DeepSeek Chat Team"

from .api.client import DeepSeekApiClient
from .api.models import DeepSeekModel, ApiRequestConfig, ApiResponse
from .domain.entities import (
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
from .services.conversation_service import ConversationService
from .services.storage_service import StorageService

__all__ = [
    # Version info
    "__version__",
    "__author__",
    # API module
    "DeepSeekApiClient",
    "DeepSeekModel",
    "ApiRequestConfig",
    "ApiResponse",
    # Domain entities
    "Message",
    "MessageRole",
    "RoleCard",
    "Conversation",
    "SpeakMode",
    "WorldBook",
    "WorldBookEntry",
    "WorldBookEntryPosition",
    "ApiKeyConfig",
    # Services
    "ConversationService",
    "StorageService",
]
