# ☕ Bot de Controle de Estoque para Cafeteria

Este é o projeto de um bot de Telegram desenvolvido em Java com Spring Boot, projetado para fornecer uma solução robusta e de fácil uso para a gestão de estoque e análise de custos em tempo real para uma cafeteria.

---

## 🛠️ Stack Tecnológica

O sistema foi construído sobre uma arquitetura moderna e escalável:

* **Linguagem:** **Java 17+**
* **Framework:** **Spring Boot 3.2.0** (Componentes `@Component`, `@Service`, `@Repository`)
* **API do Bot:** **`telegrambots`** (Com tratamento de `CallbackQuery` para botões)
* **Banco de Dados:** **MySQL** (Persistência via Spring Data JPA/Hibernate)
* **Sistema de Build:** **Maven** (`pom.xml` configurado para resolver conflitos de JAXB/Java 17)
* **Arquitetura:** Camadas (Controller-Service-Repository) com gerenciamento de **estado de conversa**.

---

## 📝 Funcionalidades Implementadas (Comandos Ativos)

As seguintes funcionalidades principais foram implementadas e estão ativas na classe `CafeteriaBotController.java`:

### 1. Gestão de Produtos (CRUD e Listagem)

| Comando | Fluxo de Dados | Descrição |
| :--- | :--- | :--- |
| **`/start`** | Mensagem Única | Inicia o bot e exibe o menu completo de comandos. |
| **`/menu`** | Mensagem Única | Exibe o menu completo de comandos. |
| **`/cadastrar`** | Multi-passo (6 etapas) | Inicia o cadastro de um novo item, coletando **código, descrição, unidade, quantidade, custo** e **valor de venda**. |
| **`/consultar`** | Descrição + Resposta | Busca um produto e exibe todos os detalhes (estoque, custo, venda, CMV do item). |
| **`/atualizar`** | Multi-passo (2 etapas) | Modifica **Quantidade, Custo e/ou Valor de Venda** em um único passo, usando `0` ou `-` para pular campos. |
| **`/deletar`** | Descrição + Confirmação | Inicia a exclusão de um item, exigindo que o usuário confirme via **botões Inline Keyboard** para segurança. |
| **`/listar`** | Mensagem Única | Lista todos os produtos no estoque, ordenados por **código**. |

### 2. Relatórios e Inventário

| Comando | Objetivo | Detalhes |
| :--- | :--- | :--- |
| **`/estoque`** | Valor Total de Inventário | Retorna o **Valor Total** (em custo) do estoque da cafeteria. |
| **`/contagem`** | Multi-passo | Inicia o fluxo para ajustar a quantidade atual de um produto no inventário. |

---

## 💾 Detalhes da Persistência e Auditoria

### Arquitetura JPA

A Entidade principal é **`ProdutoModel.java`**, mapeada para a tabela `produtos`.

* **Busca Flexível:** O campo `descricaoNormalizada` (calculado no Service) permite buscas insensíveis a maiúsculas, minúsculas e acentuação.
* **Cálculos no Model:** A Entidade fornece automaticamente os métodos `getValorEmEstoque()` e `getCmvPercentual()`.

### Trigger de Auditoria de Exclusão (MySQL)

Para rastrear todas as exclusões, foi criada uma Trigger de auditoria:

* **Trigger:** `trg_produtos_before_delete`
* **Ação:** Executada **ANTES** de qualquer `DELETE` na tabela `produtos`.
* **Função:** Copia os dados completos da linha que será excluída (usando o comando **`OLD`**) para a tabela **`produtos_excluidos`**, juntamente com a data e hora exata da exclusão (`NOW()`).


* testar projeção - projeção implementada 24/11
---

## 🚀 Próximos Passos (Próximos Comandos)

O sistema está estruturado para receber as funcionalidades financeiras e de movimentação complexa:

1.  **Relatórios Financeiros:** Implementar os fluxos `/cmvReal` e `/projecao` usando o `CalculadoraService`.
2.  **Movimentações Não Vendidas:** Implementar os fluxos `/desperdicio` e `/consumo` de funcionários.
3.  **Venda por Receita:** Criar lógica (Entidade `Receita`) para que a venda de um produto composto (ex: Cappuccino) dê baixa automática em múltiplos ingredientes.

Proximas implementações e testes:
* Testar contagem - deve ser de uma maneira simples : escreve a descrição e a quantidade ou pede um listar e vai alterando a quantidade
* implementar controle de desperdício - informa o item, o motivo e a quantidade - listar para ver o desperdício
* consumo - informa o funcionario, a data, oq ue foi consumido, listar para ver consumo por funcionário ou total
* testar calculo de cmv


