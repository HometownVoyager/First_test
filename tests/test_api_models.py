"""
Tests for API models.

This module contains unit tests for the API-related data models,
including DeepSeekModel enum, ApiRequestConfig, and ApiResponse.
"""

import pytest

from deepseek_chat.api.models import (
    ApiRequestConfig,
    ApiResponse,
    DeepSeekModel,
)


class TestDeepSeekModel:
    """Tests for the DeepSeekModel enum."""

    def test_model_values(self) -> None:
        """Test that model enum values are correct."""
        assert DeepSeekModel.DEEPSEEK_CHAT.model_id == "deepseek-chat"
        assert DeepSeekModel.DEEPSEEK_CHAT.display_name == "DeepSeek Chat"

        assert DeepSeekModel.DEEPSEEK_REASONER.model_id == "deepseek-reasoner"
        assert DeepSeekModel.DEEPSEEK_REASONER.display_name == "DeepSeek Reasoner"

        assert DeepSeekModel.DEEPSEEK_V4_FLASH.model_id == "deepseek-v4-flash"
        assert DeepSeekModel.DEEPSEEK_V4_FLASH.display_name == "DeepSeek V4 Flash"

        assert DeepSeekModel.DEEPSEEK_V4_PRO.model_id == "deepseek-v4-pro"
        assert DeepSeekModel.DEEPSEEK_V4_PRO.display_name == "DeepSeek V4 Pro"

    def test_from_string_valid(self) -> None:
        """Test from_string method with valid inputs."""
        assert DeepSeekModel.from_string("deepseek-chat") == DeepSeekModel.DEEPSEEK_CHAT
        assert (
            DeepSeekModel.from_string("deepseek-reasoner")
            == DeepSeekModel.DEEPSEEK_REASONER
        )

    def test_from_string_invalid(self) -> None:
        """Test from_string method with invalid input returns default."""
        result = DeepSeekModel.from_string("invalid-model")
        assert result == DeepSeekModel.DEEPSEEK_CHAT


class TestApiRequestConfig:
    """Tests for the ApiRequestConfig dataclass."""

    def test_default_values(self) -> None:
        """Test that default values are set correctly."""
        config = ApiRequestConfig()

        assert config.model == DeepSeekModel.DEEPSEEK_CHAT
        assert config.thinking_enabled is False
        assert config.temperature == 0.7
        assert config.max_tokens == 2048
        assert config.top_p == 1.0
        assert config.n == 1
        assert config.stream is False

    def test_custom_values(self) -> None:
        """Test creating config with custom values."""
        config = ApiRequestConfig(
            model=DeepSeekModel.DEEPSEEK_REASONER,
            thinking_enabled=True,
            temperature=0.9,
            max_tokens=4096,
        )

        assert config.model == DeepSeekModel.DEEPSEEK_REASONER
        assert config.thinking_enabled is True
        assert config.temperature == 0.9
        assert config.max_tokens == 4096

    def test_copy(self) -> None:
        """Test that copy creates an independent clone."""
        original = ApiRequestConfig(
            model=DeepSeekModel.DEEPSEEK_REASONER,
            temperature=0.9,
        )

        copied = original.copy()

        # Values should be equal
        assert copied.model == original.model
        assert copied.temperature == original.temperature

        # But they should be independent
        copied.temperature = 0.5
        assert original.temperature == 0.9
        assert copied.temperature == 0.5


class TestApiResponse:
    """Tests for the ApiResponse dataclass."""

    def test_success_response(self) -> None:
        """Test creating a successful response."""
        response = ApiResponse.success(
            response_id="test-123",
            content="Hello, world!",
            thought_content="Thinking...",
            prompt_tokens=10,
            completion_tokens=20,
        )

        assert response.id == "test-123"
        assert response.content == "Hello, world!"
        assert response.thought_content == "Thinking..."
        assert response.prompt_tokens == 10
        assert response.completion_tokens == 20
        assert response.total_tokens == 30
        assert response.success is True
        assert response.error_message is None

    def test_error_response(self) -> None:
        """Test creating an error response."""
        response = ApiResponse.error("Something went wrong")

        assert response.success is False
        assert response.error_message == "Something went wrong"
        assert response.content is None

    def test_default_timestamp(self) -> None:
        """Test that timestamp is set on creation."""
        response = ApiResponse()
        assert response.timestamp > 0
