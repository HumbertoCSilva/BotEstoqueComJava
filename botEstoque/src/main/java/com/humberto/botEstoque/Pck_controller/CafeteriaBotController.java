package com.humberto.botEstoque.Pck_controller;

import com.humberto.botEstoque.Pck_config.BotConfig;
import com.humberto.botEstoque.Pck_model.BotStateModel;
import com.humberto.botEstoque.Pck_model.ProdutoModel;
import com.humberto.botEstoque.Pck_model.UsuarioStateModel;
import com.humberto.botEstoque.Pck_service.EstoqueService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CafeteriaBotController extends TelegramLongPollingBot {

    // Gerenciamento de Estado de Conversação (em memória)
    private final Map<Long, UsuarioStateModel> userStates = new HashMap<>();

    // Injeção de Dependências
    private final EstoqueService estoqueService;
    private final BotConfig botConfig;

    public CafeteriaBotController(EstoqueService estoqueService, BotConfig botConfig) {
        this.estoqueService = estoqueService;
        this.botConfig = botConfig;
    }

    // --- MÉTODOS OBRIGATÓRIOS DA API DO TELEGRAM ---

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    // --- MÉTODO PRINCIPAL DE RECEBIMENTO DE MENSAGENS ---

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            UsuarioStateModel state = getOrCreateState(chatId);

            if (messageText.startsWith("/")) {
                processCommand(chatId, messageText, state);
            } else {
                processStepInput(chatId, messageText, state);
            }
        } else if (update.hasCallbackQuery()) { // TRATAMENTO DE CLIQUE DE BOTÃO
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    // --- TRATAMENTO DE CLIQUE EM BOTÃO (CallbackQuery) ---

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        UsuarioStateModel state = getOrCreateState(chatId);

        if (state.getEstadoAtual() == BotStateModel.DELETAR_CONFIRMACAO_PENDENTE) {

            // A descrição foi salva como String no dadosTemp
            String descricaoDeletar = (String) state.getDadosTemp().get("DescricaoDeletar");

            if (data.equals("DELETE_CONFIRM") && descricaoDeletar != null) {

                boolean sucesso = estoqueService.deletarProduto(descricaoDeletar);

                if (sucesso) {
                    editMessage(chatId, callbackQuery.getMessage().getMessageId(),
                            "✅ Produto **" + descricaoDeletar + "** deletado com sucesso.");
                } else {
                    editMessage(chatId, callbackQuery.getMessage().getMessageId(),
                            "❌ Falha ao deletar: Produto não encontrado ou já deletado.");
                }
            } else {
                editMessage(chatId, callbackQuery.getMessage().getMessageId(), "ℹ️ Operação de exclusão cancelada.");
            }

            state.setEstadoAtual(BotStateModel.IDLE);
            state.getDadosTemp().clear();
        }
    }


    // --- MÉTODOS DE LÓGICA DE CONTROLE ---

    private UsuarioStateModel getOrCreateState(Long chatId) {
        return userStates.computeIfAbsent(chatId, UsuarioStateModel::new);
    }

    private void processCommand(long chatId, String command, UsuarioStateModel state) {
        state.setEstadoAtual(BotStateModel.IDLE);
        state.setProdutoTemp(new ProdutoModel());
        state.getDadosTemp().clear();

        switch (command.split(" ")[0]) {
            case "/start":
            case "/menu":
                handleMenuCommand(chatId);
                break;
            case "/cadastrar":
                state.setEstadoAtual(BotStateModel.CADASTRO_CODIGO);
                sendMessage(chatId, "Iniciando cadastro. Por favor, envie o **código** do produto.");
                break;
            case "/consultar":
                state.setEstadoAtual(BotStateModel.CONTAGEM_INICIAL);
                sendMessage(chatId, "Qual produto deseja **consultar**? Envie a descrição.");
                break;
            case "/estoque":
                handleEstoqueTotal(chatId);
                break;
            case "/contagem":
                state.setEstadoAtual(BotStateModel.CONTAGEM_INICIAL);
                sendMessage(chatId, "Para **Contagem**, envie a **descrição** do produto que terá a quantidade atualizada.");
                break;
            case "/atualizar":
                state.setEstadoAtual(BotStateModel.ATUALIZAR_INICIAL);
                sendMessage(chatId, "Iniciando atualização. Por favor, envie a **descrição** do produto que deseja modificar.");
                break;
            case "/deletar":
                state.setEstadoAtual(BotStateModel.DELETAR_INICIAL);
                sendMessage(chatId, "⚠️ **DELETAR PRODUTO**\nPor favor, envie a **descrição** do produto que você deseja apagar.");
                break;
            case "/listar": // COMANDO NOVO
                handleListarEstoque(chatId);
                break;
            // TODO: Adicionar cases para /cmvReal, etc.
            default:
                sendMessage(chatId, "Comando desconhecido. Por favor, digite **/menu** para ver a lista de comandos.");
        }
    }

    private void processStepInput(long chatId, String text, UsuarioStateModel state) {
        switch (state.getEstadoAtual()) {
            case CADASTRO_CODIGO:
                state.getProdutoTemp().setCodigo(text.toUpperCase());
                state.setEstadoAtual(BotStateModel.CADASTRO_DESCRICAO);
                sendMessage(chatId, "Ok. Agora envie a **descrição** do produto (Ex: Café Grão).");
                break;

            case CADASTRO_DESCRICAO:
                state.getProdutoTemp().setDescricao(text);
                state.setEstadoAtual(BotStateModel.CADASTRO_UNIDADE);
                sendMessage(chatId, "Agora envie a **unidade de medida** (Ex: kg, L, un).");
                break;

            case CADASTRO_UNIDADE:
                state.getProdutoTemp().setUnidadeMedida(text);
                state.setEstadoAtual(BotStateModel.CADASTRO_QUANTIDADE);
                sendMessage(chatId, "Qual a **quantidade** inicial em estoque? (Apenas números)");
                break;

            case CADASTRO_QUANTIDADE:
                try {
                    double quantidade = Double.parseDouble(text.replace(",", "."));
                    state.getProdutoTemp().setQuantidade(quantidade);
                    state.setEstadoAtual(BotStateModel.CADASTRO_CUSTO);
                    sendMessage(chatId, "Qual o **custo unitário** deste produto? (Apenas números)");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Erro: A quantidade deve ser um número. Tente novamente.");
                }
                break;

            case CADASTRO_CUSTO:
                try {
                    double custo = Double.parseDouble(text.replace(",", "."));
                    state.getProdutoTemp().setCusto(custo);
                    state.setEstadoAtual(BotStateModel.CADASTRO_VALOR_VENDA);
                    sendMessage(chatId, "Qual o **valor de venda** unitário? (Apenas números. Digite 0 se não for para venda).");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Erro: O custo deve ser um número. Tente novamente.");
                }
                break;

            case CADASTRO_VALOR_VENDA:
                try {
                    double valorVenda = Double.parseDouble(text.replace(",", "."));
                    state.getProdutoTemp().setValorVenda(valorVenda);
                    handleFinalizarCadastro(chatId, state);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Erro: O valor de venda deve ser um número. Tente novamente.");
                }
                break;

            case CONTAGEM_INICIAL: // Usado para /consultar e para o primeiro passo do /contagem
                String descricaoBuscaContagem = text;
                Optional<ProdutoModel> produtoOptContagem = estoqueService.consultarProdutoPorDescricao(descricaoBuscaContagem);

                if (produtoOptContagem.isPresent()) {
                    ProdutoModel produto = produtoOptContagem.get();
                    if (state.getProdutoTemp().getCodigo() == null) { // Caso /consultar
                        handleConsultaProduto(chatId, produto);
                        state.setEstadoAtual(BotStateModel.IDLE);
                    } else { // Caso /contagem
                        state.getDadosTemp().put("ProdutoID", (double) produto.getId());
                        state.setEstadoAtual(BotStateModel.CONTAGEM_QUANTIDADE);
                        sendMessage(chatId, "Produto encontrado: **" + produto.getDescricao() + "**.\nQual é a **nova quantidade** em estoque?");
                    }
                } else {
                    sendMessage(chatId, "Produto não encontrado com a descrição: " + descricaoBuscaContagem);
                    state.setEstadoAtual(BotStateModel.IDLE);
                }
                break;

            case CONTAGEM_QUANTIDADE:
                // Lógica de contagem
                // TODO: Chamar estoqueService.atualizarInformacoes
                sendMessage(chatId, "Lógica de contagem em desenvolvimento.");
                state.setEstadoAtual(BotStateModel.IDLE);
                break;

            case ATUALIZAR_INICIAL: // PASSO 1: Receber Descrição
                String descricaoBuscaAtualizar = text;
                Optional<ProdutoModel> produtoOptAtualizar = estoqueService.consultarProdutoPorDescricao(descricaoBuscaAtualizar);

                if (produtoOptAtualizar.isPresent()) {
                    state.getDadosTemp().put("DescricaoAtualizar", descricaoBuscaAtualizar.toUpperCase());

                    state.setEstadoAtual(BotStateModel.ATUALIZAR_VALORES);

                    sendMessage(chatId,
                            "Produto encontrado: **" + produtoOptAtualizar.get().getDescricao() + "**.\n\n" +
                                    "✏️ **Atualize os valores** abaixo. *Use vírgulas para separar os campos.*\n\n" +
                                    "**Ordem Fixa:** [1] Quantidade, [2] Custo Unitário, [3] Valor de Venda\n\n" +
                                    "➡️ **Regra:** Digite **0** (zero) ou **-** (hífen) para pular um campo que não será alterado.\n\n" +
                                    "Exemplos:\n" +
                                    "Mudar só a Quantidade: `20, 0, 0`\n" +
                                    "Mudar só o Custo: `0, 1.50, 0`\n" +
                                    "Mudar os três: `10.5, 4.5, 6.0`"
                    );

                } else {
                    sendMessage(chatId, "❌ Produto não encontrado. Por favor, digite a descrição correta.");
                    state.setEstadoAtual(BotStateModel.IDLE);
                }
                break;

            case ATUALIZAR_VALORES: // PASSO 2: Receber e Processar 3 valores
                try {
                    String[] partes = text.split(",");
                    if (partes.length != 3) {
                        sendMessage(chatId, "❌ Formato incorreto. Envie exatamente 3 valores separados por vírgula (Ex: 10.5, 4.5, 6.0).");
                        return;
                    }

                    Double novaQuantidade = parseUpdateValue(partes[0].trim());
                    Double novoCusto = parseUpdateValue(partes[1].trim());
                    Double novoValorVenda = parseUpdateValue(partes[2].trim());

                    String descricaoOriginal = (String) state.getDadosTemp().get("DescricaoAtualizar");

                    Optional<ProdutoModel> produtoAtualizado = estoqueService.atualizarInformacoes(
                            descricaoOriginal, novaQuantidade, novoCusto, novoValorVenda);

                    if (produtoAtualizado.isPresent()) {
                        handleConsultaProduto(chatId, produtoAtualizado.get());
                        sendMessage(chatId, "✅ Informações atualizadas com sucesso!");
                    } else {
                        sendMessage(chatId, "❌ Erro ao atualizar. O produto não foi encontrado no banco de dados.");
                    }

                } catch (NumberFormatException e) {
                    sendMessage(chatId, "❌ Erro de formato. Certifique-se de que todos os valores são números válidos (use ponto para decimais).");
                } catch (Exception e) {
                    sendMessage(chatId, "❌ Erro inesperado ao processar a atualização. Tente novamente.");
                } finally {
                    state.setEstadoAtual(BotStateModel.IDLE);
                    state.getDadosTemp().clear();
                }
                break;

            case DELETAR_INICIAL: // PASSO 1 DELETAR
                String descricaoBuscaDeletar = text;
                Optional<ProdutoModel> produtoOptDeletar = estoqueService.consultarProdutoPorDescricao(descricaoBuscaDeletar);

                if (produtoOptDeletar.isPresent()) {
                    ProdutoModel produto = produtoOptDeletar.get();

                    // Salva a descrição (String) para usar no Callback
                    state.getDadosTemp().put("DescricaoDeletar", produto.getDescricao().toUpperCase());
                    state.setEstadoAtual(BotStateModel.DELETAR_CONFIRMACAO_PENDENTE);

                    sendDeleteConfirmation(chatId, produto.getDescricao());

                } else {
                    sendMessage(chatId, "❌ Produto não encontrado com a descrição: " + descricaoBuscaDeletar);
                    state.setEstadoAtual(BotStateModel.IDLE);
                }
                break;

            case DELETAR_CONFIRMACAO_PENDENTE:
                sendMessage(chatId, "⚠️ Por favor, use os botões **SIM** ou **CANCELAR**.");
                break;

            case IDLE:
                sendMessage(chatId, "Comando não reconhecido. Use **/menu** para ver a lista de comandos.");
                break;
        }
    }

    // --- NOVO HANDLER: LISTAGEM DE ESTOQUE COMPLETO ---

    private void handleListarEstoque(long chatId) {
        List<ProdutoModel> produtos = estoqueService.listarEstoqueOrdenadoPorCodigo();

        if (produtos.isEmpty()) {
            sendMessage(chatId, "ℹ️ Seu estoque está vazio. Use /cadastrar para adicionar um novo produto.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 **ESTOQUE COMPLETO (Por Código)**\n");
        sb.append("--------------------------------------------------\n");

        for (ProdutoModel p : produtos) {
            String linha = String.format("`%s` | %s | %.2f %s (R$ %.2f)\n",
                    p.getCodigo(),
                    p.getDescricao(),
                    p.getQuantidade(),
                    p.getUnidadeMedida(),
                    p.getValorEmEstoque()); // Valor em Custo

            // Verifica o limite de caracteres do Telegram (4096).
            if (sb.length() + linha.length() > 4000) {
                sendMessage(chatId, sb.toString());
                sb = new StringBuilder();
                sb.append("📋 **ESTOQUE COMPLETO (Continuação)**\n");
                sb.append("--------------------------------------------------\n");
            }
            sb.append(linha);
        }

        if (sb.length() > 0) {
            sendMessage(chatId, sb.toString());
        }
    }


    // --- HANDLERS E UTILITÁRIOS ---

    // Método para tratar valores nulos/vazios na atualização
    private Double parseUpdateValue(String input) throws NumberFormatException {
        if (input.isEmpty() || input.equals("0") || input.equals("-")) {
            return null;
        }
        return Double.parseDouble(input.replace(",", "."));
    }

    // MÉTODOS DE BOTÃO (DELETE)
    private void sendDeleteConfirmation(long chatId, String descricaoProduto) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonYes = new InlineKeyboardButton();
        buttonYes.setText("✅ Sim, Deletar");
        buttonYes.setCallbackData("DELETE_CONFIRM");
        InlineKeyboardButton buttonNo = new InlineKeyboardButton();
        buttonNo.setText("❌ Cancelar");
        buttonNo.setCallbackData("DELETE_CANCEL");
        List<InlineKeyboardButton> row1 = List.of(buttonYes, buttonNo);
        markupInline.setKeyboard(List.of(row1));
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("⚠️ **Confirmação:** Tem certeza que deseja deletar permanentemente o produto **" + descricaoProduto + "**?");
        message.setReplyMarkup(markupInline);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Erro ao enviar confirmação: " + e.getMessage());
        }
    }

    private void editMessage(long chatId, int messageId, String newText) {
        EditMessageReplyMarkup editReplyMarkup = new EditMessageReplyMarkup();
        editReplyMarkup.setChatId(String.valueOf(chatId));
        editReplyMarkup.setMessageId(messageId);
        editReplyMarkup.setReplyMarkup(null);
        try {
            execute(editReplyMarkup);
        } catch (TelegramApiException e) {
            System.err.println("Erro ao editar mensagem (remover botões): " + e.getMessage());
        }
        sendMessage(chatId, newText);
    }


    private void handleMenuCommand(long chatId) {
        String menuText =
                "📋 **MENU DE COMANDOS - GESTÃO DE ESTOQUE** 📋\n\n" +
                        "--- *Gestão de Produtos* ---\n" +
                        "/cadastrar - Inicia o cadastro de um novo produto (multi-passo).\n" +
                        "/consultar - Consulta detalhes de um produto pela descrição.\n" +
                        "/atualizar - Atualiza preço de custo ou venda de um produto.\n" +
                        "/deletar   - Deleta um produto do estoque (requer confirmação).\n\n" +
                        "--- *Movimentação e Inventário* ---\n" +
                        "/contagem  - Ajusta a quantidade de um produto após contagem física.\n" +
                        "/listar    - Lista o estoque completo por ordem de código.\n" +
                        "/desperdicio - Registra a saída de itens por desperdício/quebra (futuro).\n" +
                        "/consumo   - Registra a saída de itens consumidos por funcionários (futuro).\n\n" +
                        "--- *Relatórios Financeiros* ---\n" +
                        "/estoque   - Retorna o **Valor Total** do estoque em Reais (R$).\n" +
                        "/cmvReal   - Inicia o cálculo do Custo da Mercadoria Vendida (CMV real).\n" +
                        "/cmvAcompanhamento - Calcula o indicador de compras sobre faturamento.\n" +
                        "/projecao  - Calcula a projeção linear de faturamento para o mês.\n\n" +
                        "Use /menu para exibir este menu novamente.";

        sendMessage(chatId, menuText);
    }

    private void handleFinalizarCadastro(long chatId, UsuarioStateModel state) {
        try {
            ProdutoModel produtoFinal = state.getProdutoTemp();
            estoqueService.cadastrarProduto(produtoFinal);

            String resumo = String.format(
                    "✅ Produto **%s** cadastrado com sucesso!\n" +
                            "  Estoque Atual: %.2f %s\n" +
                            "  Valor em Estoque: R$ %.2f\n" +
                            "  CMV do Item: %.2f%%",
                    produtoFinal.getDescricao(),
                    produtoFinal.getQuantidade(),
                    produtoFinal.getUnidadeMedida(),
                    produtoFinal.getValorEmEstoque(),
                    produtoFinal.getCmvPercentual()
            );
            sendMessage(chatId, resumo);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Erro ao salvar o produto. Verifique os dados ou se o código já existe.");
        } finally {
            state.setEstadoAtual(BotStateModel.IDLE); // Finaliza o fluxo
        }
    }

    private void handleConsultaProduto(long chatId, ProdutoModel produto) {
        String resumo = String.format(
                "🔎 **Detalhes do Produto: %s** (Cód: %s)\n" +
                        "----------------------------------\n" +
                        "Unidade: %s\n" +
                        "Estoque Atual: **%.2f**\n" +
                        "Custo Unitário: R$ %.2f\n" +
                        "Valor de Venda: R$ %.2f\n" +
                        "Valor Total em Estoque: **R$ %.2f**\n" +
                        "CMV do Item: %.2f%%",
                produto.getDescricao(),
                produto.getCodigo(),
                produto.getUnidadeMedida(),
                produto.getQuantidade(),
                produto.getCusto(),
                produto.getValorVenda(),
                produto.getValorEmEstoque(),
                produto.getCmvPercentual()
        );
        sendMessage(chatId, resumo);
    }

    private void handleEstoqueTotal(long chatId) {
        double total = estoqueService.calcularEstoqueTotal();
        String resumo = String.format("💰 O **Valor Total em Estoque** (Custo) da sua cafeteria é de: **R$ %.2f**.", total);
        sendMessage(chatId, resumo);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableMarkdown(true); // Permite usar negrito (**)

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}