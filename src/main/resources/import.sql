-- ============================================
-- ITENS (Exemplo de transformação)
-- ============================================
INSERT INTO tb_items (id, name, price) VALUES (1, 'Notebook Dell Inspiron 15', 3499.90);
INSERT INTO tb_items (id, name, price) VALUES (2, 'Mouse Logitech MX Master 3', 549.90);
INSERT INTO tb_items (id, name, price) VALUES (3, 'Teclado Mecânico Keychron K2', 699.90);
-- ... repetir para os demais itens

-- ============================================
-- CLIENTES (Exemplo de transformação)
-- ============================================
INSERT INTO tb_clients (id, name, address) VALUES (1, 'Ana Silva Santos', 'Rua das Flores, 123 - São Paulo, SP');
INSERT INTO tb_clients (id, name, address) VALUES (2, 'Bruno Costa Oliveira', 'Av. Paulista, 1500 - São Paulo, SP');
INSERT INTO tb_clients (id, name, address) VALUES (3, 'Carla Mendes Souza', 'Rua Augusta, 789 - São Paulo, SP');
-- ... repetir para os 400 clientes

-- ============================================
-- ORDENS (Exemplo de transformação)
-- ============================================
INSERT INTO tb_orders (id, date, total_value, client_id) VALUES (1, '2024-11-05', 3923.63, 1);
INSERT INTO tb_orders (id, date, total_value, client_id) VALUES (2, '2024-11-05', 1680.67, 2);
INSERT INTO tb_orders (id, date, total_value, client_id) VALUES (3, '2024-11-05', 1559.4, 3);
-- ... repetir para as 10.100+ ordens

-- ============================================
-- RELAÇÃO ORDENS E ITENS
-- ============================================
INSERT INTO tb_orders_items (order_id, items_id) VALUES (1, 1);
INSERT INTO tb_orders_items (order_id, items_id) VALUES (2, 2);
INSERT INTO tb_orders_items (order_id, items_id) VALUES (3, 3);
-- ... repetir para todos os vínculos
--
--

SELECT setval('tb_clients_id_seq', (SELECT MAX(id) FROM tb_clients));
