-- Primary contact for Doc's first purchase
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (1, '9876543210', 'doc.chandrashekar@timetravel.com', NULL, 'PRIMARY', '2023-01-15 10:30:00', '2023-01-15 10:30:00', NULL);

-- Secondary contact - Doc used different email for second purchase
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (2, '9876543210', 'dr.doc@quantum.lab', 1, 'SECONDARY', '2023-02-20 14:45:00', '2023-02-20 14:45:00', NULL);

-- Secondary contact - Doc used different phone for third purchase
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (3, '8765432109', 'doc.chandrashekar@timetravel.com', 1, 'SECONDARY', '2023-03-10 09:15:00', '2023-03-10 09:15:00', NULL);

-- Another customer - Sarah Connor (unrelated to Doc)
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (4, '5551234567', 'sarah.connor@resistance.com', NULL, 'PRIMARY', '2023-01-25 16:20:00', '2023-01-25 16:20:00', NULL);

-- Sarah's secondary contact - different email
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (5, '5551234567', 's.connor@future.net', 4, 'SECONDARY', '2023-04-05 11:30:00', '2023-04-05 11:30:00', NULL);

-- Another customer - John Doe (simple case)
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (6, '1234567890', 'john.doe@example.com', NULL, 'PRIMARY', '2023-02-01 12:00:00', '2023-02-01 12:00:00', NULL);

-- Edge case - Contact with only email
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (7, NULL, 'mystery.person@anonymous.com', NULL, 'PRIMARY', '2023-03-15 18:45:00', '2023-03-15 18:45:00', NULL);

-- Edge case - Contact with only phone
INSERT INTO contacts (id, phone_number, email, linked_id, link_precedence, created_at, updated_at, deleted_at)
VALUES (8, '9999888777', NULL, NULL, 'PRIMARY', '2023-04-01 08:30:00', '2023-04-01 08:30:00', NULL);

-- Reset H2 auto-increment counter to start from 9
ALTER TABLE contacts ALTER COLUMN id RESTART WITH 9;