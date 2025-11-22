package com.humberto.botEstoque.Pck_service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraService {

    /**
     * Implementa a fórmula contábil do CMV (Custo da Mercadoria Vendida) real.
     * CMV = Estoque Inicial + Compras do Período - Estoque Final
     *
     * @param estoqueInicial Valor total do estoque no início do período.
     * @param comprasPeriodo Valor total das compras (mercadorias) feitas no período.
     * @param estoqueFinal Valor total do estoque no final do período.
     * @return O valor do CMV.
     */
    public double calcularCmvReal(double estoqueInicial, double comprasPeriodo, double estoqueFinal) {
        // CMV = EI + C - EF
        return estoqueInicial + comprasPeriodo - estoqueFinal;
    }

    /**
     * Calcula o percentual de CMV em relação ao Faturamento Bruto.
     * CMV % = (CMV / Faturamento) * 100
     *
     * @param cmv Valor do Custo da Mercadoria Vendida.
     * @param faturamento Valor total das vendas (Faturamento Bruto) no período.
     * @return O CMV em percentual.
     */
    public double calcularCmvPercentual(double cmv, double faturamento) {
        if (faturamento <= 0) {
            return 0.0;
        }
        return (cmv / faturamento) * 100.0;
    }

    /**
     * Implementa o indicador de acompanhamento (proxy do CMV).
     * CMV Acompanhamento % = (Compras do Período / Faturamento) * 100
     *
     * @param comprasPeriodo Valor total das compras (mercadorias) feitas no período.
     * @param faturamento Valor total das vendas (Faturamento Bruto) no período.
     * @return O indicador de acompanhamento em percentual.
     */
    public double calcularCmvAcompanhamento(double comprasPeriodo, double faturamento) {
        if (faturamento <= 0) {
            return 0.0;
        }
        return (comprasPeriodo / faturamento) * 100.0;
    }

    /**
     * Calcula a projeção linear do faturamento para o total de dias úteis (ou dias do mês).
     * Projeção = (Faturamento Real / Dias Trabalhados) * Dias Totais
     *
     * @param faturamento Valor total do faturamento até o momento.
     * @param diasTrabalhados Número de dias que se passaram no período (ou até o momento da projeção).
     * @param diasUteisTotal Número total de dias úteis no período (ou dias totais do mês).
     * @return O valor de faturamento projetado.
     */
    public double calcularProjecaoFaturamento(double faturamento, int diasTrabalhados, int diasUteisTotal) {
        if (diasTrabalhados <= 0 || diasUteisTotal <= 0) {
            return 0.0;
        }
        // Calcula a média diária e projeta para o total de dias úteis
        double mediaDiaria = faturamento / diasTrabalhados;
        return mediaDiaria * diasUteisTotal;
    }
}