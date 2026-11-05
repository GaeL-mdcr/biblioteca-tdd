# Biblioteca — Sistema de Biblioteca feito com TDD

Trabalho da disciplina **Teste de Software** (IFMT), atividade prática de Test-Driven Development.

O sistema controla o acervo e a circulação de uma biblioteca escolar. Os livros são cadastrados
**em etapas** e só entram em circulação depois de liberados; cada exemplar tem seu estado de
conservação, e o primeiro exemplar de cada livro fica **só para consulta local**. Há empréstimo,
renovação, devolução e perda, multa e suspensão por atraso e banimento permanente de usuários
(ninguém é excluído).

**Ninguém tem senha.** O administrador entra com o CPF e um código de 6 dígitos que chega no
e-mail dele, e o usuário confirma empréstimo, renovação e perda do mesmo jeito. Tudo o que os
administradores fazem fica numa **trilha de auditoria** que não pode ser alterada.

Todo o código de produção nasce de um teste que falhou antes (ciclo **Red → Green → Refactor**),
e o histórico de commits mostra isso.

> Status: **Etapa 2 — estrutura do projeto.** Ainda não existe código de produção nem testes.

## Tecnologias

- Java 17, com interface em Java Swing
- Maven
- JUnit 5 (Jupiter) para os testes e JaCoCo para a cobertura
- SQLite (`sqlite-jdbc`) para guardar os dados num arquivo local
- Jakarta Mail (Angus Mail) para enviar o código de verificação por e-mail

## Estrutura de pastas

```
biblioteca-tdd/
├── pom.xml                         # dependências e plugins (JUnit 5, Surefire, Failsafe, JaCoCo)
├── src/main/java/br/edu/ifmt/biblioteca/
│   ├── dominio/                    # regras de negócio: Livro, Exemplar, Usuario, Emprestimo, Cpf...
│   ├── verificacao/                # geração e envio do código por e-mail
│   ├── repositorio/                # interfaces de acesso aos dados
│   │   └── sqlite/                 # implementação com SQLite (JDBC) e triggers da auditoria
│   ├── servico/                    # casos de uso: acervo, usuários, empréstimos, acesso, auditoria
│   ├── excecao/                    # BibliotecaException e as exceções de regra de negócio
│   └── ui/                         # telas em Java Swing (fora do TDD de unidade)
├── src/main/resources/
│   └── config.properties.exemplo   # modelo da configuração de e-mail
├── src/test/java/br/edu/ifmt/biblioteca/
│   ├── dominio/                    # um teste por classe de domínio
│   ├── servico/                    # testes dos serviços com repositórios em memória
│   └── repositorio/
│       ├── memoria/                # repositórios em memória usados nos testes de unidade
│       └── sqlite/                 # testes de integração com SQLite em memória (*IT.java)
└── docs/
    ├── requisitos.md               # Etapa 1: classes, métodos, regras de negócio e fases
    └── CONTEXTO.md                 # diário de bordo: decisões, estado atual e próximos passos
```

## Como rodar os testes

Precisa do JDK 17 ou mais novo e do Maven 3.9 (IntelliJ, NetBeans e Eclipse já trazem o Maven
embutido).

```bash
mvn test       # testes de unidade (domínio e serviços)
mvn verify     # unidade + integração com SQLite + relatório de cobertura
```

O relatório de cobertura fica em `target/site/jacoco/index.html`. O GitHub Actions roda o
`mvn verify` a cada push (aba **Actions**).

## Configuração do e-mail

O código de verificação é enviado pelo Gmail. Copie
`src/main/resources/config.properties.exemplo` para `config.properties` na mesma pasta e preencha
o remetente e a senha de app. O `config.properties` está no `.gitignore` e nunca vai para o
GitHub. Os testes não precisam dele: usam um canal de envio falso.

## Como os commits estão organizados

Cada comportamento passa por até três commits, nesta ordem:

| Prefixo | O que entra | Resultado no Actions |
| --- | --- | --- |
| `RED:` | só o teste novo, que falha | ❌ (de propósito) |
| `GREEN:` | o mínimo de código para o teste passar | ✅ |
| `REFACTOR:` | melhoria sem mudar o comportamento, quando houver | ✅ |

Exemplo: `RED: CPF com dígito verificador errado é recusado`.

## Fases

A **fase 1** é o compromisso da entrega (71 métodos). A **fase 2** (reposição física, expiração
da sessão, desativação de administrador, eventos de segurança e hash encadeado na auditoria) só
começa quando a fase 1 inteira estiver verde. Ver a seção 12 de [`docs/requisitos.md`](docs/requisitos.md).

## Resultados

| Métrica | Valor |
| --- | --- |
| Testes | — |
| Cobertura | — |

(Atualizado ao fim de cada semana.)

## Integrantes

- Gabriel Mallezan da Costa Ribeiro
