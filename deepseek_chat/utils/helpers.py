"""
Helper utility functions for the DeepSeek Chat application.

This module provides common utility functions for token estimation,
cost calculation, text formatting, and other helper operations.
"""

import re
from datetime import datetime
from typing import Optional


def estimate_token_count(text: str) -> int:
    """
    Estimate the number of tokens in a text string.

    This is a rough estimation based on character count. For accurate
    token counting, use the tokenizer specific to the model.

    Args:
        text: The text to estimate tokens for.

    Returns:
        Estimated number of tokens.
    """
    if not text:
        return 0

    # Rough estimation: ~4 characters per token for English
    # This varies significantly by language and content type
    char_count = len(text)
    return max(1, int(char_count / 4))


def estimate_cost(
    prompt_tokens: int,
    completion_tokens: int,
    model_id: str = "deepseek-chat",
) -> float:
    """
    Estimate the cost of an API request based on token usage.

    Note: These are example prices. Check DeepSeek's official pricing
    for current rates.

    Args:
        prompt_tokens: Number of tokens in the prompt.
        completion_tokens: Number of tokens in the completion.
        model_id: The model identifier to get pricing for.

    Returns:
        Estimated cost in USD.
    """
    # Example pricing (update with actual DeepSeek pricing)
    pricing = {
        "deepseek-chat": {"prompt": 0.00027, "completion": 0.0011},
        "deepseek-reasoner": {"prompt": 0.00055, "completion": 0.0022},
        "deepseek-v4-flash": {"prompt": 0.00014, "completion": 0.00028},
        "deepseek-v4-pro": {"prompt": 0.00055, "completion": 0.0022},
    }

    rates = pricing.get(model_id, pricing["deepseek-chat"])
    prompt_cost = (prompt_tokens / 1000) * rates["prompt"]
    completion_cost = (completion_tokens / 1000) * rates["completion"]

    return prompt_cost + completion_cost


def format_timestamp(timestamp_ms: int) -> str:
    """
    Format a Unix timestamp (in milliseconds) to a human-readable string.

    Args:
        timestamp_ms: Unix timestamp in milliseconds.

    Returns:
        Formatted date-time string (e.g., "2024-01-15 14:30:00").
    """
    dt = datetime.fromtimestamp(timestamp_ms / 1000)
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def truncate_text(text: str, max_length: int = 50, suffix: str = "...") -> str:
    """
    Truncate text to a maximum length with an optional suffix.

    Args:
        text: The text to truncate.
        max_length: Maximum length of the output string.
        suffix: Suffix to append if text is truncated.

    Returns:
        Truncated text with suffix if necessary.
    """
    if not text or len(text) <= max_length:
        return text or ""

    return text[: max_length - len(suffix)] + suffix


def mask_api_key(api_key: str, visible_chars: int = 4) -> str:
    """
    Mask an API key for safe display.

    Args:
        api_key: The API key to mask.
        visible_chars: Number of characters to show at start and end.

    Returns:
        Masked API key string.
    """
    if not api_key or len(api_key) < visible_chars * 2:
        return "****"

    return f"{api_key[:visible_chars]}...{api_key[-visible_chars:]}"


def sanitize_filename(filename: str) -> str:
    """
    Sanitize a string to be safe for use as a filename.

    Args:
        filename: The original filename.

    Returns:
        Sanitized filename safe for filesystem use.
    """
    # Remove or replace invalid characters
    sanitized = re.sub(r'[<>:"/\\|?*]', "_", filename)
    # Remove leading/trailing spaces and dots
    sanitized = sanitized.strip(" .")
    # Limit length
    if len(sanitized) > 255:
        sanitized = sanitized[:255]
    return sanitized or "unnamed"


def parse_model_id(model_string: str) -> str:
    """
    Parse a model identifier from various input formats.

    Args:
        model_string: Model identifier string.

    Returns:
        Normalized model identifier.
    """
    # Common aliases
    aliases = {
        "chat": "deepseek-chat",
        "reasoner": "deepseek-reasoner",
        "v4": "deepseek-v4-pro",
        "flash": "deepseek-v4-flash",
    }

    normalized = model_string.lower().strip()
    return aliases.get(normalized, normalized)
