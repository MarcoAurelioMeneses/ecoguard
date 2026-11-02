-- PostGIS stores district geometries. pgvector stores future RAG embeddings.
-- Nothing in this baseline inserts geometries or vectors.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS vector;
