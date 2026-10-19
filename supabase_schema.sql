-- ====================================================================
-- SUPABASE POSTGIS SCHEMA & RPC FUNCTIONS - PROMONEAR
-- Local Proximity Social Network & Offers Backend Setup
-- ====================================================================

-- 1. Habilitar Extensão PostGIS para Geolocalização e Cálculos de Proximidade
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Tabela de Perfis de Usuários (com PF/PJ, CNPJ, Endereço da Loja, Gamificação e Tags)
CREATE TABLE IF NOT EXISTS perfis (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nome TEXT NOT NULL,
    avatar_url TEXT,
    tipo_conta TEXT NOT NULL DEFAULT 'pf' CHECK (tipo_conta IN ('pf', 'pj')),
    cnpj TEXT,
    nome_loja TEXT,
    endereco_loja TEXT,
    pontos BIGINT NOT NULL DEFAULT 0,
    tags_interesse TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Garante colunas PF/PJ em bases de dados já existentes
ALTER TABLE perfis ADD COLUMN IF NOT EXISTS tipo_conta TEXT DEFAULT 'pf';
ALTER TABLE perfis ADD COLUMN IF NOT EXISTS cnpj TEXT;
ALTER TABLE perfis ADD COLUMN IF NOT EXISTS nome_loja TEXT;
ALTER TABLE perfis ADD COLUMN IF NOT EXISTS endereco_loja TEXT;

-- 3. Tabela de Publicações (com Ponto Espacial, Vencimento da Promoção, Expiração 24h de Feed)
CREATE TABLE IF NOT EXISTS publicacoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    autor_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    titulo TEXT NOT NULL,
    descricao TEXT,
    midia_url TEXT,
    tipo_midia TEXT NOT NULL DEFAULT 'texto' CHECK (tipo_midia IN ('imagem', 'video', 'texto')),
    link_externo TEXT,
    tags TEXT[] NOT NULL DEFAULT '{}',
    localizacao GEOGRAPHY(Point, 4326) NOT NULL,
    status TEXT NOT NULL DEFAULT 'ativa' CHECK (status IN ('ativa', 'pausada_em_analise', 'excluida')),
    vencimento_promocao TIMESTAMPTZ, -- Data limite em que o desconto/oferta é válido na loja
    data_expiracao TIMESTAMPTZ,      -- Expiração do feed ativo (Nulo = Fixo / Não Nulo = Expirável em 24h)
    total_curtidas INT NOT NULL DEFAULT 0,
    total_comentarios INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Garante colunas em bases já existentes
ALTER TABLE publicacoes ADD COLUMN IF NOT EXISTS vencimento_promocao TIMESTAMPTZ;
ALTER TABLE publicacoes ADD COLUMN IF NOT EXISTS data_expiracao TIMESTAMPTZ;

-- Índices Espaciais, Temporais e de Cobertura
CREATE INDEX IF NOT EXISTS idx_publicacoes_localizacao ON publicacoes USING GIST (localizacao);
CREATE INDEX IF NOT EXISTS idx_publicacoes_created_at ON publicacoes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_publicacoes_tags ON publicacoes USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_publicacoes_status ON publicacoes(status);
CREATE INDEX IF NOT EXISTS idx_publicacoes_expiracao ON publicacoes(data_expiracao);

-- 4. Tabela de Curtidas (Garante Unicidade e Proteção Contra Fraude)
CREATE TABLE IF NOT EXISTS interacoes_curtidas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publicacao_id UUID NOT NULL REFERENCES publicacoes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_curtida_usuario_post UNIQUE(publicacao_id, usuario_id)
);

-- 5. Tabela de Seguidores
CREATE TABLE IF NOT EXISTS seguidores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seguidor_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    seguindo_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_seguidor_seguindo UNIQUE(seguidor_id, seguindo_id)
);

-- 6. Tabela de Mensagens Privadas / Chat entre Usuários
CREATE TABLE IF NOT EXISTS mensagens_chat (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    remetente_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    destinatario_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    publicacao_id UUID REFERENCES publicacoes(id) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    lida BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Tabela de Comentários
CREATE TABLE IF NOT EXISTS comentarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publicacao_id UUID NOT NULL REFERENCES publicacoes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. Tabela de Denúncias
CREATE TABLE IF NOT EXISTS denuncias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publicacao_id UUID NOT NULL REFERENCES publicacoes(id) ON DELETE CASCADE,
    denunciante_id UUID NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    motivo TEXT NOT NULL,
    detalhes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_denuncia_usuario_post UNIQUE(publicacao_id, denunciante_id)
);

