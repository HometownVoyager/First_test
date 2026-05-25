"""
Tests for domain entities.

This module contains unit tests for the core domain models,
including Message, Conversation, RoleCard, WorldBook, and ApiKeyConfig.
"""

import pytest

from deepseek_chat.domain.entities import (
    ApiKeyConfig,
    Conversation,
    Message,
    MessageRole,
    RoleCard,
    SpeakMode,
    WorldBook,
    WorldBookEntry,
    WorldBookEntryPosition,
)


class TestMessage:
    """Tests for the Message dataclass."""

    def test_default_values(self) -> None:
        """Test that default values are set correctly."""
        msg = Message()

        assert msg.role == MessageRole.USER
        assert msg.content is None
        assert msg.id is not None
        assert msg.edited is False

    def test_create_with_content(self) -> None:
        """Test creating a message with content."""
        msg = Message(role=MessageRole.ASSISTANT, content="Hello!")

        assert msg.role == MessageRole.ASSISTANT
        assert msg.content == "Hello!"

    def test_mark_as_edited(self) -> None:
        """Test marking a message as edited."""
        msg = Message(content="Original")
        assert msg.edited is False

        msg.mark_as_edited()
        assert msg.edited is True

    def test_set_content_marks_edited(self) -> None:
        """Test that setting content marks the message as edited."""
        msg = Message(content="Original")
        msg.set_content("Modified")

        assert msg.content == "Modified"
        assert msg.edited is True


class TestConversation:
    """Tests for the Conversation dataclass."""

    def test_default_values(self) -> None:
        """Test that default values are set correctly."""
        conv = Conversation()

        assert conv.title is None
        assert conv.id is not None
        assert len(conv.messages) == 0
        assert "__narrator__" in conv.participant_role_ids
        assert conv.speak_mode == SpeakMode.AUTO

    def test_add_message(self) -> None:
        """Test adding a message to a conversation."""
        conv = Conversation()
        msg = Message(role=MessageRole.USER, content="Test message")

        conv.add_message(msg)

        assert len(conv.messages) == 1
        assert conv.messages[0] == msg

    def test_remove_message(self) -> None:
        """Test removing a message from a conversation."""
        conv = Conversation()
        msg1 = Message(content="First")
        msg2 = Message(content="Second")

        conv.add_message(msg1)
        conv.add_message(msg2)

        result = conv.remove_message(msg1.id)

        assert result is True
        assert len(conv.messages) == 1
        assert conv.messages[0].id == msg2.id

    def test_generate_auto_title(self) -> None:
        """Test auto-title generation."""
        conv = Conversation()
        conv.add_message(Message(content="This is a very long message that should be truncated"))

        conv.generate_auto_title()

        assert conv.title == "This is a very long message that shoul..."

    def test_mute_unmute(self) -> None:
        """Test muting and unmuting roles."""
        conv = Conversation()
        role_id = "test-role-123"

        conv.add_participant(role_id)
        assert conv.is_muted(role_id) is False

        conv.mute(role_id)
        assert conv.is_muted(role_id) is True

        conv.unmute(role_id)
        assert conv.is_muted(role_id) is False


class TestRoleCard:
    """Tests for the RoleCard dataclass."""

    def test_create_narrator(self) -> None:
        """Test creating the narrator role card."""
        narrator = RoleCard.create_narrator()

        assert narrator.id == "__narrator__"
        assert narrator.name == "背景"
        assert narrator.is_system is True
        assert narrator.system_prompt is not None

    def test_default_role_card(self) -> None:
        """Test creating a default role card."""
        card = RoleCard(name="Test Character")

        assert card.name == "Test Character"
        assert card.id is not None
        assert card.is_system is False


class TestApiKeyConfig:
    """Tests for the ApiKeyConfig dataclass."""

    def test_default_endpoint(self) -> None:
        """Test that default endpoint is set correctly."""
        config = ApiKeyConfig(name="Test", api_key="sk-test12345678")

        assert config.endpoint == "https://api.deepseek.com"

    def test_masked_api_key(self) -> None:
        """Test API key masking."""
        config = ApiKeyConfig(api_key="sk-abcdefghij1234567890")

        masked = config.get_masked_api_key()
        assert masked == "sk-a...7890"

    def test_masked_short_key(self) -> None:
        """Test masking of short API keys."""
        config = ApiKeyConfig(api_key="short")

        masked = config.get_masked_api_key()
        assert masked == "****"


class TestWorldBookEntry:
    """Tests for the WorldBookEntry dataclass."""

    def test_matches_simple(self) -> None:
        """Test simple keyword matching."""
        entry = WorldBookEntry(key="sword, weapon", content="A sharp blade")

        assert entry.matches("He drew his sword") is True
        assert entry.matches("The weapon gleamed") is True
        assert entry.matches("Nothing here") is False

    def test_matches_disabled(self) -> None:
        """Test that disabled entries don't match."""
        entry = WorldBookEntry(key="test", content="Content")
        entry.enabled = False

        assert entry.matches("test") is False

    def test_priority_sorting(self) -> None:
        """Test that entries can be sorted by priority."""
        entry1 = WorldBookEntry(key="test", priority=1)
        entry2 = WorldBookEntry(key="test", priority=5)
        entry3 = WorldBookEntry(key="test", priority=3)

        entries = [entry1, entry2, entry3]
        sorted_entries = sorted(entries, key=lambda e: e.priority, reverse=True)

        assert sorted_entries[0].priority == 5
        assert sorted_entries[1].priority == 3
        assert sorted_entries[2].priority == 1


class TestWorldBook:
    """Tests for the WorldBook dataclass."""

    def test_find_matching_entries(self) -> None:
        """Test finding matching entries."""
        book = WorldBook(name="Test Book")
        book.add_entry(WorldBookEntry(key="magic", content="Magic content", priority=1))
        book.add_entry(WorldBookEntry(key="sword", content="Sword content", priority=2))

        matches = book.find_matching_entries("He found a magic sword")

        assert len(matches) == 2
        # Should be sorted by priority (highest first)
        assert matches[0].content == "Sword content"
        assert matches[1].content == "Magic content"
