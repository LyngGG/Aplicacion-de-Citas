-- ─────────────────────────────────────────────────────────────────────────────
-- data.sql — Datos de prueba para H2
-- Se ejecuta automáticamente al arrancar la aplicación.
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Usuarios ─────────────────────────────────────────────────────────────────
INSERT INTO usuarios (id, email, password_hash, estado) VALUES
  (1, 'ana@upm.es',    'hash_ana',    'ACTIVO'),
  (2, 'carlos@upm.es', 'hash_carlos', 'ACTIVO'),
  (3, 'lucia@upm.es',  'hash_lucia',  'ACTIVO'),
  (4, 'mario@upm.es',  'hash_mario',  'ACTIVO');

-- ── Perfiles ─────────────────────────────────────────────────────────────────
INSERT INTO perfiles (id, usuario_id, nombre, edad, descripcion, ubicacion) VALUES
  (1, 1, 'Ana',    22, 'Me encanta el senderismo y la fotografía', 'Madrid'),
  (2, 2, 'Carlos', 25, 'Apasionado del fútbol y la música', 'Madrid'),
  (3, 3, 'Lucía',  23, 'Viajera empedernida y amante del cine', 'Madrid'),
  (4, 4, 'Mario',  27, 'Chef aficionado y lector compulsivo', 'Barcelona');

-- ── Intereses de cada perfil ──────────────────────────────────────────────────
INSERT INTO perfil_intereses (perfil_id, interes) VALUES
  (1, 'senderismo'), (1, 'fotografía'), (1, 'viajes'),
  (2, 'fútbol'),     (2, 'música'),     (2, 'videojuegos'),
  (3, 'cine'),       (3, 'viajes'),     (3, 'lectura'),
  (4, 'cocina'),     (4, 'lectura'),    (4, 'senderismo');

-- ── Fotos de perfil ───────────────────────────────────────────────────────────
INSERT INTO perfil_fotos (perfil_id, url_foto) VALUES
  (1, 'https://fake-cdn.com/ana1.jpg'),
  (2, 'https://fake-cdn.com/carlos1.jpg'),
  (3, 'https://fake-cdn.com/lucia1.jpg'),
  (4, 'https://fake-cdn.com/mario1.jpg');

-- ── Matches (Ana↔Carlos activo, Lucía↔Mario activo) ─────────────────────────
INSERT INTO matches (id, usuario1_id, usuario2_id, fecha_creacion, estado) VALUES
  (1, 1, 2, '2025-01-10 10:00:00', 'ACTIVO'),
  (2, 3, 4, '2025-01-11 12:00:00', 'ACTIVO');

-- ── Mensajes en el match Ana↔Carlos ──────────────────────────────────────────
INSERT INTO mensajes (id, match_id, remitente_id, texto, timestamp, leido) VALUES
  (1, 1, 1, '¡Hola Carlos! ¿Qué tal?',         '2025-01-10 10:05:00', true),
  (2, 1, 2, 'Ana! Todo bien, ¿y tú?',           '2025-01-10 10:06:00', true),
  (3, 1, 1, 'Genial, ¿te gusta el senderismo?', '2025-01-10 10:07:00', false);

-- ── Swipes ────────────────────────────────────────────────────────────────────
-- Ana hizo LIKE a Carlos, Carlos hizo LIKE a Ana (generó el match 1)
-- Lucía hizo LIKE a Mario, Mario hizo LIKE a Lucía (generó el match 2)
-- Ana hizo DISLIKE a Mario (no aparecerá en su descubrimiento)
INSERT INTO swipes (id, remitente_id, destinatario_id, accion, timestamp) VALUES
  (1, 1, 2, 'ACEPTADO',  '2025-01-10 09:50:00'),
  (2, 2, 1, 'ACEPTADO',  '2025-01-10 09:55:00'),
  (3, 3, 4, 'ACEPTADO',  '2025-01-11 11:00:00'),
  (4, 4, 3, 'ACEPTADO',  '2025-01-11 11:30:00'),
  (5, 1, 4, 'RECHAZADO', '2025-01-10 09:48:00');
