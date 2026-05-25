"""
Conversation service for managing chat conversations.

This module implements the core business logic for conversation management,
including speaking strategies, world book matching, context building,
and message generation.
"""

from typing import Optional

from ..api.client import DeepSeekApiClient
from ..api.models import ApiRequestConfig, ApiResponse
from ..domain.entities import (
    ApiKeyConfig,
    Conversation,
    Message,
    MessageRole,
    RoleCard,
    SpeakMode,
    WorldBook,
    WorldBookEntry,
)
from .storage_service import StorageService


class ConversationService:
    """
    Core business logic for conversation management.

    This class handles:
    - Conversation CRUD operations
    - Role card management
    - API key configuration
    - World book management
    - Message generation with speaking strategies
    - Context building with world book injection

    Attributes:
        storage_manager: Service for data persistence.
        api_client: Client for DeepSeek API communication.
    """

    def __init__(self, storage_manager: StorageService) -> None:
        """
        Initialize the conversation service.

        Args:
            storage_manager: StorageService instance for data persistence.
        """
        self.storage_manager = storage_manager
        self.api_client = DeepSeekApiClient()

        # Cached data
        self._conversations: list[Conversation] = []
        self._role_cards: list[RoleCard] = []
        self._api_keys: list[ApiKeyConfig] = []
        self._world_books: list[WorldBook] = []

        # Load initial data
        self.load_data()

    def load_data(self) -> None:
        """Load all data from storage."""
        self._conversations = self.storage_manager.load_conversations()
        self._role_cards = self.storage_manager.load_role_cards()
        self._api_keys = self.storage_manager.load_api_keys()
        self._world_books = self.storage_manager.load_world_books()

    def save_data(self) -> None:
        """Save all data to storage."""
        self.storage_manager.save_conversations(self._conversations)
        self.storage_manager.save_role_cards(self._role_cards)
        self.storage_manager.save_api_keys(self._api_keys)
        self.storage_manager.save_world_books(self._world_books)

    # ==================== Conversations ====================

    def create_conversation(self, title: str) -> Conversation:
        """
        Create a new conversation.

        Args:
            title: The title for the new conversation.

        Returns:
            The newly created Conversation object.
        """
        conv = Conversation(title=title)
        conv.add_participant(RoleCard.NARRATOR_ID)
        self._conversations.append(conv)
        self.save_data()
        return conv

    def delete_conversation(self, conversation_id: str) -> bool:
        """
        Delete a conversation by ID.

        Args:
            conversation_id: The ID of the conversation to delete.

        Returns:
            True if the conversation was deleted, False otherwise.
        """
        initial_length = len(self._conversations)
        self._conversations = [
            c for c in self._conversations if c.id != conversation_id
        ]
        if len(self._conversations) < initial_length:
            self.save_data()
            return True
        return False

    def get_conversation(self, conversation_id: str) -> Optional[Conversation]:
        """
        Get a conversation by ID.

        Args:
            conversation_id: The ID of the conversation.

        Returns:
            The Conversation object, or None if not found.
        """
        for conv in self._conversations:
            if conv.id == conversation_id:
                return conv
        return None

    def get_all_conversations(self) -> list[Conversation]:
        """
        Get all conversations.

        Returns:
            List of all Conversation objects.
        """
        return list(self._conversations)

    # ==================== Role Cards ====================

    def get_all_role_cards(self) -> list[RoleCard]:
        """
        Get all role cards.

        Returns:
            List of all RoleCard objects.
        """
        return list(self._role_cards)

    def get_role_card(self, role_id: str) -> Optional[RoleCard]:
        """
        Get a role card by ID.

        Args:
            role_id: The ID of the role card.

        Returns:
            The RoleCard object, or None if not found.
        """
        for card in self._role_cards:
            if card.id == role_id:
                return card
        return None

    def save_role_card(self, card: RoleCard) -> None:
        """
        Add or update a role card.

        Args:
            card: The RoleCard to save.
        """
        existing_index = next(
            (i for i, r in enumerate(self._role_cards) if r.id == card.id), None
        )

        if existing_index is not None:
            self._role_cards[existing_index] = card
        else:
            self._role_cards.append(card)

        self.save_data()

    def delete_role_card(self, role_id: str) -> bool:
        """
        Delete a role card by ID (cannot delete narrator).

        Args:
            role_id: The ID of the role card to delete.

        Returns:
            True if the role card was deleted, False otherwise.
        """
        if role_id == RoleCard.NARRATOR_ID:
            return False  # Cannot delete narrator

        initial_length = len(self._role_cards)
        self._role_cards = [r for r in self._role_cards if r.id != role_id]
        if len(self._role_cards) < initial_length:
            self.save_data()
            return True
        return False

    # ==================== API Keys ====================

    def get_all_api_keys(self) -> list[ApiKeyConfig]:
        """
        Get all API key configurations.

        Returns:
            List of all ApiKeyConfig objects.
        """
        return list(self._api_keys)

    def get_api_key(self, key_id: str) -> Optional[ApiKeyConfig]:
        """
        Get an API key config by ID.

        Args:
            key_id: The ID of the API key config.

        Returns:
            The ApiKeyConfig object, or None if not found.
        """
        for key in self._api_keys:
            if key.id == key_id:
                return key
        return None

    def save_api_key(self, config: ApiKeyConfig) -> None:
        """
        Add or update an API key config.

        Args:
            config: The ApiKeyConfig to save.
        """
        import time

        existing_index = next(
            (i for i, k in enumerate(self._api_keys) if k.id == config.id), None
        )

        if existing_index is not None:
            config.updated_at = int(time.time() * 1000)
            self._api_keys[existing_index] = config
        else:
            self._api_keys.append(config)

        self.save_data()

    def delete_api_key(self, key_id: str) -> bool:
        """
        Delete an API key config by ID.

        Args:
            key_id: The ID of the API key config to delete.

        Returns:
            True if the API key was deleted, False otherwise.
        """
        initial_length = len(self._api_keys)
        self._api_keys = [k for k in self._api_keys if k.id != key_id]
        if len(self._api_keys) < initial_length:
            self.save_data()
            return True
        return False

    # ==================== World Books ====================

    def get_all_world_books(self) -> list[WorldBook]:
        """
        Get all world books.

        Returns:
            List of all WorldBook objects.
        """
        return list(self._world_books)

    def get_world_book(self, book_id: str) -> Optional[WorldBook]:
        """
        Get a world book by ID.

        Args:
            book_id: The ID of the world book.

        Returns:
            The WorldBook object, or None if not found.
        """
        for book in self._world_books:
            if book.id == book_id:
                return book
        return None

    def save_world_book(self, book: WorldBook) -> None:
        """
        Save a world book.

        Args:
            book: The WorldBook to save.
        """
        existing_index = next(
            (i for i, b in enumerate(self._world_books) if b.id == book.id), None
        )

        if existing_index is not None:
            self._world_books[existing_index] = book
        else:
            self._world_books.append(book)

        self.save_data()

    def delete_world_book(self, book_id: str) -> bool:
        """
        Delete a world book by ID.

        Args:
            book_id: The ID of the world book to delete.

        Returns:
            True if the world book was deleted, False otherwise.
        """
        initial_length = len(self._world_books)
        self._world_books = [b for b in self._world_books if b.id != book_id]
        if len(self._world_books) < initial_length:
            self.save_data()
            return True
        return False

    # ==================== Conversation Operations ====================

    def add_user_message(self, conversation: Conversation, content: str) -> None:
        """
        Add a user message to the conversation.

        Args:
            conversation: The conversation to add the message to.
            content: The message content.
        """
        msg = Message(role=MessageRole.USER, content=content)
        conversation.add_message(msg)

        # Auto-generate title if needed
        if not conversation.title or not conversation.title.strip():
            conversation.generate_auto_title()

        self.save_data()

    def edit_message(
        self, conversation: Conversation, message_id: str, new_content: str
    ) -> None:
        """
        Edit an existing message.

        Args:
            conversation: The conversation containing the message.
            message_id: The ID of the message to edit.
            new_content: The new content for the message.
        """
        for msg in conversation.messages:
            if msg.id == message_id:
                msg.set_content(new_content)
                break
        self.save_data()

    def remove_message(self, conversation: Conversation, message_id: str) -> None:
        """
        Remove a message (for regeneration).

        Args:
            conversation: The conversation containing the message.
            message_id: The ID of the message to remove.
        """
        conversation.remove_message(message_id)
        self.save_data()

    def add_participant(self, conversation: Conversation, role_id: str) -> None:
        """
        Add a participant to a conversation.

        Args:
            conversation: The conversation to modify.
            role_id: The role ID to add.
        """
        conversation.add_participant(role_id)
        self.save_data()

    def remove_participant(self, conversation: Conversation, role_id: str) -> None:
        """
        Remove a participant from a conversation.

        Args:
            conversation: The conversation to modify.
            role_id: The role ID to remove.
        """
        conversation.remove_participant(role_id)
        self.save_data()

    def mute_role(self, conversation: Conversation, role_id: str) -> None:
        """
        Mute a role in a conversation.

        Args:
            conversation: The conversation to modify.
            role_id: The role ID to mute.
        """
        conversation.mute(role_id)
        self.save_data()

    def unmute_role(self, conversation: Conversation, role_id: str) -> None:
        """
        Unmute a role in a conversation.

        Args:
            conversation: The conversation to modify.
            role_id: The role ID to unmute.
        """
        conversation.unmute(role_id)
        self.save_data()

    # ==================== Message Generation ====================

    def determine_next_speaker(self, conversation: Conversation) -> str:
        """
        Determine the next speaker based on the speak mode.

        Args:
            conversation: The conversation to analyze.

        Returns:
            The role ID of the next speaker.
        """
        participants = conversation.participant_role_ids
        muted = conversation.muted_role_ids

        # Filter out muted roles (except narrator can always speak)
        active_participants = [
            id_ for id_ in participants
            if id_ == RoleCard.NARRATOR_ID or id_ not in muted
        ]

        if not active_participants:
            return RoleCard.NARRATOR_ID

        speak_mode = conversation.speak_mode

        if speak_mode == SpeakMode.MANUAL:
            next_id = conversation.next_speaker_id
            if next_id and next_id in active_participants:
                return next_id
            # Fall back to first active participant
            return active_participants[0]

        elif speak_mode == SpeakMode.ROUND_ROBIN:
            # Find the role with the fewest messages
            counts = conversation.message_counts
            return min(
                active_participants,
                key=lambda id_: counts.get(id_, 0),
                default=active_participants[0],
            )

        else:  # AUTO or default
            # Return the last assistant speaker's role, or first active
            for i in range(len(conversation.messages) - 1, -1, -1):
                msg = conversation.messages[i]
                if (
                    msg.role == MessageRole.ASSISTANT
                    and msg.role_id in active_participants
                ):
                    # Return a different role if possible
                    for p in active_participants:
                        if p != msg.role_id:
                            return p
                    return msg.role_id
            return active_participants[0]

    def build_context_messages(
        self, conversation: Conversation, current_speaker_id: str
    ) -> list[Message]:
        """
        Build the context messages for API request, including world book injection.

        Args:
            conversation: The conversation to build context from.
            current_speaker_id: The ID of the current speaker.

        Returns:
            List of messages for the API request.
        """
        context: list[Message] = []
        speaker = self.get_role_card(current_speaker_id)

        if not speaker:
            return context

        # Build system prompt
        system_prompt_parts = []

        # Add role's system prompt
        if speaker.system_prompt:
            system_prompt_parts.append(speaker.system_prompt)

        # Inject world book entries
        if conversation.world_book_id:
            book = self.get_world_book(conversation.world_book_id)
            if book:
                # Get the last user message for matching
                last_user_message = None
                for i in range(len(conversation.messages) - 1, -1, -1):
                    msg = conversation.messages[i]
                    if msg.role == MessageRole.USER:
                        last_user_message = msg.content
                        break

                if last_user_message:
                    matching_entries = book.find_matching_entries(last_user_message)
                    for entry in matching_entries:
                        comment = entry.comment if entry.comment else entry.key
                        system_prompt_parts.append(
                            f"\n\n[World Info: {comment}]\n{entry.content}"
                        )

        # Add system message
        if system_prompt_parts:
            sys_msg = Message(
                role=MessageRole.SYSTEM, content="\n".join(system_prompt_parts)
            )
            context.append(sys_msg)

        # Add conversation history
        context.extend(conversation.messages)

        return context

    def generate_response(
        self,
        conversation: Conversation,
        speaker_id: str,
        config: ApiRequestConfig,
    ) -> ApiResponse:
        """
        Generate a response from a specific role.

        Args:
            conversation: The conversation to generate a response for.
            speaker_id: The ID of the role that should respond.
            config: Configuration for the API request.

        Returns:
            ApiResponse containing the generated response.
        """
        speaker = self.get_role_card(speaker_id)
        if not speaker:
            return ApiResponse.error(f"Role not found: {speaker_id}")

        # Get the API key bound to this role
        api_key_id = speaker.api_key_id
        if not api_key_id:
            # Use first available API key
            if not self._api_keys:
                return ApiResponse.error(
                    "No API key configured. Please add an API key first."
                )
            api_key_id = self._api_keys[0].id

        api_key_config = self.get_api_key(api_key_id)
        if not api_key_config:
            return ApiResponse.error(f"API key not found for role: {speaker.name}")

        # Build context
        context = self.build_context_messages(conversation, speaker_id)

        # Send request
        response = self.api_client.send_chat_request(
            api_key=api_key_config.api_key or "",
            request_config=config,
            messages=context,
        )

        if response.success:
            # Create assistant message
            assistant_msg = Message(
                role=MessageRole.ASSISTANT,
                content=response.content,
                role_id=speaker_id,
                role_name=speaker.name,
            )
            assistant_msg.thought_content = response.thought_content
            conversation.add_message(assistant_msg)
            self.save_data()

        return response

    def regenerate_last_message(
        self, conversation: Conversation, config: ApiRequestConfig
    ) -> ApiResponse:
        """
        Regenerate the last assistant message.

        Args:
            conversation: The conversation to regenerate the message for.
            config: Configuration for the API request.

        Returns:
            ApiResponse containing the regenerated response.
        """
        # Find the last assistant message
        last_assistant_msg = None

        for i in range(len(conversation.messages) - 1, -1, -1):
            msg = conversation.messages[i]
            if msg.role == MessageRole.ASSISTANT:
                last_assistant_msg = msg
                break

        if not last_assistant_msg:
            return ApiResponse.error("No assistant message to regenerate.")

        # Remove the last assistant message
        conversation.remove_message(last_assistant_msg.id)

        # Generate new response with the same speaker
        return self.generate_response(conversation, last_assistant_msg.role_id, config)

    def close(self) -> None:
        """Close the API client connection."""
        self.api_client.close()

    def __enter__(self) -> "ConversationService":
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb) -> None:
        """Context manager exit."""
        self.close()
