UPDATE notes
SET user_id = (SELECT id FROM users LIMIT 1);

ALTER TABLE notes ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE notes
ADD CONSTRAINT notes_user_fk
FOREIGN KEY (user_id)
REFERENCES users(id);
