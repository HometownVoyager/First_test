"""
DeepSeek API client for making HTTP requests.

This module provides the DeepSeekApiClient class which handles all HTTP
communication with the DeepSeek API, including chat completions and
balance inquiries.
"""

import json
import time
from typing import TYPE_CHECKING, Optional

import httpx

from .models import ApiRequestConfig, ApiResponse, DeepSeekModel

if TYPE_CHECKING:
    from ..domain.entities import Message


class DeepSeekApiClient:
    """
    Client for interacting with the DeepSeek API.

    This class handles HTTP communication with the DeepSeek API, including
    request building, response parsing, and error handling.

    Attributes:
        base_url: The base URL for the DeepSeek API.
        api_key: The API key for authentication.
        timeout: Request timeout in seconds.
    """

    DEFAULT_TIMEOUT = 30.0
    DEFAULT_READ_TIMEOUT = 120.0

    def __init__(
        self,
        base_url: str = "https://api.deepseek.com",
        api_key: Optional[str] = None,
        timeout: float = DEFAULT_TIMEOUT,
        read_timeout: float = DEFAULT_READ_TIMEOUT,
    ) -> None:
        """
        Initialize the DeepSeek API client.

        Args:
            base_url: The base URL for the DeepSeek API.
            api_key: The API key for authentication.
            timeout: Connection timeout in seconds.
            read_timeout: Read timeout in seconds.
        """
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout
        self.read_timeout = read_timeout
        self._client: Optional[httpx.Client] = None

    def _get_client(self) -> httpx.Client:
        """
        Get or create an HTTP client instance.

        Returns:
            An httpx.Client instance configured for DeepSeek API.
        """
        if self._client is None or self._client.is_closed:
            self._client = httpx.Client(
                timeout=httpx.Timeout(
                    connect=self.timeout,
                    read=self.read_timeout,
                    write=self.timeout,
                ),
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {self.api_key}",
                },
            )
        return self._client

    def send_chat_request(
        self,
        api_key: str,
        request_config: ApiRequestConfig,
        messages: list["Message"],
    ) -> ApiResponse:
        """
        Send a chat completion request to the DeepSeek API.

        Args:
            api_key: The API key for authentication.
            request_config: Configuration for the request (model, parameters, etc.).
            messages: List of messages in the conversation.

        Returns:
            ApiResponse containing the response content and metadata.

        Raises:
            httpx.HTTPError: If the HTTP request fails.
        """
        url = f"{self.base_url}/chat/completions"

        # Build request body
        body: dict = {
            "model": request_config.model.model_id,
            "stream": request_config.stream,
            "temperature": request_config.temperature,
            "max_tokens": request_config.max_tokens,
            "top_p": request_config.top_p,
            "n": request_config.n,
        }

        # Add thinking/reasoning mode parameter if enabled
        if request_config.thinking_enabled:
            extra_body: dict = {"enable_thinking": True}
            # For reasoner model, also add reasoning_effort
            if request_config.model == DeepSeekModel.DEEPSEEK_REASONER:
                extra_body["reasoning_effort"] = "high"
            body["extra_body"] = extra_body

        # Build messages array
        messages_array = []
        for msg in messages:
            if msg.content is None:
                continue

            msg_obj: dict = {
                "role": msg.role.value,
            }

            # Handle thought content for assistant messages
            if msg.thought_content and msg.thought_content.strip():
                msg_obj["content"] = f"{msg.thought_content}\n\n{msg.content}"
            else:
                msg_obj["content"] = msg.content

            messages_array.append(msg_obj)

        body["messages"] = messages_array

        # Make the request
        client = self._get_client()
        # Temporarily update auth header with provided api_key
        original_headers = client.headers.copy()
        client.headers["Authorization"] = f"Bearer {api_key}"

        try:
            response = client.post(url, json=body)
            response.raise_for_status()
            response_data = response.json()
        finally:
            # Restore original headers
            client.headers = original_headers

        # Extract choices
        choices = response_data.get("choices", [])
        if not choices:
            return ApiResponse.error("No choices in API response.")

        first_choice = choices[0]
        message = first_choice.get("message", {})
        if not message:
            return ApiResponse.error("No message in API response.")

        assistant_content = message.get("content", "")
        thought_content = message.get("reasoning_content") or message.get("thought")

        # Extract usage statistics
        usage = response_data.get("usage", {})
        prompt_tokens = usage.get("prompt_tokens", 0)
        completion_tokens = usage.get("completion_tokens", 0)

        response_id = response_data.get("id")

        return ApiResponse.success(
            response_id=response_id,
            content=assistant_content,
            thought_content=thought_content,
            prompt_tokens=prompt_tokens,
            completion_tokens=completion_tokens,
        )

    def get_balance(self, api_key: str) -> float:
        """
        Fetch the current balance from the DeepSeek API.

        Args:
            api_key: The API key for authentication.

        Returns:
            Balance amount, or -1.0 if fetch failed.

        Raises:
            httpx.HTTPError: If the HTTP request fails.
        """
        url = f"{self.base_url}/user/balance"

        client = self._get_client()
        original_headers = client.headers.copy()
        client.headers["Authorization"] = f"Bearer {api_key}"

        try:
            response = client.get(url)
            if not response.is_success:
                return -1.0

            balance_data = response.json()
            if "balance" in balance_data:
                return float(balance_data["balance"])
            elif "total_balance" in balance_data:
                return float(balance_data["total_balance"])
        except Exception:
            pass
        finally:
            client.headers = original_headers

        return -1.0

    def close(self) -> None:
        """Close the underlying HTTP client."""
        if self._client and not self._client.is_closed:
            self._client.close()

    def __enter__(self) -> "DeepSeekApiClient":
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb) -> None:
        """Context manager exit."""
        self.close()
