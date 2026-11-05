/**
 * Implementação dos repositórios com SQLite (JDBC), gravando no arquivo
 * {@code biblioteca.db}.
 *
 * <p>Coberta por testes de integração (classes terminadas em {@code IT}) que rodam
 * no {@code mvn verify} com banco em memória. A tabela de auditoria só aceita
 * inclusão: triggers bloqueiam {@code UPDATE} e {@code DELETE}.
 */
package br.edu.ifmt.biblioteca.repositorio.sqlite;
