# RFC – SindiGo! - Detalhado
**Aplicação Web para Gestão de Atividades Condominiais**  
Centro Universitário Católica de Santa Catarina  
Aluno: Gustavo Henrique Martins  
Joinville, SC – 2025  

---

## Resumo executivo  
O SindiGo! é uma aplicação web voltada para síndicos e administradoras de condomínios, cujo objetivo é centralizar o planejamento, execução e registro de atividades periódicas (ex.: limpeza de piscina, dedetização, troca de gás) e pontuais (manutenções, emergências). O sistema também oferecerá funcionalidades complementares como reserva de áreas comuns, registro simplificado de fluxos financeiros, relatórios e mecanismos de auditoria para suporte à gestão. O projeto prioriza segurança (OWASP), privacidade (LGPD), usabilidade e praticidade para o dia a dia do síndico.

---

## 1. Introdução

### 1.1 Contexto  
A rotina de gestão condominial envolve tarefas repetitivas (manutenções preventivas) e eventos pontuais (consertos, emergências). Síndicos normalmente utilizam planilhas, mensagens em grupos e memoriais escritos, o que provoca perda de histórico, dificuldade de prestação de contas e risco legal. Conforme Sommerville (2011), falhas em processos administrativos podem comprometer a confiabilidade das informações e gerar impactos legais e financeiros. O SindiGo! se apresenta como solução prática, centralizando rotinas condominiais em uma plataforma única.

### 1.2 Justificativa  
- **Eficiência operacional**: centralizar atividades e reservas reduz tempo gasto em coordenação.  
- **Rastreabilidade**: registros formais essenciais para prestação de contas.  
- **Segurança**: proteção de dados e controle de acessos reduzem riscos.  

### 1.3 Objetivos  
**Objetivo Geral**  
Desenvolver e validar uma aplicação web para gestão de atividades condominiais.  

**Metas Específicas**  
1. Cadastro e autenticação de síndicos e moradores.  
2. Registro e execução de atividades periódicas.  
3. Registro de atividades pontuais e emergenciais.  
4. Reservas de áreas comuns com regras de conflito.  
5. Carteira financeira simplificada.  
6. Relatórios gerenciais e logs de auditoria.  
7. Conformidade com LGPD e OWASP. ( A confirmar com a professora) 
8. Interface responsiva e acessível.  

---

## 2. Descrição do Projeto

### 2.1 Escopo funcional (MVP)  
- Gestão de usuários e papéis.  
- CRUD de condomínios, blocos, unidades.  
- Gestão de atividades periódicas e pontuais.  
- Agenda de reservas.  
- Lançamentos financeiros simples.  
- Dashboard unificado.  
- Exportação de relatórios.  
- Logs de auditoria.  

### 2.2 Fora do escopo  
- ERP financeiro completo.  
- Aplicativo mobile nativo.  
- Consultoria jurídica.  
- Integração bancária direta.  

### 2.3 Personas  
- **Síndico**: administra atividades e reservas.  
- **Morador**: solicita reservas e abre chamados.  
- **Administrador**: configura regras e usuários.  

---

## 3. Especificação Técnica

### 3.1 Requisitos Funcionais  
- RF01: Autenticação e autorização.  
- RF02: Gestão de condomínios/unidades.  
- RF03: Atividades periódicas.  
- RF04: Atividades pontuais.  
- RF05: Reservas de áreas comuns.  
- RF06: Financeiro simplificado.  
- RF07: Dashboard e relatórios.  
- RF08: Auditoria.  

### 3.2 Requisitos Não Funcionais  ( A confirmar com a professora) 
- Disponibilidade 95% no MVP.  
- Resposta < 700 ms p95.  
- Arquitetura escalável.  
- Segurança OWASP Top 10.  
- Backup diário com RTO < 4h.  
- Acessibilidade WCAG AA.  

---

## 4. Arquitetura de Software (C4)

### 4.1 Contexto  
Usuários acessam via navegador (React) → API (Spring Boot) → PostgreSQL + Redis + S3.  

### 4.2 Containers  
- Frontend (React).  
- Backend API (Spring Boot).  
- Worker/Jobs.  
- Banco de dados (Postgres).  
- Cache (Redis).  
- Object Storage (S3).  

### 4.3 Componentes internos  
- Auth, Activities, Reservations, Finance, Notifications, Admin.  

---

## 5. Segurança e Conformidade

### 5.1 OWASP   ( A confirmar com a professora) 
- SQL Injection: ORM + prepared statements.  
- XSS: escape de saída, CSP.  
- CSRF: tokens.  
- Dependências seguras.  
- Logs mascarando PII.  
- Criptografia TLS + AES-256.  

### 5.2 LGPD  
- Consentimento explícito.  
- Direito de exclusão.  
- Política de retenção.  
- Acordos de processamento com provedores.  

---

## 6. Observabilidade e Backup

### 6.1 Observabilidade  
- Métricas Prometheus/Grafana.  
- Logs estruturados JSON.  
- Tracing distribuído.  
- Alertas de erro > 1%.  

### 6.2 Backup  
- Dumps + WAL.  
- Testes trimestrais.  
- Retenção configurável.  

---

## 7. Infraestrutura e Deploy

- Docker multi-stage builds.  
- Registry privado.  
- Deploy inicial em VM/Compose, futuro em Kubernetes.  
- GitHub Actions CI/CD com stages.  

---

## 8. Qualidade e Testes

- Unit tests (JUnit/Jest).  
- Integration tests (TestContainers).  
- E2E (Cypress).  
- Performance (k6).  
- Security (SAST/DAST).  

---

## 9. Integrações e Notificações  ( A confirmar com a professora) 

- Email (SendGrid/SES).  
- Push Web.  
- WhatsApp/SMS via API.  
- Importação CSV.  
- Webhooks externos.  

---

## 10. UX e Acessibilidade

- Design system (Storybook).  
- WCAG AA.  
- Internacionalização.  
- Testes de usabilidade.  

---

## 11. Licenciamento e Ética

- Código sob licença MIT.  
- Uso apenas de OSS compatível.  
- Política de privacidade clara.  
- Plano de resposta a incidentes.  

---

## 12. Roadmap

- **Fase 0:** Preparação.  
- **Fase 1:** Prototipação.  
- **Fase 2:** Infra e backlog.  
- **Fase 3:** MVP (8–12 semanas).  
- **Fase 4:** Testes e segurança.  
- **Fase 5:** Validação.  
- **Fase 6:** Documentação e entrega.  

---

## 13. Referências

- Brasil. Lei nº 13.709/2018 (LGPD).  
- OWASP Top 10.  
- RFC 7519 – JWT.  
- ISO/IEC 27001.  
- SOMMERVILLE, Ian. *Engenharia de Software*. 10. ed. Pearson, 2011.  
- PRESSMAN, Roger S. *Engenharia de Software: uma abordagem profissional*. 8. ed. McGraw-Hill, 2016.   

---

## Autor
**Gustavo Henrique Martins**  
Curso: Engenharia de Software – Católica de Santa Catarina  
