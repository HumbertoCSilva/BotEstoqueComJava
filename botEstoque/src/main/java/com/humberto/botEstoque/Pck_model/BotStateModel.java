package com.humberto.botEstoque.Pck_model;

// Este é o único tipo público de nível superior neste arquivo
public enum BotStateModel {
    IDLE,
    CADASTRO_INICIAL,
    CADASTRO_CODIGO,
    CADASTRO_DESCRICAO,
    CADASTRO_UNIDADE,
    CADASTRO_QUANTIDADE,
    CADASTRO_CUSTO,
    CADASTRO_VALOR_VENDA,
    CMV_REAL_INICIAL,
    CMV_REAL_FMT,
    CMV_REAL_COMPRAS,
    CMV_REAL_ESTOQUE_FINAL,
    CMV_REAL_ESTOQUE_INICIAL,
    CONTAGEM_INICIAL,
    CONTAGEM_QUANTIDADE,
    ATUALIZAR_INICIAL,  // 1. Espera a descrição do produto a atualizar
    ATUALIZAR_VALORES,  // 2. Espera a string com os três novos valores
    DELETAR_INICIAL,             // Novo: Espera a descrição do produto a deletar
    DELETAR_CONFIRMACAO_PENDENTE, // Novo: Espera o clique do botão (CallbackQuery)
    PROJECAO_INICIAL,
    PROJECAO_FATURAMENTO,
    PROJECAO_DIAS_TRABALHADOS,
    PROJECAO_DIAS_TOTAIS
}