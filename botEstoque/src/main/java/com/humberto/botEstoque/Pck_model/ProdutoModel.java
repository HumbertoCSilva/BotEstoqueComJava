package com.humberto.botEstoque.Pck_model;

import jakarta.persistence.*;
import lombok.Data; // lombok para getters, setters
import java.text.Normalizer;

@Entity // marca a classe como entidade para o JPA
@Table(name="produtos") // define o nome da tabela
@Data // gera os getters, setters, toString, equals e hashcode
public class ProdutoModel {
    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AutoIncrement
    private long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String unidadeMedida;

    @Column(nullable = false)
    private double quantidade; // Estoque atual

    @Column(nullable = false)
    private double custo; // Custo unitário

    private double valorVenda; // Valor de venda unitário

    // Campo de apoio para buscas sem acento/case-insensitive
    @Column(nullable = false)
    private String descricaoNormalizada;

    // Métodos de calculo (lógica de Negócio)

    /*
    * Calcula o valor total em estoque ( (Quantidade * Custo )
    * */
    public double getValorEmEstoque() {
        return this.quantidade * this.custo;
    }

    /*
    * Calcula o CMV do item em % (Custo / valor de venda * 100)
    * */
    public double getCmvPercentual () {
        if (this.valorVenda > 0) {
            return (this.custo / this.quantidade) * 100.0f;
        }
        return 0.0f;
    }


    /*
    * Noemaliza a descrição do produto, removendo acentos, convertendo para
    * minusculas e define a descriçãoNormalizada antes de persistir
    **/
    //@PrePersist // Executa antes de persistir
    //@PreUpdate // Executa antes de atualizar
    //private void normalizeDescription(){
    //    if(this.descricao != null){
    //        String temp = Normalizer.normalize(this.descricao, Normalizer.Form.NFD).replaceAll("[^\\p{InCombinigDiacriticalMarks}]", "").toLowerCase();
    //
    //        this.descricaoNormalizada = temp;
    //   }
    //}

}