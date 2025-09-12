# SindiGo! 

## Resumo  
O **SindiGo!** é uma aplicação web voltada para síndicos e administradoras condominiais que enfrentam desafios na organização de atividades periódicas (ex.: limpeza de piscina, dedetização, manutenção preventiva) e pontuais (ex.: reparos emergenciais, incidentes). O sistema oferece **rastreamento completo**, **reserva de áreas comuns**, **acompanhamento financeiro simplificado** e relatórios gerenciais. Sua arquitetura baseia-se em **Spring Boot, React e PostgreSQL**, promovendo escalabilidade, segurança (JWT, OWASP Top 10) e modularidade.  

---

## 1. Introdução  

### Contexto  
A gestão condominial exige organização de diversas atividades e fluxos de informação. A ausência de ferramentas especializadas leva síndicos a utilizarem planilhas, grupos de mensagens e sistemas descentralizados, complicando a rastreabilidade. Conforme Sommerville (2011), falhas em processos administrativos podem comprometer a confiabilidade das informações e gerar impactos legais e financeiros. O **SindiGo!** se apresenta como solução prática, centralizando rotinas condominiais em uma plataforma única.  

### Justificativa  
A proposta é fundamentada em três pilares:  
- **Eficiência:** redução de ruído na comunicação e ganho de produtividade.  
- **Transparência:** registro de histórico de ações e auditoria para prestação de contas.  
- **Conformidade:** adequação à LGPD e aplicação de boas práticas de segurança.  

### Objetivos  

#### Objetivo Principal  
Desenvolver um sistema web para gestão condominial, permitindo cadastro de usuários, controle de atividades, reservas de áreas e registros financeiros simplificados.  

#### Metas  
- Implementar autenticação segura e controle de acessos baseado em papéis.  
- Disponibilizar dashboards intuitivos para visualização de tarefas e reservas.  
- Registrar atividades periódicas e pontuais com rastreabilidade.  
- Incorporar módulo de finanças simplificado com relatórios de entradas/saídas.  
- Garantir escalabilidade e disponibilidade em nuvem.  

---

## 2. Descrição do Projeto  

### Tema  
Sistema web de gestão condominial com foco em atividades periódicas, reservas e finanças básicas.  

### Problemas a Resolver  
- Dificuldade em monitorar atividades recorrentes.  
- Falta de organização e conflitos na reserva de áreas comuns.  
- Complexidade na gestão financeira simplificada.  
- Carência de sistema unificado para síndicos.  

### Limitações  
- Não será um ERP financeiro completo.  
- Não terá aplicativo mobile nativo (apenas web responsivo).  
- Questões jurídicas e legais específicas não estarão no escopo.  

---

## 3. Especificação Técnica  

### Requisitos Funcionais (RF)  
- RF01: Permitir cadastro e autenticação de síndicos e moradores.  
- RF02: Registrar atividades periódicas com cronogramas.  
- RF03: Registrar atividades pontuais emergenciais.  
- RF04: Controlar reservas de áreas comuns com regras de conflito.  
- RF05: Manter registro financeiro simplificado de entradas e saídas.  
- RF06: Disponibilizar dashboard com calendário e relatórios.  
- RF07: Manter histórico de auditoria de ações.  

### Requisitos Não Funcionais (RNF)  
- RNF01: Garantir hospedagem em nuvem com disponibilidade mínima de 95%.  
- RNF02: Garantir autenticação segura via JWT e hash de senhas.  
- RNF03: Fornecer interface responsiva e acessível (WCAG 2.1).  
- RNF04: Utilizar banco de dados relacional (PostgreSQL).  
- RNF05: Disponibilizar API REST documentada via OpenAPI.  

---

## 4. Stack Tecnológica e Considerações de Design  

### Considerações de Design  
- **Arquitetura em Camadas (MVC):** separação de responsabilidades entre domínio, API e frontend.  
- **API RESTful:** comunicação entre backend (Spring Boot) e frontend (React).  
- **Deploy em containers:** uso de Docker para padronização.  
- **Escalabilidade:** design stateless no backend e uso de cache quando necessário.  

### Tecnologias Utilizadas  
| Camada         | Tecnologias                            |
|----------------|---------------------------------------|
| Linguagens     | Java, JavaScript/TypeScript           |
| Backend        | Spring Boot, Hibernate                |
| Frontend       | React, Tailwind CSS                   |
| Banco de Dados | PostgreSQL                            |
| Containerização| Docker                                |
| CI/CD          | GitHub Actions                        |
| Monitoramento  | Prometheus, Grafana, Sentry           |

---

## 5. Diagramas de Caso de Uso (UML)  

### Caso de Uso 1: Cadastro e Autenticação  
*(Diagrama a ser incluído no apêndice)*  

### Caso de Uso 2: Gestão de Atividades  
*(Diagrama a ser incluído no apêndice)*  

### Caso de Uso 3: Reservas de Áreas Comuns  
*(Diagrama a ser incluído no apêndice)*  

---

## 6. Modelagem C4  

O modelo C4 será utilizado para representar a arquitetura em níveis (Contexto, Containers, Componentes e Código).  

- **Contexto:** usuários (síndicos e moradores) acessam a aplicação via navegador.  
- **Containers:** frontend (React) e backend (Spring Boot) conectados ao PostgreSQL.  
- **Componentes:** módulos de autenticação, atividades, reservas, finanças e auditoria.  
- **Código:** orientado a DDD, SOLID e testes automatizados.  

*(Diagramas C4 a serem adicionados em anexo)*  

---

## 7. Considerações de Segurança  

- **HTTPS obrigatório** em todas as comunicações.  
- **Autenticação JWT + RBAC** para controle de acesso.  
- **Proteção contra ataques comuns:** prevenção de SQL Injection, XSS e CSRF.  
- **Conformidade com LGPD:** coleta mínima de dados, consentimento explícito e direito de exclusão.  
- **Monitoramento contínuo:** auditoria de acessos e logs estruturados.  

---

## 8. Próximos Passos  

- Refinar os requisitos funcionais e diagramas UML.  
- Criar protótipos navegáveis no Figma.  
- Configurar ambiente de desenvolvimento e CI/CD no GitHub Actions.  
- Implementar MVP com sprints quinzenais (autenticação, atividades, reservas, financeiro).  
- Testar segurança (OWASP ZAP) e usabilidade com usuários reais.  
- Implantar versão piloto em condomínio parceiro para validação.  

---

## 9. Referências  

### Frameworks e Bibliotecas  
- [Spring Boot](https://spring.io/projects/spring-boot)  
- [React.js](https://reactjs.org/)  
- [Tailwind CSS](https://tailwindcss.com/)  
- [PostgreSQL](https://www.postgresql.org/)  
- [Docker](https://www.docker.com/)  
- [GitHub Actions](https://github.com/features/actions)  

### Segurança e Conformidade  
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)  
- [LGPD – Lei nº 13.709/2018](https://www.gov.br/cidadania/pt-br/acesso-a-informacao/lgpd)  

### Livros e Artigos  
- SOMMERVILLE, Ian. *Engenharia de Software*. 10. ed. Pearson, 2011.  
- PRESSMAN, Roger S. *Engenharia de Software: uma abordagem profissional*. 8. ed. McGraw-Hill, 2016.  

---

## 10. Autor  

**Gustavo Henrique Martins**  
Curso: Engenharia de Software – Católica de Santa Catarina  
Orientadores: Professores: 
"""
