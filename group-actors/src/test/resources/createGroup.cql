CREATE TABLE IF NOT EXISTS sunbird.group (id text, name text, description text, activities map<text,text>, status text, membershipType text, createdBy text, createdOn timestamp, updatedBy text, updatedOn timestamp, PRIMARY KEY (id));

