-- ============================================================================
-- MIGRATION V3: MELHORIAS DE NÍVEL SÊNIOR
-- ============================================================================

-- OBJETIVO: Adicionar recursos avançados para demonstrar expertise sênior:
-- 1. Optimistic Locking (campo version)
-- 2. Auditoria completa (created_by, updated_by)
-- 3. Constraints de validação no banco
-- 4. Trigger para updated_at automático
-- 5. Campo external_hash para sincronização O(n)
-- 6. Views SQL para queries complexas
-- 7. Comentários documentando o schema
-- ============================================================================

-- ============================================================================
-- 1. OPTIMISTIC LOCKING - Controle de Concorrência
-- ============================================================================
-- Problema: Dois usuários editando o mesmo registro simultaneamente
-- Solução: Campo version que JPA incrementa automaticamente
-- Se version mudou, lança OptimisticLockException (HTTP 409 Conflict)

ALTER TABLE artistas ADD COLUMN version INTEGER DEFAULT 0;
ALTER TABLE albuns ADD COLUMN version INTEGER DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN version INTEGER DEFAULT 0;

COMMENT ON COLUMN artistas.version IS 'Controle de concorrência otimista (JPA @Version)';
COMMENT ON COLUMN albuns.version IS 'Controle de concorrência otimista (JPA @Version)';
COMMENT ON COLUMN usuarios.version IS 'Controle de concorrência otimista (JPA @Version)';

-- ============================================================================
-- 2. AUDITORIA COMPLETA - Quem criou/modificou
-- ============================================================================
-- Requisito: Rastreabilidade completa de alterações
-- Campos: created_by (quem criou), updated_by (quem modificou)

ALTER TABLE artistas ADD COLUMN created_by VARCHAR(100);
ALTER TABLE artistas ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE albuns ADD COLUMN created_by VARCHAR(100);
ALTER TABLE albuns ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE imagens_capa ADD COLUMN uploaded_by VARCHAR(100);

COMMENT ON COLUMN artistas.created_by IS 'Username de quem criou o registro';
COMMENT ON COLUMN artistas.updated_by IS 'Username de quem modificou o registro por último';
COMMENT ON COLUMN albuns.created_by IS 'Username de quem criou o registro';
COMMENT ON COLUMN albuns.updated_by IS 'Username de quem modificou o registro por último';
COMMENT ON COLUMN imagens_capa.uploaded_by IS 'Username de quem fez o upload da imagem';

-- ============================================================================
-- 3. TABELA artista_album - Campo papel para colaborações
-- ============================================================================
-- Requisito: Identificar função do artista no álbum (Vocalista, Guitarrista, etc)

ALTER TABLE artista_album ADD COLUMN papel VARCHAR(100);
ALTER TABLE artista_album ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

COMMENT ON COLUMN artista_album.papel IS 'Função do artista no álbum (ex: Vocalista, Guitarrista, Produtor)';

-- ============================================================================
-- 4. REGIONAL - Hash para detecção de mudanças O(n)
-- ============================================================================
-- Problema: Como detectar se dados externos mudaram sem comparar campo a campo?
-- Solução: Hash MD5 dos dados externos - se hash diferente, houve mudança

ALTER TABLE regionais ADD COLUMN external_hash VARCHAR(64);

CREATE INDEX idx_regional_external_hash ON regionais(external_hash);

COMMENT ON COLUMN regionais.external_hash IS 'MD5 hash do nome para detectar alterações na API externa (algoritmo O(n))';

-- ============================================================================
-- 5. CONSTRAINTS DE VALIDAÇÃO
-- ============================================================================
-- Validação no nível mais baixo (banco) garante integridade mesmo se bug no backend

-- Ano de lançamento válido (1900 até ano atual)
ALTER TABLE albuns ADD CONSTRAINT chk_ano_lancamento
    CHECK (ano_lancamento IS NULL OR (ano_lancamento > 1900 AND ano_lancamento <= EXTRACT(YEAR FROM CURRENT_DATE) + 1));

-- Tamanho de arquivo positivo
ALTER TABLE imagens_capa ADD CONSTRAINT chk_tamanho_positivo
    CHECK (tamanho > 0);

-- Content-type apenas imagens
ALTER TABLE imagens_capa ADD CONSTRAINT chk_content_type
    CHECK (content_type LIKE 'image/%');

-- Caminho MinIO único
ALTER TABLE imagens_capa ADD CONSTRAINT uq_caminho_minio
    UNIQUE (caminho_minio);

-- Nome de arquivo único por álbum
ALTER TABLE imagens_capa ADD CONSTRAINT uq_album_filename
    UNIQUE (album_id, nome_arquivo);

-- Username mínimo 3 caracteres
ALTER TABLE usuarios ADD CONSTRAINT chk_username_minlength
    CHECK (LENGTH(username) >= 3);

-- Email para usuários
ALTER TABLE usuarios ADD COLUMN email VARCHAR(200);
ALTER TABLE usuarios ADD COLUMN ativo BOOLEAN DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE usuarios ADD COLUMN updated_at TIMESTAMP DEFAULT NOW();

