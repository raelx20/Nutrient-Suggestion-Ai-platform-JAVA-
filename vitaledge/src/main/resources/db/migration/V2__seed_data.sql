-- Seed data: roles, permissions, default questionnaire + questions, sample products.

-- ============ Permissions ============
INSERT INTO permissions (id, name, description, is_active, created_at, updated_at) VALUES
  ('10000000-0000-0000-0000-000000000001', 'product.view', 'View product catalog', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000002', 'product.manage', 'Create, update and delete products', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000003', 'assessment.view', 'View assessments and results', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000004', 'assessment.manage', 'Manage assessments', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000005', 'conversation.manage', 'Manage conversations', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000006', 'user.manage', 'Manage users', TRUE, now(), now()),
  ('10000000-0000-0000-0000-000000000007', 'audit.view', 'View audit logs', TRUE, now(), now());

-- ============ Roles ============
INSERT INTO roles (id, name, role_type, description, is_active, created_at, updated_at) VALUES
  ('20000000-0000-0000-0000-000000000001', 'CONSUMER', 'consumer', 'Regular consumer user', TRUE, now(), now()),
  ('20000000-0000-0000-0000-000000000002', 'ADMIN', 'admin', 'Platform administrator', TRUE, now(), now()),
  ('20000000-0000-0000-0000-000000000003', 'COUNSELLOR', 'counsellor', 'Nutrition counsellor', TRUE, now(), now());

-- ============ Role-permissions ============
INSERT INTO role_permissions (role_id, permission_id) VALUES
  ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001'),
  ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000006'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000007'),
  ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001'),
  ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
  ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005');

-- ============ Questionnaire ============
INSERT INTO questionnaires (id, code, title, description, is_default, is_active, version, question_ids, created_at, updated_at)
VALUES ('30000000-0000-0000-0000-000000000001', 'q_health_baseline', 'Health Baseline Assessment',
        'Baseline nutrition and health assessment', TRUE, TRUE, 1,
        '["31000000-0000-0000-0000-000000000001","31000000-0000-0000-0000-000000000002","31000000-0000-0000-0000-000000000003","31000000-0000-0000-0000-000000000004","31000000-0000-0000-0000-000000000005","31000000-0000-0000-0000-000000000006","31000000-0000-0000-0000-000000000007","31000000-0000-0000-0000-000000000008","31000000-0000-0000-0000-000000000009","31000000-0000-0000-0000-000000000010","31000000-0000-0000-0000-000000000011","31000000-0000-0000-0000-000000000012","31000000-0000-0000-0000-000000000013","31000000-0000-0000-0000-000000000014","31000000-0000-0000-0000-000000000015","31000000-0000-0000-0000-000000000016"]',
        now(), now());

