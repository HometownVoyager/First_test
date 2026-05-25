"""
Domain entities for the DeepSeek Chat application.

This module defines the core data models used throughout the application,
including messages, conversations, role cards, world books, and API key configs.
"""

from dataclasses import dataclass, field
from enum import Enum
from typing import Optional
import uuid
import time


class MessageRole(Enum):
    """
    Role of a message sender in a conversation.

    Attributes:
        value: The string value used in API requests.
    """

    USER = "user"
    ASSISTANT = "assistant"
    SYSTEM = "system"
    NARRATOR = "narrator"

    @classmethod
    def from_string(cls, role_str: str) -> "MessageRole":
        """
        Get a MessageRole from its string value.

        Args:
            role_str: The role string.

        Returns:
            The corresponding MessageRole enum value, or ASSISTANT as default.
        """
        for role in cls:
            if role.value.lower() == role_str.lower():
                return role
        return cls.ASSISTANT


@dataclass
class Message:
    """
    Represents a single message in a conversation.

    Attributes:
        id: Unique identifier for the message.
        role: The role of the message sender.
        content: The message content.
        role_id: For assistant messages, which role card sent this.
        role_name: Display name of the role.
        timestamp: Unix timestamp when the message was created.
        edited: Whether this message was manually edited.
        thought_content: Content from thinking mode (if available).
    """

    role: MessageRole = MessageRole.USER
    content: Optional[str] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    role_id: Optional[str] = None
    role_name: Optional[str] = None
    timestamp: int = field(default_factory=lambda: int(time.time() * 1000))
    edited: bool = False
    thought_content: Optional[str] = None

    def mark_as_edited(self) -> None:
        """Mark this message as edited due to manual user modification."""
        self.edited = True

    def set_content(self, new_content: str) -> None:
        """
        Set the message content and mark it as edited.

        Args:
            new_content: The new content for the message.
        """
        self.content = new_content
        self.mark_as_edited()

    def __str__(self) -> str:
        """Return a string representation of the message."""
        content_preview = ""
        if self.content:
            content_preview = (
                self.content[:50] + "..." if len(self.content) > 50 else self.content
            )
        return f"{self.role.name}: {content_preview}"


@dataclass
class ApiKeyConfig:
    """
    Represents an API Key configuration with metadata.

    Attributes:
        id: Unique identifier for this config.
        name: Human-readable name for the API key.
        api_key: The actual API key string.
        endpoint: The API endpoint URL.
        created_at: Unix timestamp when this config was created.
        updated_at: Unix timestamp when this config was last updated.
    """

    name: Optional[str] = None
    api_key: Optional[str] = None
    endpoint: str = "https://api.deepseek.com"
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    updated_at: int = field(default_factory=lambda: int(time.time() * 1000))

    def get_masked_api_key(self) -> str:
        """
        Return the API key in masked form for display purposes.

        Returns:
            Masked API key string (e.g., "abcd...xyz").
        """
        if not self.api_key or len(self.api_key) < 8:
            return "****"
        return f"{self.api_key[:4]}...{self.api_key[-4:]}"

    def __str__(self) -> str:
        """Return a string representation of the config."""
        return self.name if self.name else self.get_masked_api_key()


class SpeakMode(Enum):
    """
    Speaking mode for conversation participants.

    Attributes:
        MANUAL: User manually specifies next speaker.
        ROUND_ROBIN: Roles take turns in order.
        AUTO: Model decides who speaks next.
    """

    MANUAL = "manual"
    ROUND_ROBIN = "round_robin"
    AUTO = "auto"


