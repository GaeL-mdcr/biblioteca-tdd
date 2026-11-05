/**
 * Geração e envio do código de verificação de 6 dígitos.
 *
 * <p>Aqui vão ficar as interfaces {@code GeradorCodigo} e {@code CanalEnvio}, o
 * {@code GeradorCodigoAleatorio} (com {@code SecureRandom}) e o
 * {@code EmailCanalEnvio} (Jakarta Mail). O envio real de e-mail fica fora do TDD
 * de unidade: nos testes, um canal falso só guarda a mensagem.
 */
package br.edu.ifmt.biblioteca.verificacao;
