#!/usr/bin/env python3
"""
DeepSeek AI Chat - Console Application

A console-based AI chat application with role-playing features,
connecting to the DeepSeek API.

This is the main entry point for the Python refactored version of
the DeepSeek Chat application.
"""

import sys
from typing import Optional

from deepseek_chat.api.models import ApiRequestConfig, DeepSeekModel
from deepseek_chat.domain.entities import MessageRole, RoleCard
from deepseek_chat.services.conversation_service import ConversationService
from deepseek_chat.services.storage_service import StorageService
from deepseek_chat.utils.helpers import estimate_cost, format_timestamp


def print_welcome() -> None:
    """Print welcome message."""
    print("=" * 60)
    print("DeepSeek AI Chat - Console Application")
    print("=" * 60)
    print()
    print("Commands:")
    print("  /exit     - Exit the application")
    print("  /balance  - Check account balance")
    print("  /clear    - Clear conversation history")
    print("  /model    - Show/change current model")
    print("  /help     - Show this help message")
    print()


def get_user_input(prompt: str = "> ") -> Optional[str]:
    """
    Get user input from the console.

    Args:
        prompt: The prompt to display.

    Returns:
        User input string, or None if EOF.
    """
    try:
        return input(prompt).strip()
    except EOFError:
        return None


def handle_command(
    command: str,
    service: ConversationService,
    config: ApiRequestConfig,
) -> bool:
    """
    Handle a user command.

    Args:
        command: The command string (without leading slash).
        service: The conversation service.
        config: Current request configuration.

    Returns:
        True if the application should continue, False to exit.
    """
    cmd = command.lower().strip()

    if cmd == "exit" or cmd == "quit":
        print("Goodbye!")
        return False

    elif cmd == "help":
        print_welcome()

    elif cmd == "balance":
        api_keys = service.get_all_api_keys()
        if not api_keys:
            print("No API keys configured.")
        else:
            api_key = api_keys[0]
            try:
                balance = service.api_client.get_balance(api_key.api_key or "")
                if balance >= 0:
                    print(f"Current balance: ${balance:.4f}")
                else:
                    print("Failed to fetch balance.")
            except Exception as e:
                print(f"Error fetching balance: {e}")

    elif cmd == "clear":
        conversations = service.get_all_conversations()
        if conversations:
            current = conversations[0]
            current.messages.clear()
            current.message_counts.clear()
            service.save_data()
            print("Conversation history cleared.")
        else:
            print("No active conversation.")

    elif cmd.startswith("model"):
        parts = cmd.split(maxsplit=1)
        if len(parts) > 1:
            # Change model
            model_str = parts[1].lower()
            try:
                new_model = DeepSeekModel.from_string(model_str)
                config.model = new_model
                print(f"Model changed to: {new_model.display_name}")
            except ValueError:
                print(f"Unknown model: {model_str}")
                print(f"Available models: {[m.model_id for m in DeepSeekModel]}")
        else:
            # Show current model
            print(f"Current model: {config.model.display_name} ({config.model.model_id})")

    else:
        print(f"Unknown command: /{cmd}")
        print("Type /help for available commands.")

    return True


def main() -> int:
    """
    Main entry point for the console application.

    Returns:
        Exit code (0 for success, non-zero for error).
    """
    print_welcome()

    # Initialize services
    storage = StorageService()
    service = ConversationService(storage)

    # Request configuration
    config = ApiRequestConfig()

    # Check for existing API keys
    api_keys = service.get_all_api_keys()
    if not api_keys:
        print("No API keys configured. Please enter your DeepSeek API key:")
        api_key_value = get_user_input("API Key: ")
        if not api_key_value:
            print("API key is required. Exiting.")
            return 1

        from deepseek_chat.domain.entities import ApiKeyConfig

        key_config = ApiKeyConfig(name="Default", api_key=api_key_value)
        service.save_api_key(key_config)
        api_keys = [key_config]
        print("API key saved.")

    # Get or create a conversation
    conversations = service.get_all_conversations()
    if not conversations:
        conv = service.create_conversation("New Conversation")
        print(f"Created new conversation: {conv.title}")
    else:
        conv = conversations[0]
        print(f"Using conversation: {conv.title}")

    print()
    print("Start chatting! Type your message and press Enter.")
    print()

    # Main chat loop
    while True:
        user_input = get_user_input()
        if user_input is None:
            print("\nGoodbye!")
            break

        if not user_input:
            continue

        # Check for commands
        if user_input.startswith("/"):
            command = user_input[1:]
            if not handle_command(command, service, config):
                break
            print()
            continue

        # Add user message
        service.add_user_message(conv, user_input)

        # Determine next speaker
        next_speaker_id = service.determine_next_speaker(conv)
        speaker = service.get_role_card(next_speaker_id)
        speaker_name = speaker.name if speaker else "Assistant"

        print(f"\n{speaker_name} is thinking...")

        try:
            # Generate response
            response = service.generate_response(conv, next_speaker_id, config)

            if response.success:
                print(f"\n{speaker_name}:")
                print(response.content or "[No content]")

                # Show token usage and cost
                if response.total_tokens > 0:
                    cost = estimate_cost(
                        response.prompt_tokens,
                        response.completion_tokens,
                        config.model.model_id,
                    )
                    print(
                        f"\n[Tokens: {response.prompt_tokens} + {response.completion_tokens} = {response.total_tokens}, "
                        f"Estimated cost: ${cost:.6f}]"
                    )
            else:
                print(f"Error: {response.error_message}")

        except Exception as e:
            print(f"Error generating response: {e}")

        print()

    # Cleanup
    service.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
