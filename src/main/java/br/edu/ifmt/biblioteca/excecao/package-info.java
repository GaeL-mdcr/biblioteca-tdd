/**
 * Exceções de regra de negócio. Todas vão herdar de {@code BibliotecaException}
 * ({@code RuntimeException}), para a tela tratar num só {@code catch}.
 *
 * <p>Dados inválidos usam {@code IllegalArgumentException} e operações fora de hora
 * usam {@code IllegalStateException}.
 */
package br.edu.ifmt.biblioteca.excecao;