-- ============ Questions ============
INSERT INTO questions (id, questionnaire_id, question_key, text, question_type, options, question_order, is_required, is_active, validation_rules, created_at, updated_at) VALUES
  ('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'age_group', 'What is your age group?', 'select', '["children","adults","seniors"]', 1, TRUE, TRUE, '{"required":true}', now(), now()),
  ('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'gender', 'What is your gender?', 'select', '["male","female","other"]', 2, TRUE, TRUE, '{"required":true}', now(), now()),
  ('31000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'weight_kg', 'What is your weight in kilograms?', 'number', NULL, 3, TRUE, TRUE, '{"min":2,"max":300}', now(), now()),
  ('31000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', 'height_cm', 'What is your height in centimeters?', 'number', NULL, 4, TRUE, TRUE, '{"min":50,"max":250}', now(), now()),
  ('31000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000001', 'activity_level', 'What is your activity level?', 'select', '["sedentary","light","moderate","active","very_active"]', 5, TRUE, TRUE, '{"required":true}', now(), now()),
  ('31000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000001', 'health_goals', 'What are your health goals?', 'multi_select', '["weight_loss","weight_gain","muscle_gain","general_health","digestive_health","immunity"]', 6, TRUE, TRUE, '{"min_selections":1}', now(), now()),
  ('31000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000001', 'dietary_preferences', 'Any dietary preferences?', 'multi_select', '["vegetarian","vegan","gluten_free","dairy_free","balanced","high_protein","low_calorie"]', 7, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000001', 'allergies', 'Do you have any food allergies?', 'multi_select', '["peanuts","tree_nuts","dairy","eggs","gluten","soy","shellfish","none"]', 8, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000001', 'medical_conditions', 'Any medical conditions?', 'multi_select', '["diabetes","hypertension","heart_disease","thyroid","none"]', 9, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000001', 'pregnant_or_nursing', 'Are you pregnant or nursing?', 'boolean', NULL, 10, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000001', 'chronic_diseases', 'Any chronic diseases?', 'multi_select', '["diabetes","hypertension","heart_disease","asthma","other","none"]', 11, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000001', 'blood_group', 'What is your blood group?', 'select', '["A+","A-","B+","B-","AB+","AB-","O+","O-"]', 12, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000001', 'sleep_hours', 'How many hours of sleep do you get per night?', 'number', NULL, 13, FALSE, TRUE, '{"min":0,"max":24}', now(), now()),
  ('31000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000001', 'stress_level', 'What is your typical stress level?', 'select', '["low","moderate","high","very_high"]', 14, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000015', '30000000-0000-0000-0000-000000000001', 'smoker', 'Do you smoke?', 'boolean', NULL, 15, FALSE, TRUE, '{}', now(), now()),
  ('31000000-0000-0000-0000-000000000016', '30000000-0000-0000-0000-000000000001', 'alcohol', 'Do you consume alcohol?', 'boolean', NULL, 16, FALSE, TRUE, '{}', now(), now());

-- ============ Products ============
INSERT INTO products (id, name, sku, category_code, category, age_group, status, description, allergens, dietary_tags, nutritional_info, created_at, updated_at) VALUES
  ('40000000-0000-0000-0000-000000000001', 'Whey Protein Isolate', 'SKU-0001', 'SPORTS', 'high', 'adults', 'ACTIVE', 'High-protein whey isolate for muscle recovery.', '["dairy"]', '["high_protein","muscle_gain","weight_gain"]', '{"protein_per_100g":85,"calories_per_100g":380}', now(), now()),
  ('40000000-0000-0000-0000-000000000002', 'Daily Multi-Vitamin', 'SKU-0002', 'SUPPLEMENTS', 'medium', 'all', 'ACTIVE', 'Comprehensive daily multivitamin.', '[]', '["balanced","immunity","general_health"]', '{"vitamin_d":1000,"vitamin_c":120}', now(), now()),
  ('40000000-0000-0000-0000-000000000003', 'Omega-3 Fish Oil', 'SKU-0003', 'SUPPLEMENTS', 'medium', 'adults', 'ACTIVE', 'Omega-3 fatty acids for heart and brain health.', '["fish"]', '["general_health","heart_health"]', '{"epa_mg":500,"dha_mg":250}', now(), now()),
  ('40000000-0000-0000-0000-000000000004', 'Low-Calorie Meal Replacement', 'SKU-0004', 'MEAL_REPLACEMENT', 'low', 'adults', 'ACTIVE', 'Balanced meal replacement shake for weight management.', '["dairy"]', '["low_calorie","weight_loss","balanced"]', '{"calories_per_serving":200,"protein_per_serving":20}', now(), now()),
  ('40000000-0000-0000-0000-000000000005', 'Probiotic Digestive Support', 'SKU-0005', 'SUPPLEMENTS', 'medium', 'adults', 'ACTIVE', 'Probiotic blend supporting digestive health.', '[]', '["digestive_health","probiotic","general_health"]', '{"cfu":10000000000,"strains":6}', now(), now()),
  ('40000000-0000-0000-0000-000000000006', 'Vitamin C Immunity Boost', 'SKU-0006', 'SUPPLEMENTS', 'medium', 'all', 'ACTIVE', 'Vitamin C with zinc for immune support.', '[]', '["immunity","general_health","vitamin_c"]', '{"vitamin_c_mg":500,"zinc_mg":10}', now(), now()),
  ('40000000-0000-0000-0000-000000000007', 'High-Fiber Breakfast Cereal', 'SKU-0007', 'FOOD', 'medium', 'all', 'ACTIVE', 'High-fiber wholegrain cereal.', '["gluten"]', '["digestive_health","balanced","weight_loss"]', '{"fiber_per_100g":12,"calories_per_100g":330}', now(), now()),
  ('40000000-0000-0000-0000-000000000008', 'Balanced Nutrition Shake', 'SKU-0008', 'MEAL_REPLACEMENT', 'medium', 'adults', 'ACTIVE', 'Everyday balanced nutrition shake.', '["soy"]', '["balanced","general_health","high_protein"]', '{"calories_per_serving":250,"protein_per_serving":22}', now(), now()),
  ('40000000-0000-0000-0000-000000000009', 'Kids Calcium Chewables', 'SKU-0009', 'SUPPLEMENTS', 'medium', 'children', 'ACTIVE', 'Calcium and vitamin D chewables for children.', '[]', '["balanced","general_health","children"]', '{"calcium_mg":300,"vitamin_d":400}', now(), now()),
  ('40000000-0000-0000-0000-000000000010', 'Senior Protein Blend', 'SKU-0010', 'SPORTS', 'high', 'seniors', 'ACTIVE', 'Protein blend formulated for senior nutrition.', '["dairy"]', '["high_protein","muscle_gain","seniors"]', '{"protein_per_100g":75,"calories_per_100g":360}', now(), now());

-- ============ Product rules ============
INSERT INTO product_rules (id, product_id, rule_definition, is_active, created_at, updated_at) VALUES
  ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '{"not_for_age_groups":["children"],"allergens":["dairy"]}', TRUE, now(), now()),
  ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '{"medical_exclusions":["diabetes"],"allergens":["dairy"]}', TRUE, now(), now()),
  ('50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000009', '{"not_for_age_groups":["adults","seniors"]}', TRUE, now(), now()),
  ('50000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000003', '{"exclude_if_pregnant":true,"allergens":["fish"]}', TRUE, now(), now());

-- ============ AI configuration ============
INSERT INTO ai_configurations (id, model_name, parameters, version, is_active, created_at, updated_at) VALUES
  ('60000000-0000-0000-0000-000000000001', 'vitaledge-fallback', '{"provider":"fallback","temperature":0.7,"max_tokens":600}', 1, TRUE, now(), now());