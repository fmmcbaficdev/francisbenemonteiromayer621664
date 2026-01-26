-- TABELA: artistas
CREATE TABLE artistas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    biografia TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_artista_nome ON artistas(nome);

-- TABELA: albuns
CREATE TABLE albuns (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    ano_lancamento INTEGER,
    descricao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_album_titulo ON albuns(titulo);

-- TABELA: artista_album (N:N)
CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL REFERENCES artistas(id) ON DELETE CASCADE,
    album_id BIGINT NOT NULL REFERENCES albuns(id) ON DELETE CASCADE,
    PRIMARY KEY (artista_id, album_id)
);

-- TABELA: imagens_capa
CREATE TABLE imagens_capa (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL REFERENCES albuns(id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_minio VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- TABELA: usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(200) NOT NULL
);

-- TABELA: regionais
CREATE TABLE regionais (
    id BIGSERIAL PRIMARY KEY,
    codigo_externo INTEGER NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    ultima_sincronizacao TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regional_codigo_externo ON regionais(codigo_externo);
CREATE INDEX idx_regional_ativa ON regionais(ativa);