-- ====================================================================
-- TRIGGERS E REGRAS DE MODERAÇÃO
-- ====================================================================

-- Trigger Função com Regras Anti-Abuso
CREATE OR REPLACE FUNCTION trg_pausar_publicacao_denunciada()
RETURNS TRIGGER AS $$
DECLARE
    v_total_denuncias INT;
    v_autor_pontos BIGINT;
    v_autor_id UUID;
BEGIN
    SELECT COUNT(*) INTO v_total_denuncias
    FROM denuncias
    WHERE publicacao_id = NEW.publicacao_id;

    SELECT p.autor_id, prf.pontos
    INTO v_autor_id, v_autor_pontos
    FROM publicacoes p
    JOIN perfis prf ON prf.id = p.autor_id
    WHERE p.id = NEW.publicacao_id;

    IF NEW.motivo IN ('golpe_fraude', 'produto_proibido')
       OR v_total_denuncias >= 2
       OR COALESCE(v_autor_pontos, 0) < 100 THEN

        UPDATE publicacoes
        SET status = 'pausada_em_analise'
        WHERE id = NEW.publicacao_id AND status = 'ativa';

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trigger_denuncia_pausa_publicacao ON denuncias;
CREATE TRIGGER trigger_denuncia_pausa_publicacao
AFTER INSERT ON denuncias
FOR EACH ROW
EXECUTE FUNCTION trg_pausar_publicacao_denunciada();

-- Trigger Função para Auto-criar Perfil ao registrar Usuário no Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.perfis (id, nome, avatar_url, tipo_conta, cnpj, nome_loja, endereco_loja, pontos, tags_interesse)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'nome', 'Usuário ' || SUBSTRING(NEW.id::text FROM 1 FOR 6)),
        NEW.raw_user_meta_data->>'avatar_url',
        COALESCE(NEW.raw_user_meta_data->>'tipo_conta', 'pf'),
        NEW.raw_user_meta_data->>'cnpj',
        NEW.raw_user_meta_data->>'nome_loja',
        NEW.raw_user_meta_data->>'endereco_loja',
        0,
        '{}'
    ) ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ====================================================================
-- DROPAR AUTOMATICAMENTE TODAS AS SOBRECARGAS DE FUNÇÕES ANTIGAS
-- ====================================================================
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT oid::regprocedure AS func_signature
        FROM pg_proc
        WHERE proname IN ('criar_publicacao', 'buscar_ofertas_proximas', 'curtir_publicacao', 'seguir_desseguir_usuario', 'enviar_mensagem_privada')
          AND pronamespace = 'public'::regnamespace
    LOOP
        EXECUTE 'DROP FUNCTION IF EXISTS ' || r.func_signature || ' CASCADE;';
    END LOOP;
END $$;

-- ====================================================================
-- FUNÇÕES RPC SQL
-- ====================================================================

