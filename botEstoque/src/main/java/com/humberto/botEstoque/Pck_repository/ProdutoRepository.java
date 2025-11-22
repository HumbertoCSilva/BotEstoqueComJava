package com.humberto.botEstoque.Pck_repository;

import com.humberto.botEstoque.Pck_model.ProdutoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

// A CORREÇÃO ESTÁ AQUI:
// O primeiro parâmetro deve ser a classe da sua Entidade: ProdutoModel
// O segundo parâmetro é o tipo da Chave Primária (Long)
public interface ProdutoRepository extends JpaRepository<ProdutoModel, Long> {

    // Se você estava usando: JpaRepository<ProdutoRepositoty, Long>, o erro acontece.

    // Métodos personalizados (devem usar ProdutoModel)
    Optional<ProdutoModel> findByDescricaoNormalizada(String descricaoNormalizada);
    Optional<ProdutoModel> findByCodigo(String codigo);
    // NOVO MÉTODO: Encontra todos os produtos ordenados pelo código
    List<ProdutoModel> findAllByOrderByCodigoAsc();
}