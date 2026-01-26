-- ============================================
-- INSERIR USUÁRIO PADRÃO
-- ============================================
-- Senha: admin123 (BCrypt)
INSERT INTO usuarios (username, password, nome) VALUES
('admin', '$2a$10$rZJ1JKuZ8.Nxw.GQx8h8OuGY7k5oLZ9hO.6fK9qC7kGYqXD8YZJKa', 'Administrador');

-- ============================================
-- INSERIR ARTISTAS (CONFORME EDITAL)
-- ============================================
INSERT INTO artistas (nome, biografia) VALUES
('Serj Tankian', 'Vocalista da banda System of a Down, também conhecido por sua carreira solo.'),
('Mike Shinoda', 'Vocalista, guitarrista e rapper da banda Linkin Park.'),
('Michel Teló', 'Cantor e compositor brasileiro de música sertaneja.'),
('Guns N'' Roses', 'Banda americana de hard rock formada em 1985.');

-- ============================================
-- INSERIR ÁLBUNS (CONFORME EDITAL)
-- ============================================

-- Álbuns de Serj Tankian
INSERT INTO albuns (titulo, ano_lancamento, descricao) VALUES
('Harakiri', 2012, 'Terceiro álbum solo de Serj Tankian'),
('Black Blooms', 2021, 'EP lançado durante a pandemia'),
('The Rough Dog', NULL, 'Projeto colaborativo');

-- Álbuns de Mike Shinoda
INSERT INTO albuns (titulo, ano_lancamento, descricao) VALUES
('The Rising Tied', 2005, 'Álbum do projeto Fort Minor'),
('Post Traumatic', 2018, 'Álbum solo pós-tragédia do Linkin Park'),
('Post Traumatic EP', 2018, 'EP que precedeu o álbum completo'),
('Where''d You Go', 2006, 'Single do Fort Minor');

-- Álbuns de Michel Teló
INSERT INTO albuns (titulo, ano_lancamento, descricao) VALUES
('Bem Sertanejo', 2012, 'Álbum que inclui o hit "Ai Se Eu Te Pego"'),
('Bem Sertanejo - O Show (Ao Vivo)', 2013, 'Versão ao vivo do álbum'),
('Bem Sertanejo - (1ª Temporada) - EP', 2012, 'EP da primeira temporada');

-- Álbuns de Guns N' Roses
INSERT INTO albuns (titulo, ano_lancamento, descricao) VALUES
('Use Your Illusion I', 1991, 'Parte 1 do álbum duplo'),
('Use Your Illusion II', 1991, 'Parte 2 do álbum duplo'),
('Greatest Hits', 2004, 'Coletânea dos maiores sucessos');

-- ============================================
-- ASSOCIAR ARTISTAS E ÁLBUNS (N:N)
-- ============================================

-- Serj Tankian (id=1) -> Álbuns (1, 2, 3)
INSERT INTO artista_album (artista_id, album_id) VALUES
(1, 1),  -- Harakiri
(1, 2),  -- Black Blooms
(1, 3);  -- The Rough Dog

-- Mike Shinoda (id=2) -> Álbuns (4, 5, 6, 7)
INSERT INTO artista_album (artista_id, album_id) VALUES
(2, 4),  -- The Rising Tied
(2, 5),  -- Post Traumatic
(2, 6),  -- Post Traumatic EP
(2, 7);  -- Where'd You Go

-- Michel Teló (id=3) -> Álbuns (8, 9, 10)
INSERT INTO artista_album (artista_id, album_id) VALUES
(3, 8),   -- Bem Sertanejo
(3, 9),   -- Bem Sertanejo - O Show
(3, 10);  -- Bem Sertanejo - EP

-- Guns N' Roses (id=4) -> Álbuns (11, 12, 13)
INSERT INTO artista_album (artista_id, album_id) VALUES
(4, 11),  -- Use Your Illusion I
(4, 12),  -- Use Your Illusion II
(4, 13);  -- Greatest Hits

-- Inserir dados iniciais (serão sincronizados depois)
INSERT INTO regionais (codigo_externo, nome, ativa) VALUES
(9, 'REGIONAL DE CUIABÁ', true),
(10, 'REGIONAL DE VÁRZEA GRANDE', true),
(20, 'REGIONAL DE RONDONÓPOLIS', true);
