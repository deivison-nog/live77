# Live 77 Admin

Painel administrativo genérico para gestão de usuários.

## Requisitos
- PHP 8+
- MySQL/MariaDB
- Apache ou Nginx
- Extensão PDO MySQL habilitada

## Instalação

1. Copie os arquivos para o seu servidor local.
2. Crie o banco e tabelas executando `sql/schema.sql`.
3. Ajuste `includes/config.php` com seus dados do banco.
4. Acesse `login.php`.

## Login inicial
- Email: `admin@local.test`
- Senha: você deve gerar um hash válido com `password_hash()` antes de usar em produção.

## Observações
- Login e senha dos usuários são compostos por exatamente 6 números.
- O acesso padrão expira em 30 dias.
- Recomendado alterar credenciais padrão e proteger o ambiente com HTTPS.