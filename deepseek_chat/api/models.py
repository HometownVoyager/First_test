"""
API models for DeepSeek API interactions.

This module defines the data structures used for API requests and responses,
including model enums, request configurations, and response objects.
"""

from dataclasses import dataclass, field
from enum import Enum
from typing import Optional


class DeepSeekModel(Enum):
    """
    Available DeepSeek models.

    Attributes:
        model_id: The actual model identifier string for API calls.
        display_name: Human-readable name for UI display.
    """

    DEEPSEEK_V4_FLASH = ("deepseek-v4-flash", "DeepSeek V4 Flash")
    DEEPSEEK_V4_PRO = ("deepseek-v4-pro", "DeepSeek V4 Pro")
    DEEPSEEK_CHAT = ("deepseek-chat", "DeepSeek Chat")
    DEEPSEEK_REASONER = ("deepseek-reasoner", "DeepSeek Reasoner")

    def __init__(self, model_id: str, display_name: str) -> None:
        self.model_id = model_id
        self.display_name = display_name

    @classmethod
    def from_string(cls, model_str: str) -> "DeepSeekModel":
        """
        Get a DeepSeekModel enum from its model_id string.

        Args:
            model_str: The model identifier string.

        Returns:
            The corresponding DeepSeekModel enum value, or DEEPSEEK_CHAT as default.
        """
        for model in cls:
            if model.model_id == model_str:
                return model
        return cls.DEEPSEEK_CHAT


@dataclass
class ApiRequestConfig:
    """
    Configuration for a single API request.

    This class holds all parameters that can be customized when making
    a chat completion request to the DeepSeek API.

    Attributes:
        model: The model to use for the request.
        thinking_enabled: Whether to enable thinking/reasoning mode.
        temperature: Sampling temperature (0.0 to 2.0).
        max_tokens: Maximum number of tokens to generate.
        top_p: Nucleus sampling parameter.
        n: Number of completions to generate.
        stream: Whether to stream the response.
    """

    model: DeepSeekModel = DeepSeekModel.DEEPSEEK_CHAT
    thinking_enabled: bool = False
    temperature: float = 0.7
    max_tokens: int = 2048
    top_p: float = 1.0
    n: int = 1
    stream: bool = False

    def copy(self) -> "ApiRequestConfig":
        """
        Create a copy of this configuration.

        Returns:
            A new ApiRequestConfig instance with the same values.
        """
        return ApiRequestConfig(
            model=self.model,
            thinking_enabled=self.thinking_enabled,
            temperature=self.temperature,
            max_tokens=self.max_tokens,
            top_p=self.top_p,
            n=self.n,
            stream=self.stream,
        )


@dataclass
class ApiResponse:
    """
    Represents the response from a DeepSeek API call.

    This class encapsulates both successful and error responses,
    providing convenient access to response content and metadata.

    Attributes:
        id: Unique identifier for the response.
        content: The main content of the assistant's response.
        thought_content: Content from thinking/reasoning mode (if available).
        prompt_tokens: Number of tokens in the prompt.
        completion_tokens: Number of tokens in the completion.
        total_tokens: Total tokens used (prompt + completion).
        timestamp: Unix timestamp when the response was created.
        success: Whether the API call was successful.
        error_message: Error message if the call failed.
    """

    id: Optional[str] = None
    content: Optional[str] = None
    thought_content: Optional[str] = None
    prompt_tokens: int = 0
    completion_tokens: int = 0
    total_tokens: int = 0
    timestamp: int = field(default_factory=lambda: int(__import__("time").time() * 1000))
    success: bool = True
    error_message: Optional[str] = None

    @classmethod
    def success(
        cls,
        response_id: str,
        content: str,
        thought_content: Optional[str],
        prompt_tokens: int,
        completion_tokens: int,
    ) -> "ApiResponse":
        """
        Create a successful API response.

        Args:
            response_id: Unique identifier for the response.
            content: The assistant's response content.
            thought_content: Optional thinking/reasoning content.
            prompt_tokens: Number of tokens in the prompt.
            completion_tokens: Number of tokens in the completion.

        Returns:
            An ApiResponse instance representing success.
        """
        return cls(
            id=response_id,
            content=content,
            thought_content=thought_content,
            prompt_tokens=prompt_tokens,
            completion_tokens=completion_tokens,
            total_tokens=prompt_tokens + completion_tokens,
            success=True,
        )

    @classmethod
    def error(cls, error_message: str) -> "ApiResponse":
        """
        Create an error API response.

        Args:
            error_message: Description of the error.

        Returns:
            An ApiResponse instance representing failure.
        """
        return cls(
            success=False,
            error_message=error_message,
        )
