package com.humberto.botEstoque.Pck_service;

import com.humberto.botEstoque.Pck_model.ProdutoModel;
import com.humberto.botEstoque.Pck_repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final CalculadoraService calculadoraService;

    // Injeção de Dependências
    public EstoqueService(ProdutoRepository produtoRepository, CalculadoraService calculadoraService) {
        this.produtoRepository = produtoRepository;
        this.calculadoraService = calculadoraService;
    }

    // método que retorna a lista de produtos do estoque
    public List<ProdutoModel> listarEstoqueOrdenadoPorCodigo() {
        return produtoRepository.findAllByOrderByCodigoAsc();
    }


    // --- MÉTODOS DE CADASTRO E CONSULTA (CRUD) ---

    @Transactional
    public ProdutoModel cadastrarProduto(ProdutoModel novoProduto) {
        // CORREÇÃO CRÍTICA: Normalização manual para evitar o erro NOT NULL
        String descricaoNormalizada = normalizarString(novoProduto.getDescricao());
        novoProduto.setDescricaoNormalizada(descricaoNormalizada);

        return produtoRepository.save(novoProduto);
    }

    public Optional<ProdutoModel> consultarProdutoPorDescricao(String descricao) {
        // Normaliza a string de busca do usuário
        String descricaoNormalizada = normalizarString(descricao);
        return produtoRepository.findByDescricaoNormalizada(descricaoNormalizada);
    }

    // --- MÉTODOS DE ATUALIZAÇÃO E MOVIMENTAÇÃO DE ESTOQUE (/atualizar, /contagem) ---

    @Transactional
    public Optional<ProdutoModel> atualizarInformacoes(
            String descricao, Double novaQuantidade, Double novoCusto, Double novoValorVenda) {

        return consultarProdutoPorDescricao(descricao).map(produto -> {

            if (novaQuantidade != null) {
                produto.setQuantidade(novaQuantidade);
            }
            if (novoCusto != null) {
                produto.setCusto(novoCusto);
            }
            if (novoValorVenda != null) {
                produto.setValorVenda(novoValorVenda);
            }

            // Re-salva o produto, atualizando implicitamente ValorEmEstoque e CMV%
            return produtoRepository.save(produto);
        });
    }

    @Transactional
    public boolean deletarProduto(String descricao) {
        Optional<ProdutoModel> produtoOpt = consultarProdutoPorDescricao(descricao);
        if (produtoOpt.isPresent()) {
            produtoRepository.delete(produtoOpt.get());
            return true;
        }
        return false;
    }

    // --- MÉTODOS DE RELATÓRIO DE ESTOQUE (/estoque) ---

    public double calcularEstoqueTotal() {
        List<ProdutoModel> todosProdutos = produtoRepository.findAll();
        // Soma o valor de estoque (custo) de todos os produtos
        return todosProdutos.stream()
                .mapToDouble(ProdutoModel::getValorEmEstoque)
                .sum();
    }

    // --- MÉTODOS DE RELATÓRIO FINANCEIRO (Usando CalculadoraService) ---

    public double realizarCmvReal(double estoqueInicial, double faturamento, double comprasPeriodo, double estoqueFinal) {
        double cmvValor = calculadoraService.calcularCmvReal(estoqueInicial, comprasPeriodo, estoqueFinal);
        return calculadoraService.calcularCmvPercentual(cmvValor, faturamento);
    }

    public double realizarCmvAcompanhamento(double comprasPeriodo, double faturamento) {
        return calculadoraService.calcularCmvAcompanhamento(comprasPeriodo, faturamento);
    }

    public double realizarProjecaoFaturamento(double faturamento, int diasTrabalhados, int diasUteisTotal) {
        return calculadoraService.calcularProjecaoFaturamento(faturamento, diasTrabalhados, diasUteisTotal);
    }

    // --- MÉTODO AUXILIAR PRIVADO (ESSENCIAL) ---

    /**
     * Remove acentos e converte para minúsculas. Usado para normalizar strings de busca e persistência.
     */
    private String normalizarString(String input) {
        if (input == null) return null;
        // Normaliza para formato NFD (separando acentos) e remove caracteres de acento
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
    }
}