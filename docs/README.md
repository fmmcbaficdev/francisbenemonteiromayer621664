# Documentação do projeto

Documentos disponíveis para o avaliador e para deploy.

| Documento | Uso |
|-----------|-----|
| **[COMO_ABRIR_PARA_AVALIADOR.md](COMO_ABRIR_PARA_AVALIADOR.md)** | Passo a passo mínimo para clonar, configurar (`.env`) e subir o projeto com Docker. |
| **[PROBLEMAS_COMUNS_AVALIADOR.md](PROBLEMAS_COMUNS_AVALIADOR.md)** | Problemas ao executar (porta em uso, backend não healthy, CORS, 429, etc.) e como resolver. |
| **[TESTES_VIA_SWAGGER.md](TESTES_VIA_SWAGGER.md)** | Como testar a API via Swagger (login, Authorize com JWT, endpoints). |
| **[REVISAO_EDITAL_ANEXO_IIC.md](REVISAO_EDITAL_ANEXO_IIC.md)** | Conferência requisito a requisito do edital (Anexo II-C) e onde verificar no código. |
| **[REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md](REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md)** | Como o avaliador pode testar cada requisito (health checks, testes unitários, rate limit, WebSocket, etc.). |
| **[DEPLOY_PRODUCAO.md](DEPLOY_PRODUCAO.md)** | Deploy em produção com Docker Compose e imagens do registry (Docker Hub). |

**Ordem sugerida para o avaliador:** 1) [COMO_ABRIR_PARA_AVALIADOR](COMO_ABRIR_PARA_AVALIADOR.md) → 2) em caso de erro, [PROBLEMAS_COMUNS_AVALIADOR](PROBLEMAS_COMUNS_AVALIADOR.md) → 3) [TESTES_VIA_SWAGGER](TESTES_VIA_SWAGGER.md) para testar a API → 4) [REVISAO_EDITAL_ANEXO_IIC](REVISAO_EDITAL_ANEXO_IIC.md) e [REQUISITOS_SENIOR](REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md) para conferir requisitos.