@dataclass
class Conversation:
    """
    Represents a conversation (chat room) with multiple participants.

    Attributes:
        id: Unique identifier for the conversation.
        title: The conversation title.
        messages: List of messages in the conversation.
        participant_role_ids: IDs of roles participating in this chat.
        muted_role_ids: IDs of roles that are muted.
        world_book_id: Bound world book ID.
        speak_mode: The speaking mode for this conversation.
        next_speaker_id: For MANUAL mode, the next speaker's role ID.
        created_at: Unix timestamp when the conversation was created.
        updated_at: Unix timestamp when the conversation was last updated.
        message_counts: Track message count per role for round-robin.
    """

    title: Optional[str] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    messages: list[Message] = field(default_factory=list)
    participant_role_ids: set[str] = field(default_factory=set)
    muted_role_ids: set[str] = field(default_factory=set)
    world_book_id: Optional[str] = None
    speak_mode: SpeakMode = SpeakMode.AUTO
    next_speaker_id: Optional[str] = None
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    updated_at: int = field(default_factory=lambda: int(time.time() * 1000))
    message_counts: dict[str, int] = field(default_factory=dict)

    def __post_init__(self) -> None:
        """Initialize defaults after dataclass initialization."""
        # Ensure narrator is always included by default
        self.participant_role_ids.add(RoleCard.NARRATOR_ID)

    def add_message(self, message: Message) -> None:
        """
        Add a message to this conversation.

        Args:
            message: The message to add.
        """
        if message:
            self.messages.append(message)
            self.updated_at = int(time.time() * 1000)

            # Update message count for round-robin
            if message.role_id:
                self.message_counts[message.role_id] = (
                    self.message_counts.get(message.role_id, 0) + 1
                )

    def remove_message(self, message_id: str) -> bool:
        """
        Remove a message by ID (for regeneration).

        Args:
            message_id: The ID of the message to remove.

        Returns:
            True if the message was removed, False otherwise.
        """
        initial_length = len(self.messages)
        self.messages = [m for m in self.messages if m.id != message_id]
        if len(self.messages) < initial_length:
            self.updated_at = int(time.time() * 1000)
            return True
        return False

    def get_last_message(self) -> Optional[Message]:
        """
        Get the last message in the conversation.

        Returns:
            The last message, or None if the conversation is empty.
        """
        return self.messages[-1] if self.messages else None

    def add_participant(self, role_id: str) -> None:
        """
        Add a participant role to this conversation.

        Args:
            role_id: The role ID to add.
        """
        if role_id:
            self.participant_role_ids.add(role_id)
            self.updated_at = int(time.time() * 1000)

    def remove_participant(self, role_id: str) -> None:
        """
        Remove a participant role from this conversation.

        Args:
            role_id: The role ID to remove.
        """
        if role_id and role_id != RoleCard.NARRATOR_ID:
            self.participant_role_ids.discard(role_id)
            self.updated_at = int(time.time() * 1000)

    def is_muted(self, role_id: str) -> bool:
        """
        Check if a role is muted.

        Args:
            role_id: The role ID to check.

        Returns:
            True if the role is muted, False otherwise.
        """
        return role_id in self.muted_role_ids

    def mute(self, role_id: str) -> None:
        """
        Mute a role.

        Args:
            role_id: The role ID to mute.
        """
        if role_id and role_id != RoleCard.NARRATOR_ID:
            self.muted_role_ids.add(role_id)
            self.updated_at = int(time.time() * 1000)

    def unmute(self, role_id: str) -> None:
        """
        Unmute a role.

        Args:
            role_id: The role ID to unmute.
        """
        if role_id:
            self.muted_role_ids.discard(role_id)
            self.updated_at = int(time.time() * 1000)

    def generate_auto_title(self) -> None:
        """Generate an auto title from the first message."""
        if not self.title or not self.title.strip():
            if self.messages:
                first_msg = self.messages[0]
                if first_msg.content:
                    content = first_msg.content
                    self.title = (
                        content[:30] + "..." if len(content) > 30 else content
                    )
                else:
                    self.title = "New Conversation"
            else:
                self.title = "New Conversation"

    def __str__(self) -> str:
        """Return a string representation of the conversation."""
        return self.title if self.title else f"Conversation {self.id[:8]}"


@dataclass
class RoleCard:
    """
    Represents a role card compatible with SillyTavern format (V2/V3).

    Attributes:
        id: Unique identifier for the role card.
        name: The character's name.
        description: Character description.
        personality: Character personality traits.
        scenario: The scenario/context for the role.
        first_mes: First message from the character.
        mes_example: Example messages.
        creator_notes: Notes from the creator.
        system_prompt: System prompt for the character.
        avatar: Avatar image (path or base64 data).
        avatar_data_type: MIME type for avatar data.
        api_key_id: Bound API Key ID.
        tags: List of tags.
        character_version: Version string.
        created_at: Unix timestamp when created.
        updated_at: Unix timestamp when last updated.
        is_system: Whether this is a built-in system role.
        extensions: JSON string for extensions data.
        data: JSON string for full SillyTavern data.
    """

    NARRATOR_ID: str = field(default="__narrator__", init=False)
    NARRATOR_NAME: str = field(default="背景", init=False)

    name: Optional[str] = None
    description: Optional[str] = None
    personality: Optional[str] = None
    scenario: Optional[str] = None
    first_mes: Optional[str] = None
    mes_example: Optional[str] = None
    creator_notes: Optional[str] = None
    system_prompt: Optional[str] = None
    avatar: Optional[str] = None
    avatar_data_type: Optional[str] = None
    api_key_id: Optional[str] = None
    tags: list[str] = field(default_factory=list)
    character_version: Optional[str] = None
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    updated_at: int = field(default_factory=lambda: int(time.time() * 1000))
    is_system: bool = False
    extensions: Optional[str] = None
    data: Optional[str] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __post_init__(self) -> None:
        """Set is_system flag if this is the narrator role."""
        if self.id == "__narrator__":
            self.is_system = True

    @classmethod
    def create_narrator(cls) -> "RoleCard":
        """
        Create the special Narrator (背景) role card.

        Returns:
            A RoleCard instance configured as the narrator.
        """
        narrator = cls(
            id="__narrator__",
            name="背景",
            description="旁白与宏观调控者，可以看到全部对话上下文，负责场景描述和事件推进。",
            system_prompt=(
                "You are the narrator and macro controller. You can see all conversation "
                "context and all characters' speeches. You provide scene descriptions, "
                "narrative transitions, and event progression. You can also mute or "
                "unmute specific characters."
            ),
            first_mes="*The scene awaits your action...*",
            is_system=True,
        )
        return narrator

    def __str__(self) -> str:
        """Return a string representation of the role card."""
        return self.name if self.name else "Unnamed Role"


