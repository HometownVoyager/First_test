# DeepSeek AI Chat - Console Application
A simple Java-based console chat application that connects to the **DeepSeek API**.  
It supports custom system prompts, real-time token tracking, and balance checking.
It supports
## Features
##
- Interactive chat with DeepSeek's chat model (`deepseek-chat`)
- Customizable **system prompt** (set your own personality)
- Token usage statistics (prompt tokens, completion tokens)
- Balance inquiry (`balance` command)
- Conversation history management (`clear` command)
- Simple exit (`exit` command)
- Simple
## Prerequisites
## Pr
- **Java 17** or later
- **Maven 3.8+** (for building)
- A valid **DeepSeek API Key** (get one at [platform.deepseek.com](https://platform.deepseek.com/api_keys))
- A valid
## Quick Start
## Quick Start
### 1. Clone the repository
### 1. Clone the repository

git clone https://github.com/HometownVoyager/First_test.git
cd First_test

```

### 2. Build the project

```
mvn clean packagem
```

This will create an executable JAR with all dependencies inside the `target/` folder.

### 3. Run the application

```
java -jar target/deepseek-chat-app-1.0.0-jar-with-dependencies.jarjava -
```

Or, if you prefer to compile and run directly:

```
mvn clean compile exec:java -Dexec.mainClass="com.deepseek.chat.DeepSeekChat"
```

### 4. Follow the prompts

- Enter your **DeepSeek API Key**.
- Optionally enter a custom system prompt (e.g., "You are a Shakespearean poet").
- Start chatting!

## Commands

| Command | Description |
|---|---|
| `exit` | Quit the chat application |
| `balance` | Check your current DeepSeek account balance |
| `clear` | Clear the current conversation history |

## How It Works

1. The app sends user input along with conversation history and a system prompt to the DeepSeek Chat Completions API.
1. It parses the response and prints the assistant's reply.
1. After each turn, it displays token consumption and an estimated cost.
1. You can check your real balance at any time by typing `balance`.

## Project Structure

```
First_test/First_test
├── pom.xml
├── src/
│       └── java/
│           └── com/
│               └── deepseek/
│                   └── chat/
│                       └── DeepSeekChat.java
├── README.md
└── .gitignore
└── .
```

## Dependencies

- [OkHttp 4.12.0](https://square.github.io/okhttp/) - HTTP client
- [Gson 2.11.0](https://github.com/google/gson) - JSON serialization/deserialization

## Notes

- API Key is never stored; you'll need to enter it each time you start the app.
- Token costs are estimated based on DeepSeek's public pricing.
- Balance is fetched from the DeepSeek API user endpoint.

## License

MIT - feel free to use and modify.