-- ============================================================================
-- 6. TRIGGER PARA updated_at AUTOMÁTICO
-- ============================================================================
-- Vantagem: Funciona mesmo em queries SQL diretas (não depende do JPA)

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para artistas
CREATE TRIGGER trg_artistas_updated_at
    BEFORE UPDATE ON artistas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para albuns
CREATE TRIGGER trg_albuns_updated_at
    BEFORE UPDATE ON albuns
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para usuarios
CREATE TRIGGER trg_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para regionais
CREATE TRIGGER trg_regionais_updated_at
    BEFORE UPDATE ON regionais
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 7. ÍNDICES ADICIONAIS PARA PERFORMANCE
-- ============================================================================

-- Índice para busca de álbuns por ano
CREATE INDEX idx_album_ano ON albuns(ano_lancamento);

-- Índice para join artista_album
CREATE INDEX idx_artista_album_artista ON artista_album(artista_id);
CREATE INDEX idx_artista_album_album ON artista_album(album_id);

-- Índice para busca de imagens por álbum
CREATE INDEX idx_imagem_album_id ON imagens_capa(album_id);

-- Índice para usuários por username
CREATE INDEX idx_usuario_username ON usuarios(username);

-- ============================================================================
-- 8. VIEWS SQL - Demonstra conhecimento avançado
-- ============================================================================

-- View: Artistas com contagem de álbuns
CREATE OR REPLACE VIEW vw_artistas_com_albuns AS
SELECT
    a.id,
    a.nome,
    a.biografia,
    COUNT(aa.album_id) AS total_albuns,
    a.created_at,
    a.updated_at,
    a.created_by,
    a.updated_by
FROM artistas a
LEFT JOIN artista_album aa ON a.id = aa.artista_id
GROUP BY a.id, a.nome, a.biografia, a.created_at, a.updated_at, a.created_by, a.updated_by;

-- View: Álbuns com artistas concatenados e contagem de capas
CREATE OR REPLACE VIEW vw_albuns_completos AS
SELECT
    alb.id AS album_id,
    alb.titulo,
    alb.ano_lancamento,
    alb.descricao,
    STRING_AGG(DISTINCT art.nome, ', ' ORDER BY art.nome) AS artistas,
    COUNT(DISTINCT ic.id) AS total_capas,
    alb.created_at,
    alb.updated_at,
    alb.created_by,
    alb.updated_by
FROM albuns alb
LEFT JOIN artista_album aa ON alb.id = aa.album_id
LEFT JOIN artistas art ON aa.artista_id = art.id
LEFT JOIN imagens_capa ic ON alb.id = ic.album_id
GROUP BY alb.id, alb.titulo, alb.ano_lancamento, alb.descricao,
         alb.created_at, alb.updated_at, alb.created_by, alb.updated_by;

-- View: Regionais ativas com estatísticas
CREATE OR REPLACE VIEW vw_regionais_ativas AS
SELECT
    r.id,
    r.codigo_externo,
    r.nome,
    r.ultima_sincronizacao,
    r.external_hash,
    CASE
        WHEN r.ultima_sincronizacao IS NULL THEN 'Nunca sincronizada'
        WHEN r.ultima_sincronizacao < NOW() - INTERVAL '1 day' THEN 'Desatualizada'
        ELSE 'Atualizada'
    END AS status_sincronizacao
FROM regionais r
WHERE r.ativa = TRUE;

-- ============================================================================
-- 9. COMENTÁRIOS NAS TABELAS (Documentação no schema)
-- ============================================================================

COMMENT ON TABLE artistas IS 'Armazena informações dos artistas musicais cadastrados no sistema';
COMMENT ON TABLE albuns IS 'Armazena informações dos álbuns musicais, com relacionamento N:N com artistas';
COMMENT ON TABLE artista_album IS 'Tabela de relacionamento N:N entre artistas e álbuns (permite colaborações)';
COMMENT ON TABLE imagens_capa IS 'Metadados das imagens de capa armazenadas no MinIO (S3-compatible)';
COMMENT ON TABLE usuarios IS 'Usuários do sistema com autenticação JWT';
COMMENT ON TABLE regionais IS 'Regionais sincronizadas com API externa (complexidade O(n))';

COMMENT ON COLUMN imagens_capa.caminho_minio IS 'Object key no bucket MinIO (ex: albums/123/cover-abc.jpg)';

-- ============================================================================
-- 10. ATUALIZAR REGISTROS EXISTENTES
-- ============================================================================

-- Definir version inicial para registros existentes
UPDATE artistas SET version = 0 WHERE version IS NULL;
UPDATE albuns SET version = 0 WHERE version IS NULL;
UPDATE usuarios SET version = 0 WHERE version IS NULL;

-- Definir created_by como 'system' para registros existentes (seed data)
UPDATE artistas SET created_by = 'system' WHERE created_by IS NULL;
UPDATE albuns SET created_by = 'system' WHERE created_by IS NULL;
UPDATE imagens_capa SET uploaded_by = 'system' WHERE uploaded_by IS NULL;

-- Calcular hash inicial para regionais existentes
UPDATE regionais SET external_hash = MD5(nome) WHERE external_hash IS NULL;

-- Ativar usuário admin
UPDATE usuarios SET ativo = TRUE WHERE username = 'admin';
