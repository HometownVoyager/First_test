"""
Tests for utility helpers.

This module contains unit tests for the utility helper functions,
including token estimation, cost calculation, and text formatting.
"""

import pytest

from deepseek_chat.utils.helpers import (
    estimate_cost,
    estimate_token_count,
    format_timestamp,
    mask_api_key,
    sanitize_filename,
    truncate_text,
)


class TestEstimateTokenCount:
    """Tests for the estimate_token_count function."""

    def test_empty_string(self) -> None:
        """Test with empty string."""
        assert estimate_token_count("") == 0
        assert estimate_token_count(None) == 0  # type: ignore

    def test_short_text(self) -> None:
        """Test with short text."""
        # "Hello" = 5 chars, roughly 1-2 tokens
        result = estimate_token_count("Hello")
        assert result >= 1

    def test_longer_text(self) -> None:
        """Test with longer text."""
        text = "This is a longer piece of text that should result in multiple tokens."
        result = estimate_token_count(text)
        # ~70 chars / 4 = ~17-18 tokens
        assert result > 10


class TestEstimateCost:
    """Tests for the estimate_cost function."""

    def test_zero_tokens(self) -> None:
        """Test with zero tokens."""
        cost = estimate_cost(0, 0)
        assert cost == 0.0

    def test_basic_calculation(self) -> None:
        """Test basic cost calculation."""
        cost = estimate_cost(1000, 500, "deepseek-chat")
        # prompt: 1000/1000 * 0.00027 = 0.00027
        # completion: 500/1000 * 0.0011 = 0.00055
        # total: 0.00082
        assert abs(cost - 0.00082) < 0.00001

    def test_different_models(self) -> None:
        """Test cost calculation for different models."""
        cost_chat = estimate_cost(1000, 1000, "deepseek-chat")
        cost_reasoner = estimate_cost(1000, 1000, "deepseek-reasoner")

        # Reasoner should be more expensive
        assert cost_reasoner > cost_chat


class TestFormatTimestamp:
    """Tests for the format_timestamp function."""

    def test_valid_timestamp(self) -> None:
        """Test formatting a valid timestamp."""
        # 2024-01-15 14:30:00 UTC
        timestamp_ms = 1705329000000
        result = format_timestamp(timestamp_ms)

        assert "2024-01-15" in result
        assert "14:30:00" in result


class TestTruncateText:
    """Tests for the truncate_text function."""

    def test_no_truncation_needed(self) -> None:
        """Test when no truncation is needed."""
        text = "Short text"
        result = truncate_text(text, max_length=50)
        assert result == text

    def test_truncation_with_suffix(self) -> None:
        """Test truncation adds suffix."""
        text = "This is a much longer text that needs truncation"
        result = truncate_text(text, max_length=20)
        assert len(result) == 20
        assert result.endswith("...")

    def test_empty_input(self) -> None:
        """Test with empty input."""
        assert truncate_text("", max_length=10) == ""
        assert truncate_text(None, max_length=10) == ""  # type: ignore


class TestMaskApiKey:
    """Tests for the mask_api_key function."""

    def test_normal_key(self) -> None:
        """Test masking a normal-length key."""
        key = "sk-abcdefghij1234567890"
        masked = mask_api_key(key)
        assert masked == "sk-a...7890"

    def test_short_key(self) -> None:
        """Test masking a short key."""
        key = "short"
        masked = mask_api_key(key)
        assert masked == "****"

    def test_empty_key(self) -> None:
        """Test masking an empty key."""
        masked = mask_api_key("")
        assert masked == "****"


class TestSanitizeFilename:
    """Tests for the sanitize_filename function."""

    def test_valid_filename(self) -> None:
        """Test with a valid filename."""
        filename = "normal_file.txt"
        result = sanitize_filename(filename)
        assert result == filename

    def test_invalid_characters(self) -> None:
        """Test removal of invalid characters."""
        filename = "file<name>:with*invalid?chars.txt"
        result = sanitize_filename(filename)
        assert "<" not in result
        assert ">" not in result
        assert ":" not in result
        assert "*" not in result
        assert "?" not in result

    def test_long_filename(self) -> None:
        """Test truncation of long filenames."""
        filename = "a" * 300 + ".txt"
        result = sanitize_filename(filename)
        assert len(result) <= 255
