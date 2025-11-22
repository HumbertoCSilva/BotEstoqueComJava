package com.humberto.botEstoque;

import com.humberto.botEstoque.Pck_controller.CafeteriaBotController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class BotEstoqueApplication {

	public static void main(String[] args) {
		// Inicia o ambiente Spring Boot e armazena o contexto
		SpringApplication.run(BotEstoqueApplication.class, args);
	}

	/**
	 * Define um Bean que inicializa a API de bots do Telegram.
	 * * Ele busca a instância do nosso CafeteriaBot do contexto Spring e o registra
	 * para começar a escutar as mensagens (Long Polling).
	 * * @param context O contexto de aplicação do Spring, que gerencia todas as classes (@Service, @Controller, etc.)
	 * @return O objeto TelegramBotsApi inicializado
	 */
	@Bean
	public TelegramBotsApi telegramBotsApi(ApplicationContext context) throws TelegramApiException {
		// Inicializa o ambiente da API
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

		// Busca a instância do nosso Bot no contexto Spring e o registra
		// NOTA: O Spring fará a injeção de dependência necessária no CafeteriaBot
		botsApi.registerBot(context.getBean(CafeteriaBotController.class));

		return botsApi;
	}
}