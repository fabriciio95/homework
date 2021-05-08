# Homework
[![NPM](https://img.shields.io/github/license/fabriciio95/homework)](https://github.com/fabriciio95/homework/blob/master/LICENSE) 

# Sobre o projeto

http://dothehomework.herokuapp.com/

Homework é um sistema para criação e entrega de trabalhos acadêmicos construído para fins de **aprendizado**.

## Modelo Conceitual
![Conceitual](https://github.com/fabriciio95/arquivos-read-me/blob/master/arquivos-homework/modelo-conceitual.jpg)

## Modelo Lógico
![Lógico](https://github.com/fabriciio95/arquivos-read-me/blob/master/arquivos-homework/modelo-logico.jpg)

# Tecnologias utilizadas
- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- Gradle
- Java Bean Validation
- Lombok
- Thymeleaf
- JasperReports
- JavaMail

# Implantação em produção
- Heroku: [Homework](http://dothehomework.herokuapp.com/)
- Banco de dados: PostgreSQL

# Como executar o projeto
Pré-requisitos: Java 11, Gradle 6.8.3, PostgreSQL, PgAdmin

```bash
# Abra o PgAdmin e crie um banco de dados chamado homeworkdb com usuário postgres e senha admin

# clonar repositório
git clone https://github.com/fabriciio95/homework

# Entre na pasta raiz do projeto:
cd homework

# E para rodar o projeto, você pode executar:
gradle bootRun
```