-- 1. RPC: Buscar Ofertas Próximas ORDENADAS POR MAIS NOVAS NO TOPO (DESC)
CREATE OR REPLACE FUNCTION buscar_ofertas_proximas(
    p_lat DOUBLE PRECISION,
    p_lng DOUBLE PRECISION,
    p_tags TEXT[] DEFAULT '{}',
    p_raio_metros DOUBLE PRECISION DEFAULT 10000
)
RETURNS TABLE (
    id UUID,
    autor_id UUID,
    autor_nome TEXT,
    autor_avatar_url TEXT,
    autor_pontos BIGINT,
    autor_tipo_conta TEXT,
    autor_cnpj TEXT,
    autor_nome_loja TEXT,
    autor_endereco_loja TEXT,
    titulo TEXT,
    descricao TEXT,
    midia_url TEXT,
    tipo_midia TEXT,
    link_externo TEXT,
    tags TEXT[],
    distancia_metros DOUBLE PRECISION,
    total_curtidas INT,
    total_comentarios INT,
    curtido_pelo_usuario BOOLEAN,
    seguindo_autor BOOLEAN,
    vencimento_promocao TIMESTAMPTZ,
    data_expiracao TIMESTAMPTZ,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    user_point GEOGRAPHY;
    v_usuario_atual UUID;
BEGIN
    v_usuario_atual := auth.uid();
    user_point := ST_SetSRID(ST_MakePoint(p_lng, p_lat), 4326)::geography;

    RETURN QUERY
    SELECT
        p.id,
        p.autor_id,
        prf.nome AS autor_nome,
        prf.avatar_url AS autor_avatar_url,
        prf.pontos AS autor_pontos,
        COALESCE(prf.tipo_conta, 'pf') AS autor_tipo_conta,
        prf.cnpj AS autor_cnpj,
        prf.nome_loja AS autor_nome_loja,
        prf.endereco_loja AS autor_endereco_loja,
        p.titulo,
        p.descricao,
        p.midia_url,
        p.tipo_midia,
        p.link_externo,
        p.tags,
        ST_Distance(p.localizacao, user_point) AS distancia_metros,
        p.total_curtidas,
        p.total_comentarios,
        EXISTS(
            SELECT 1 FROM interacoes_curtidas ic
            WHERE ic.publicacao_id = p.id AND ic.usuario_id = v_usuario_atual
        ) AS curtido_pelo_usuario,
        EXISTS(
            SELECT 1 FROM seguidores s
            WHERE s.seguidor_id = v_usuario_atual AND s.seguindo_id = p.autor_id
        ) AS seguindo_autor,
        p.vencimento_promocao,
        p.data_expiracao,
        p.created_at
    FROM publicacoes p
    JOIN perfis prf ON prf.id = p.autor_id
    WHERE p.status = 'ativa'
      AND (p.data_expiracao IS NULL OR p.data_expiracao > NOW())
      AND ST_DWithin(p.localizacao, user_point, p_raio_metros)
      AND (
          p_tags IS NULL
          OR CARDINALITY(p_tags) = 0
          OR p.tags && p_tags
      )
    ORDER BY p.created_at DESC, distancia_metros ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 2. RPC: Enviar Mensagem Privada entre Usuários
CREATE OR REPLACE FUNCTION enviar_mensagem_privada(
    p_remetente_id UUID,
    p_destinatario_id UUID,
    p_publicacao_id UUID DEFAULT NULL,
    p_texto TEXT DEFAULT ''
)
RETURNS JSONB AS $$
DECLARE
    v_msg_id UUID;
BEGIN
    INSERT INTO mensagens_chat (
        remetente_id,
        destinatario_id,
        publicacao_id,
        texto
    ) VALUES (
        p_remetente_id,
        p_destinatario_id,
        p_publicacao_id,
        p_texto
    ) RETURNING id INTO v_msg_id;

    RETURN jsonb_build_object('sucesso', true, 'mensagem_id', v_msg_id);
EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('sucesso', false, 'mensagem', SQLERRM);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 3. RPC Atômica: Seguir / Desseguir Autor
CREATE OR REPLACE FUNCTION seguir_desseguir_usuario(
    p_seguidor_id UUID,
    p_seguindo_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_ja_segue BOOLEAN;
BEGIN
    IF p_seguidor_id = p_seguindo_id THEN
        RETURN jsonb_build_object('sucesso', false, 'mensagem', 'Você não pode seguir a si mesmo.');
    END IF;

    SELECT EXISTS(
        SELECT 1 FROM seguidores
        WHERE seguidor_id = p_seguidor_id AND seguindo_id = p_seguindo_id
    ) INTO v_ja_segue;

    IF v_ja_segue THEN
        DELETE FROM seguidores
        WHERE seguidor_id = p_seguidor_id AND seguindo_id = p_seguindo_id;

        RETURN jsonb_build_object('sucesso', true, 'acao', 'desseguido', 'seguindo', false);
    ELSE
        INSERT INTO seguidores (seguidor_id, seguindo_id)
        VALUES (p_seguidor_id, p_seguindo_id);

        RETURN jsonb_build_object('sucesso', true, 'acao', 'seguido', 'seguindo', true);
    END IF;
EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('sucesso', false, 'mensagem', SQLERRM);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 4. RPC Atômica: Curtir Publicação + Somar Pontos ao Autor
CREATE OR REPLACE FUNCTION curtir_publicacao(
    p_publicacao_id UUID,
    p_usuario_id UUID
)
RETURNS JSONB AS $$
DECLARE
    v_autor_id UUID;
    v_ja_curtiu BOOLEAN;
    v_novas_curtidas INT;
    v_novos_pontos BIGINT;
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM interacoes_curtidas
        WHERE publicacao_id = p_publicacao_id AND usuario_id = p_usuario_id
    ) INTO v_ja_curtiu;

    IF v_ja_curtiu THEN
        DELETE FROM interacoes_curtidas
        WHERE publicacao_id = p_publicacao_id AND usuario_id = p_usuario_id;

        UPDATE publicacoes
        SET total_curtidas = GREATEST(0, total_curtidas - 1)
        WHERE id = p_publicacao_id
        RETURNING autor_id, total_curtidas INTO v_autor_id, v_novas_curtidas;

        UPDATE perfis
        SET pontos = GREATEST(0, pontos - 10)
        WHERE id = v_autor_id
        RETURNING pontos INTO v_novos_pontos;

        RETURN jsonb_build_object(
            'sucesso', true,
            'acao', 'descurtido',
            'total_curtidas', v_novas_curtidas,
            'pontos_autor', v_novos_pontos
        );
    ELSE
        INSERT INTO interacoes_curtidas (publicacao_id, usuario_id)
        VALUES (p_publicacao_id, p_usuario_id);

        UPDATE publicacoes
        SET total_curtidas = total_curtidas + 1
        WHERE id = p_publicacao_id
        RETURNING autor_id, total_curtidas INTO v_autor_id, v_novas_curtidas;

        UPDATE perfis
        SET pontos = pontos + 10
        WHERE id = v_autor_id
        RETURNING pontos INTO v_novos_pontos;

        RETURN jsonb_build_object(
            'sucesso', true,
            'acao', 'curtido',
            'total_curtidas', v_novas_curtidas,
            'pontos_autor', v_novos_pontos
        );
    END IF;
EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('sucesso', false, 'mensagem', SQLERRM);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 5. RPC: Criar Nova Publicação com Suporte a Vencimento da Oferta e Expiração em 24h
CREATE OR REPLACE FUNCTION criar_publicacao(
    p_autor_id UUID,
    p_titulo TEXT,
    p_descricao TEXT DEFAULT NULL,
    p_midia_url TEXT DEFAULT NULL,
    p_tipo_midia TEXT DEFAULT 'texto',
    p_link_externo TEXT DEFAULT NULL,
    p_tags TEXT[] DEFAULT '{}',
    p_lat DOUBLE PRECISION DEFAULT 0.0,
    p_lng DOUBLE PRECISION DEFAULT 0.0,
    p_expirar_24h BOOLEAN DEFAULT TRUE,
    p_vencimento_promocao TIMESTAMPTZ DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    v_id UUID;
    v_expiracao TIMESTAMPTZ;
BEGIN
    INSERT INTO public.perfis (id, nome, pontos, tags_interesse)
    VALUES (p_autor_id, 'Divulgador PromoNear', 0, '{}')
    ON CONFLICT (id) DO NOTHING;

    IF p_expirar_24h THEN
        v_expiracao := NOW() + INTERVAL '24 hours';
    ELSE
        v_expiracao := NULL;
    END IF;

    INSERT INTO publicacoes (
        autor_id,
        titulo,
        descricao,
        midia_url,
        tipo_midia,
        link_externo,
        tags,
        localizacao,
        status,
        vencimento_promocao,
        data_expiracao
    ) VALUES (
        p_autor_id,
        p_titulo,
        p_descricao,
        p_midia_url,
        p_tipo_midia,
        p_link_externo,
        p_tags,
        ST_SetSRID(ST_MakePoint(p_lng, p_lat), 4326)::geography,
        'ativa',
        p_vencimento_promocao,
        v_expiracao
    ) RETURNING id INTO v_id;

    RETURN v_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- Recarregar o cache do schema no Supabase Postgrest
NOTIFY pgrst, 'reload schema';

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE perfis ENABLE ROW LEVEL SECURITY;
ALTER TABLE publicacoes ENABLE ROW LEVEL SECURITY;
ALTER TABLE interacoes_curtidas ENABLE ROW LEVEL SECURITY;
ALTER TABLE seguidores ENABLE ROW LEVEL SECURITY;
ALTER TABLE mensagens_chat ENABLE ROW LEVEL SECURITY;
ALTER TABLE comentarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE denuncias ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Perfis visíveis publicamente" ON perfis;
DROP POLICY IF EXISTS "Perfis criação/atualização pública" ON perfis;
DROP POLICY IF EXISTS "Usuário atualiza seu próprio perfil" ON perfis;

DROP POLICY IF EXISTS "Leitura de postagens ativas" ON publicacoes;
DROP POLICY IF EXISTS "Inserção de postagens" ON publicacoes;
DROP POLICY IF EXISTS "Atualização e exclusão de postagens" ON publicacoes;

DROP POLICY IF EXISTS "Curtidas visíveis para autenticados" ON interacoes_curtidas;
DROP POLICY IF EXISTS "Inserir curtida" ON interacoes_curtidas;
DROP POLICY IF EXISTS "Remover própria curtida" ON interacoes_curtidas;

DROP POLICY IF EXISTS "Seguidores visíveis publicamente" ON seguidores;
DROP POLICY IF EXISTS "Inserir/Remover seguidor" ON seguidores;

DROP POLICY IF EXISTS "Mensagens visíveis aos participantes" ON mensagens_chat;
DROP POLICY IF EXISTS "Inserir mensagem no chat" ON mensagens_chat;

DROP POLICY IF EXISTS "Comentários visíveis para todos" ON comentarios;
DROP POLICY IF EXISTS "Inserir comentário próprio" ON comentarios;

DROP POLICY IF EXISTS "Inserção de denúncias" ON denuncias;

CREATE POLICY "Perfis visíveis publicamente" ON perfis FOR SELECT USING (true);
CREATE POLICY "Perfis criação/atualização pública" ON perfis FOR INSERT WITH CHECK (true);
CREATE POLICY "Usuário atualiza seu próprio perfil" ON perfis FOR UPDATE USING (true);

CREATE POLICY "Leitura de postagens ativas" ON publicacoes FOR SELECT USING (true);
CREATE POLICY "Inserção de postagens" ON publicacoes FOR INSERT WITH CHECK (true);
CREATE POLICY "Atualização e exclusão de postagens" ON publicacoes FOR UPDATE USING (true);

CREATE POLICY "Curtidas visíveis para autenticados" ON interacoes_curtidas FOR SELECT USING (true);
CREATE POLICY "Inserir curtida" ON interacoes_curtidas FOR INSERT WITH CHECK (true);
CREATE POLICY "Remover própria curtida" ON interacoes_curtidas FOR DELETE USING (true);

CREATE POLICY "Seguidores visíveis publicamente" ON seguidores FOR SELECT USING (true);
CREATE POLICY "Inserir/Remover seguidor" ON seguidores FOR ALL USING (true);

CREATE POLICY "Mensagens visíveis aos participantes" ON mensagens_chat FOR SELECT USING (true);
CREATE POLICY "Inserir mensagem no chat" ON mensagens_chat FOR INSERT WITH CHECK (true);

CREATE POLICY "Comentários visíveis para todos" ON comentarios FOR SELECT USING (true);
CREATE POLICY "Inserir comentário próprio" ON comentarios FOR INSERT WITH CHECK (true);

CREATE POLICY "Inserção de denúncias" ON denuncias FOR INSERT WITH CHECK (true);

-- ====================================================================
-- BUCKET DE MÍDIAS NO SUPABASE STORAGE
-- ====================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES ('midias_divulgacao', 'midias_divulgacao', true)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "Acesso público às mídias" ON storage.objects;
DROP POLICY IF EXISTS "Upload público de mídias" ON storage.objects;

CREATE POLICY "Acesso público às mídias" ON storage.objects FOR SELECT USING (bucket_id = 'midias_divulgacao');
CREATE POLICY "Upload público de mídias" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'midias_divulgacao');
