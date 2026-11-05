/**
 * Casos de uso que coordenam o domínio: {@code AcessoService}, {@code AuditoriaService},
 * {@code VerificacaoService}, {@code UsuarioService}, {@code AcervoService} e
 * {@code EmprestimoService}.
 *
 * <p>Toda operação que altera dados exige um administrador com sessão aberta e
 * grava um registro na trilha de auditoria. As dependências (repositórios,
 * calculadoras, canal de e-mail, {@code Clock}) entram pelo construtor.
 */
package br.edu.ifmt.biblioteca.servico;
