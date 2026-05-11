# Sistema de Gerenciamento de Locadora - BD I

## Descrição

Sistema Java desenvolvido para manipulação de dados de uma locadora de filmes utilizando banco de dados PostgreSQL. O sistema atende aos requisitos solicitados no 1º trabalho da disciplina de Banco de Dados I.

## Requisitos Implementados

✓ **Gerenciamento de Filmes**
- Incluir novo filme
- Alterar dados de um filme existente
- Excluir filme (com validação de fitas associadas)
- Listar todos os filmes cadastrados

✓ **Gerenciamento de Fitas**
- Incluir nova fita (associada a um filme)
- Alterar situação da fita (Disponível, Alugada, Danificada)
- Listar todas as fitas com suas situações

✓ **Gerenciamento de Locações**
- Realizar locação (vincular cliente, fita e valor)
- Devolver fita (registra data de devolução)
- Listar locações ativas e finalizadas

## Estrutura do Banco de Dados

O sistema utiliza as seguintes tabelas:

### FILME
- `cod_filme` (PK): Código identificador
- `nom_filme`: Nome do filme
- `cod_cor`: Código da cor (FK)
- `cod_genero`: Código do gênero (FK)

### FITA
- `cod_fita` (PK): Código identificador
- `cod_filme` (FK): Código do filme
- `sit_fita`: Situação (1=Disponível, 2=Alugada, 3=Danificada)
- `dat_aquisicao`: Data de aquisição

### LOCAÇÃO
- `cod_locacao` (PK): Código identificador
- `cod_cliente` (FK): Código do cliente
- `cod_fita` (FK): Código da fita
- `dat_locacao`: Data e hora da locação
- `dat_prevista_devolucao`: Data prevista de devolução
- `dat_devolucao`: Data/hora real de devolução (NULL se não devolvida)
- `val_locacao`: Valor da locação

### Tabelas de Suporte
- **CLIENTE**: Dados dos clientes
- **COR**: Cores disponíveis com valor de fita
- **GENERO**: Gêneros de filmes
- **DOMINIO**: Domínios (lookup tables)

## Funcionalidades Principais

### 1. Gerenciamento de Filmes

#### Incluir Filme
- Solicita nome, cor e gênero
- Valida cor e gênero existentes
- Retorna código gerado automaticamente

#### Alterar Filme
- Permite alterar: nome, cor ou gênero
- Valida dados antes de atualizar

#### Excluir Filme
- Verifica se existem fitas associadas
- Solicita confirmação antes de excluir
- Impede exclusão se há fitas do filme

#### Listar Filmes
- Exibe tabela formatada com código, nome, cor e gênero

### 2. Gerenciamento de Fitas

#### Incluir Fita
- Associa fita a um filme existente
- Define situação inicial como "Disponível"
- Registra data de aquisição

#### Alterar Situação
- Oferece opções: Disponível (1), Alugada (2), Danificada (3)
- Atualiza status da fita

#### Listar Fitas
- Exibe todas as fitas com informações do filme e situação

### 3. Gerenciamento de Locações

#### Realizar Locação
- Seleciona cliente válido
- Seleciona fita disponível
- Define valor e duração (padrão: 3 dias)
- Calcula data prevista de devolução automaticamente
- Atualiza situação da fita para "Alugada"

#### Devolver Fita
- Lista locações ativas
- Registra data/hora de devolução
- Atualiza situação da fita para "Disponível"

#### Listar Locações
- Exibe histórico completo de locações
- Indica se locação está ativa ou finalizada

## Requisitos Técnicos

### Dependências
- Java 8 ou superior
- PostgreSQL 10 ou superior
- Driver JDBC PostgreSQL

### Configuração

Antes de executar, configure as credenciais do banco de dados em `TrabalhoBD.java`:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/loc004";
private static final String USER = "postgres";
private static final String PASSWORD = "postgres";
```

### Compilação

```bash
javac TrabalhoBD.java
```

### Execução

```bash
java -cp .;postgresql-VERSÃO.jar TrabalhoBD
```

## Menu de Operações

```
===============================================
         SISTEMA DE LOCADORA - MENU PRINCIPAL
===============================================
--- FILMES ---
1. Incluir novo filme
2. Alterar filme
3. Excluir filme
4. Listar filmes
--- FITAS ---
5. Incluir nova fita
6. Alterar situação da fita
7. Listar fitas
--- LOCAÇÕES ---
8. Realizar locação
9. Devolver fita
10. Listar locações
0. Sair
```

## Validações Implementadas

- ✓ Verificação de existência de registros antes de operar
- ✓ Validação de cor e gênero contra tabelas de domínio
- ✓ Impedimento de excluir filme com fitas associadas
- ✓ Verificação de disponibilidade de fita antes de locar
- ✓ Validação de cliente existente
- ✓ Confirmação antes de exclusões críticas

## Tratamento de Erros

O sistema inclui tratamento abrangente de exceções SQL com mensagens amigáveis ao usuário:
- Mensagens de sucesso (✓)
- Mensagens de erro (✗)
- Recuperação de IDs gerados automaticamente

## Tratamento de Datas

- Datas de aquisição: `yyyy-MM-dd`
- Datas/horas de locação: Timestamp automático
- Cálculo automático de data prevista de devolução
- Registro automático de data de devolução

## Fluxo Típico de Uso

### Cenário: Alugar um Filme

1. **Incluir Filme** (se novo)
   - Menu → 1 → Preencher dados

2. **Incluir Fita** (se nova)
   - Menu → 5 → Selecionar filme → Data aquisição

3. **Realizar Locação**
   - Menu → 8 → Selecionar cliente → Selecionar fita → Valor e duração
   - Sistema atualiza automaticamente fita como "Alugada"

4. **Devolver Fita**
   - Menu → 9 → Selecionar locação
   - Sistema registra devolução e muda fita para "Disponível"

## Notas de Implementação

- Uso de `PreparedStatement` para prevenir SQL Injection
- Pool de conexões gerenciado automaticamente
- Fechamento de recursos (ResultSet, Statement) garantido
- Formatação de tabelas para melhor legibilidade
- Interface interativa por linha de comando

## Autor

Aluno: Desenvolvido para o 1º Trabalho - Banco de Dados I

## Data de Entrega

25/05/2026

## Observações

O sistema foi desenvolvido seguindo boas práticas de programação e segurança de banco de dados. Todas as operações utilizam `PreparedStatement` para evitar SQL Injection. O código está bem estruturado com métodos bem definidos e validações apropriadas em cada operação.