@dataclass
class WorldBookEntryPosition(Enum):
    """Position where world book entry content should be inserted."""

    BEFORE = "before"  # Insert before user message
    AFTER = "after"  # Insert after user message


@dataclass
class WorldBookEntry:
    """
    Represents a World Info / Lorebook entry compatible with SillyTavern format.

    Attributes:
        id: Unique identifier for the entry.
        key: Trigger keywords (comma-separated).
        content: The lore content to inject.
        comment: Optional comment/description.
        priority: Higher priority entries are processed first.
        enabled: Whether this entry is active.
        position: Where to insert (BEFORE or AFTER).
        selectivity: Match threshold (0-1).
        use_regex: Whether key is a regex pattern.
        keys: Alternative keys list.
    """

    content: Optional[str] = None
    key: Optional[str] = None
    comment: Optional[str] = None
    priority: int = 0
    enabled: bool = True
    position: WorldBookEntryPosition = WorldBookEntryPosition.BEFORE
    selectivity: float = 1.0
    use_regex: bool = False
    keys: list[str] = field(default_factory=list)
    id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __post_init__(self) -> None:
        """Parse keys from key string if keys list is empty."""
        if self.key and not self.keys:
            self.keys = [k.strip() for k in self.key.split(",")]

    def matches(self, text: str) -> bool:
        """
        Check if the given text matches any of the trigger keys.

        Args:
            text: The text to check against trigger keys.

        Returns:
            True if the text matches, False otherwise.
        """
        if not self.enabled or not text:
            return False

        lower_text = text.lower()
        for k in self.keys:
            if not k or not k.strip():
                continue
            if self.use_regex:
                try:
                    import re

                    if re.search(k.lower(), lower_text):
                        return True
                except re.error:
                    # Invalid regex, skip
                    pass
            else:
                if k.lower() in lower_text:
                    return True
        return False

    def __str__(self) -> str:
        """Return a string representation of the entry."""
        return self.comment if self.comment else (self.key if self.key else "Unnamed Entry")


@dataclass
class WorldBook:
    """
    Represents a World Book (Lorebook) containing multiple entries.

    Attributes:
        id: Unique identifier for the world book.
        name: The world book name.
        description: Description of the world book.
        entries: List of world book entries.
        created_at: Unix timestamp when created.
        updated_at: Unix timestamp when last updated.
    """

    name: Optional[str] = None
    description: Optional[str] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    entries: list[WorldBookEntry] = field(default_factory=list)
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    updated_at: int = field(default_factory=lambda: int(time.time() * 1000))

    def add_entry(self, entry: WorldBookEntry) -> None:
        """
        Add an entry to this world book.

        Args:
            entry: The entry to add.
        """
        if entry:
            self.entries.append(entry)
            self.updated_at = int(time.time() * 1000)

    def remove_entry(self, entry_id: str) -> bool:
        """
        Remove an entry by ID.

        Args:
            entry_id: The ID of the entry to remove.

        Returns:
            True if the entry was removed, False otherwise.
        """
        initial_length = len(self.entries)
        self.entries = [e for e in self.entries if e.id != entry_id]
        if len(self.entries) < initial_length:
            self.updated_at = int(time.time() * 1000)
            return True
        return False

    def find_matching_entries(self, text: str) -> list[WorldBookEntry]:
        """
        Find all enabled entries that match the given text.

        Args:
            text: The text to match against entry keys.

        Returns:
            List of matching entries, sorted by priority (highest first).
        """
        matching = [entry for entry in self.entries if entry.matches(text)]
        # Sort by priority (higher first)
        matching.sort(key=lambda e: e.priority, reverse=True)
        return matching

    def __str__(self) -> str:
        """Return a string representation of the world book."""
        return self.name if self.name else "Unnamed World Book"
