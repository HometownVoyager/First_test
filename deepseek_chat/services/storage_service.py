"""
Storage service for data persistence.

This module handles all file-based persistence operations, including
loading and saving conversations, role cards, API keys, and world books.
It also provides import/export functionality for SillyTavern format.
"""

import json
import os
import base64
from pathlib import Path
from typing import Any, Generic, TypeVar

from ..domain.entities import (
    ApiKeyConfig,
    Conversation,
    RoleCard,
    WorldBook,
    WorldBookEntry,
    WorldBookEntryPosition,
)

T = TypeVar("T")


class StorageService:
    """
    Handles persistence of all application data to local files.

    This class manages file-based storage for conversations, role cards,
    API keys, and world books. It uses JSON format for serialization
    and supports SillyTavern format import/export.

    Attributes:
        data_dir: The directory where data files are stored.
    """

    def __init__(self, data_dir: str | None = None) -> None:
        """
        Initialize the storage service.

        Args:
            data_dir: Custom data directory path. If None, uses default location.
        """
        if data_dir:
            self.data_dir = Path(data_dir)
        else:
            home = Path.home()
            self.data_dir = home / ".deepseek-chat-app"

        # Ensure data directory exists
        self.data_dir.mkdir(parents=True, exist_ok=True)

        # File paths
        self.conversations_file = self.data_dir / "conversations.json"
        self.role_cards_file = self.data_dir / "role_cards.json"
        self.api_keys_file = self.data_dir / "api_keys.json"
        self.world_books_file = self.data_dir / "world_books.json"
        self.settings_file = self.data_dir / "settings.json"

    # ==================== Conversations ====================

    def load_conversations(self) -> list[Conversation]:
        """
        Load all conversations from file.

        Returns:
            List of Conversation objects.
        """
        return self._load_list(self.conversations_file, Conversation)

    def save_conversations(self, conversations: list[Conversation]) -> None:
        """
        Save all conversations to file.

        Args:
            conversations: List of Conversation objects to save.
        """
        self._save_list(self.conversations_file, conversations)

    # ==================== Role Cards ====================

    def load_role_cards(self) -> list[RoleCard]:
        """
        Load all role cards from file.

        Returns:
            List of RoleCard objects, ensuring narrator always exists.
        """
        cards = self._load_list(self.role_cards_file, RoleCard)
        # Ensure narrator always exists
        has_narrator = any(card.id == "__narrator__" for card in cards)
        if not has_narrator:
            cards.append(RoleCard.create_narrator())
        return cards

    def save_role_cards(self, role_cards: list[RoleCard]) -> None:
        """
        Save all role cards to file.

        Args:
            role_cards: List of RoleCard objects to save.
        """
        self._save_list(self.role_cards_file, role_cards)

    # ==================== API Keys ====================

    def load_api_keys(self) -> list[ApiKeyConfig]:
        """
        Load all API key configurations from file.

        Returns:
            List of ApiKeyConfig objects.
        """
        return self._load_list(self.api_keys_file, ApiKeyConfig)

    def save_api_keys(self, api_keys: list[ApiKeyConfig]) -> None:
        """
        Save all API key configurations to file.

        Args:
            api_keys: List of ApiKeyConfig objects to save.
        """
        self._save_list(self.api_keys_file, api_keys)

    # ==================== World Books ====================

    def load_world_books(self) -> list[WorldBook]:
        """
        Load all world books from file.

        Returns:
            List of WorldBook objects.
        """
        return self._load_list(self.world_books_file, WorldBook)

    def save_world_books(self, world_books: list[WorldBook]) -> None:
        """
        Save all world books to file.

        Args:
            world_books: List of WorldBook objects to save.
        """
        self._save_list(self.world_books_file, world_books)

    # ==================== Generic List Load/Save ====================

    def _load_list(self, file_path: Path, cls: type[T]) -> list[T]:
        """
        Load a list of objects from a JSON file.

        Args:
            file_path: Path to the JSON file.
            cls: The class type to deserialize to.

        Returns:
            List of deserialized objects.
        """
        if not file_path.exists():
            return []

        try:
            with open(file_path, "r", encoding="utf-8") as f:
                data = json.load(f)
                if isinstance(data, dict) and "items" in data:
                    items_data = data["items"]
                    return [self._deserialize_object(item, cls) for item in items_data]
        except Exception as e:
            print(f"Error loading {file_path}: {e}")

        return []

    def _save_list(self, file_path: Path, items: list[Any]) -> None:
        """
        Save a list of objects to a JSON file.

        Args:
            file_path: Path to the JSON file.
            items: List of objects to save.
        """
        data = {"items": [self._serialize_object(item) for item in items]}

        try:
            with open(file_path, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
        except Exception as e:
            print(f"Error saving {file_path}: {e}")

    def _serialize_object(self, obj: Any) -> dict:
        """
        Serialize an object to a dictionary.

        Args:
            obj: The object to serialize.

        Returns:
            Dictionary representation of the object.
        """
        if hasattr(obj, "__dataclass_fields__"):
            from dataclasses import asdict

            return asdict(obj)
        elif hasattr(obj, "__dict__"):
            return obj.__dict__
        return obj

    def _deserialize_object(self, data: dict, cls: type[T]) -> T:
        """
        Deserialize a dictionary to an object.

        Args:
            data: The dictionary to deserialize.
            cls: The target class type.

        Returns:
            Deserialized object.
        """
        if hasattr(cls, "__dataclass_fields__"):
            # Handle enum fields
            cleaned_data = {}
            for key, value in data.items():
                if key == "role" and cls == Conversation.__class__:  # Wrong check
                    pass
                cleaned_data[key] = value

            # Create instance using **kwargs
            try:
                return cls(**cleaned_data)  # type: ignore
            except TypeError:
                # If some fields don't match, create with available fields
                obj = cls()
                for key, value in cleaned_data.items():
                    if hasattr(obj, key):
                        setattr(obj, key, value)
                return obj
        return data  # type: ignore

    # ==================== SillyTavern Import/Export ====================

    def import_sillytavern_character(self, json_content: str) -> RoleCard | None:
        """
        Import a SillyTavern character card from JSON string.

        Args:
            json_content: JSON string of the character card.

        Returns:
            RoleCard object, or None if import failed.
        """
        try:
            data = json.loads(json_content)
            card = RoleCard()

            # Extract standard fields
            card.name = data.get("name")
            card.description = data.get("description")
            card.personality = data.get("personality")
            card.scenario = data.get("scenario")
            card.first_mes = data.get("first_mes")
            card.mes_example = data.get("mes_example")
            card.creator_notes = data.get("creator_notes")
            card.avatar = data.get("avatar")

            # Store raw data for compatibility
            card.data = json_content
            card.updated_at = int(__import__("time").time() * 1000)

            return card
        except Exception as e:
            print(f"Error importing character: {e}")
            return None

    def export_sillytavern_character(self, card: RoleCard) -> str:
        """
        Export a role card to SillyTavern format JSON.

        Args:
            card: The RoleCard to export.

        Returns:
            JSON string in SillyTavern format.
        """
        data = {
            "spec": "chara_card_v3",
            "spec_version": "3.0",
        }

        if card.name:
            data["name"] = card.name
        if card.description:
            data["description"] = card.description
        if card.personality:
            data["personality"] = card.personality
        if card.scenario:
            data["scenario"] = card.scenario
        if card.first_mes:
            data["first_mes"] = card.first_mes
        if card.mes_example:
            data["mes_example"] = card.mes_example
        if card.creator_notes:
            data["creator_notes"] = card.creator_notes
        if card.avatar:
            data["avatar"] = card.avatar

        return json.dumps(data, indent=2, ensure_ascii=False)

    def import_sillytavern_world_book(self, json_content: str) -> WorldBook | None:
        """
        Import a SillyTavern world book from JSON string.

        Args:
            json_content: JSON string of the world book.

        Returns:
            WorldBook object, or None if import failed.
        """
        try:
            data = json.loads(json_content)
            book = WorldBook()

            book.name = data.get("name")
            book.description = data.get("description")

            # Parse entries
            entries_obj = data.get("entries", {})
            if isinstance(entries_obj, dict):
                for key in entries_obj:
                    entry_data = entries_obj[key]
                    entry = WorldBookEntry()

                    entry.key = entry_data.get("key")
                    entry.content = entry_data.get("content")
                    entry.comment = entry_data.get("comment")
                    entry.priority = entry_data.get("priority", 0)
                    entry.enabled = entry_data.get("enabled", True)

                    position_str = entry_data.get("position", "before")
                    entry.position = (
                        WorldBookEntryPosition.AFTER
                        if position_str.lower() == "after"
                        else WorldBookEntryPosition.BEFORE
                    )

                    book.add_entry(entry)

            return book
        except Exception as e:
            print(f"Error importing world book: {e}")
            return None

    def export_sillytavern_world_book(self, book: WorldBook) -> str:
        """
        Export a world book to SillyTavern format JSON.

        Args:
            book: The WorldBook to export.

        Returns:
            JSON string in SillyTavern format.
        """
        data: dict = {}

        if book.name:
            data["name"] = book.name
        if book.description:
            data["description"] = book.description

        entries_obj = {}
        for index, entry in enumerate(book.entries):
            entry_data = {
                "key": entry.key,
                "content": entry.content,
                "comment": entry.comment,
                "priority": entry.priority,
                "enabled": entry.enabled,
                "position": "after" if entry.position == WorldBookEntryPosition.AFTER else "before",
            }
            entries_obj[str(index)] = entry_data

        data["entries"] = entries_obj

        return json.dumps(data, indent=2, ensure_ascii=False)

    # ==================== PNG Character Card (with embedded JSON) ====================

    def read_png_chara_data(self, png_file: Path) -> str | None:
        """
        Read JSON data from a PNG file's tEXt chunk (SillyTavern format).

        Args:
            png_file: Path to the PNG file.

        Returns:
            JSON string from the PNG, or None if not found.
        """
        try:
            with open(png_file, "rb") as f:
                # Skip PNG signature (8 bytes)
                signature = f.read(8)
                if signature != b"\x89PNG\r\n\x1a\n":
                    return None

                # Read chunks until we find tEXt with "chara" keyword
                while f.tell() < os.fstat(f.fileno()).st_size:
                    # Read chunk length
                    length_bytes = f.read(4)
                    if len(length_bytes) < 4:
                        break
                    length = int.from_bytes(length_bytes, "big")

                    # Read chunk type
                    chunk_type = f.read(4).decode("ascii", errors="ignore")

                    if chunk_type == "tEXt":
                        # Read chunk data
                        chunk_data = f.read(length)
                        text = chunk_data.decode("latin-1", errors="ignore")

                        # Check for chara keyword
                        if text.startswith("chara="):
                            base64_data = text[6:]
                            decoded = base64.b64decode(base64_data)
                            return decoded.decode("utf-8")
                    else:
                        # Skip chunk data and CRC
                        f.seek(length + 4, os.SEEK_CUR)

        except Exception as e:
            print(f"Error reading PNG chara data: {e}")

        return None
