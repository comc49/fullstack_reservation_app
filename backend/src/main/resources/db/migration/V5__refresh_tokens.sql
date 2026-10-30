-- V5: refresh tokens for sign-in sessions.
-- Each sign-in starts a family (a chain of tokens). Every refresh marks the old
-- token used and issues a new one in the same family. If a used token comes back,
-- it was copied, so the whole family is revoked.
-- Only a SHA-256 hash of each token is stored, so a copy of the database
-- contains no working tokens.


CREATE TABLE refresh_tokens (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  person_id bigint NOT NULL REFERENCES people (id),
  token_hash text	NOT NULL UNIQUE,
  family_id	uuid	NOT NULL,
  created_at	timestamptz	NOT NULL DEFAULT now(),
  expires_at	timestamptz	NOT NULL,
  used_at	timestamptz,
  revoked_at	timestamptz
);

-- Theft detection: revoke every token in one sign-in's chain.
CREATE INDEX refresh_tokens_family_id_idx ON refresh_tokens (family_id);

-- Log out everywhere: revoke every token belonging to one person.
CREATE INDEX refresh_tokens_person_id_idx ON refresh_tokens (person_id);