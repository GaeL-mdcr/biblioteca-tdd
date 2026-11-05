# Diário de bordo

Registro das decisões, do estado atual e dos próximos passos. Os requisitos completos estão em
[`requisitos.md`](requisitos.md), e a organização do código em [`arquitetura.md`](arquitetura.md).

## Regras do projeto

- Ciclo **RED → GREEN → REFACTOR** para todo método, com um commit por etapa do ciclo.
- Nenhum código de produção antes do teste que o pede.
- Tudo em português: código, testes, mensagens de commit e documentação.
- Commits só no nome de Gabriel Mallezan da Costa Ribeiro, sem linha de coautoria.
- Nomes de teste no padrão `deve[Acao]Quando[Condicao]`.

## Decisões

| Data | Decisão |
| --- | --- |
| 04/11/2026 | Contexto: Sistema de Biblioteca, trabalho individual, Java 17 com Swing |
| 04/11/2026 | Primeiro exemplar de cada livro é só para consulta local; livro com exemplar único não sai |
| 04/11/2026 | Usuário nunca é excluído; em caso grave, é banido para sempre |
| 04/11/2026 | Multa de R$ 1,00 por dia com teto de R$ 50,00; suspensão de 2 dias por dia de atraso |
| 04/11/2026 | Renovação (até 2 vezes, em dia) e perda de livro entram no escopo |
| 04/11/2026 | Dados em SQLite, com repositórios em memória nos testes de unidade |
| 04/11/2026 | Sem senha: usuário com nome, CPF e e-mail; código por e-mail confirma empréstimo, renovação e perda; administrador entra com CPF e código |
| 04/11/2026 | WhatsApp descartado: o cadastro não guarda telefone |
| 04/11/2026 | Sem tipos de usuário: todos pegam até 3 exemplares por 7 dias |
| 04/11/2026 | Cadastro de livro em etapas; o livro só circula depois de liberado |
| 04/11/2026 | Trilha de auditoria só de inclusão, com triggers no SQLite |
| 05/11/2026 | Separação em fase 1 (71 métodos, compromisso) e fase 2 (só se sobrar tempo) |
| 05/11/2026 | Etapa 2: repositório público, Maven com JUnit 5, Surefire, Failsafe e JaCoCo, GitHub Actions |
| 05/11/2026 | Desenvolvimento no VS Code (Extension Pack for Java); Maven Wrapper 3.3.4 para não precisar instalar o Maven |
| 05/11/2026 | Arquitetura em camadas com portas e adaptadores; receita única para os métodos de serviço; fakes em vez de Mockito; `ProvedorDeOperador` e `Transacao` como interfaces; `EmprestimoService` usa os repositórios direto |

## Estado atual

- Etapa 1 (requisitos): feita, em `requisitos.md`.
- Etapa 2 (configuração): estrutura vazia criada, sem código de produção nem testes.
- Arquitetura e princípios decididos, em `arquitetura.md`.

## Próximos passos

Etapa 3, seguindo o plano em [`plano-etapa-3.md`](plano-etapa-3.md), na ordem da seção 12 dos requisitos:

1. `Cpf`, `Isbn`, `CalculadoraMulta`, `CalculadoraSuspensao`
2. `Livro` e `Exemplar`
3. `Usuario` e `Administrador`
4. `Emprestimo`
5. `CodigoVerificacao` e `GeradorCodigoAleatorio`
6. `Sessao`, `RegistroAuditoria`, `AuditoriaService`, `VerificacaoService`, `AcessoService`
7. `UsuarioService`, `AcervoService`, `EmprestimoService`
8. Repositórios SQLite e canal de e-mail
9. Interface Swing
