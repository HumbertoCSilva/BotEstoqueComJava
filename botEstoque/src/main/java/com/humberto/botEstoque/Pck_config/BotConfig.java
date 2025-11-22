package com.humberto.botEstoque.Pck_config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

// Anotação que marca a classe como fonte de definições de beans Spring
@Configuration
public class BotConfig {

    // Injeta o valor do token do application.properties
    @Value("${telegram.bot.token}")
    private String botToken;

    // Injeta o valor do username do application.properties
    @Value("${telegram.bot.username}")
    private String botUsername;

    // Métodos para acesso seguro (getters)

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }
}