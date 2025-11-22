package com.humberto.botEstoque.Pck_model;
import com.humberto.botEstoque.Pck_model.BotStateModel;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

// ÚNICA classe pública neste arquivo
@Data
public class UsuarioStateModel {

    private Long chatId;

    // Referência ao enum BotState
    private BotStateModel estadoAtual = BotStateModel.IDLE;

    // Armazenamento temporário do produto em rascunho (para /cadastrar)
    private ProdutoModel produtoTemp;

    // Armazenamento temporário para valores soltos (para /cmvReal, /projeção)
    private Map<String, Object> dadosTemp = new HashMap<>();

    public UsuarioStateModel(Long chatId) {
        this.chatId = chatId;
        // Inicializa o produtoTemp para evitar NullPointerException
        this.produtoTemp = new ProdutoModel();
    }
}