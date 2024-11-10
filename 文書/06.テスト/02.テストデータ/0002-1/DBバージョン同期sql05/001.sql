Create Table db_version_a(
modify_count integer NOT NULL,
db_version integer ,
db_version_app integer ,
update_u_id integer ,
update_date timestamp ,
primary key (MODIFY_COUNT));

Create Table db_version_b(
modify_count integer NOT NULL,
db_version integer ,
db_version_app integer ,
update_u_id integer ,
update_date timestamp ,
primary key (MODIFY_COUNT));

COMMENT ON COLUMN db_version_a.modify_count IS '更新回数';
COMMENT ON COLUMN db_version_a.db_version IS 'DBバージョン';
COMMENT ON COLUMN db_version_a.db_version_app IS 'DBバージョンAPP';
COMMENT ON COLUMN db_version_a.update_u_id IS '更新者';
COMMENT ON COLUMN db_version_a.update_date IS '更新日時';

COMMENT ON COLUMN db_version_b.modify_count IS '更新回数';
COMMENT ON COLUMN db_version_b.db_version IS 'DBバージョン';
COMMENT ON COLUMN db_version_b.db_version_app IS 'DBバージョンAPP';
COMMENT ON COLUMN db_version_b.update_u_id IS '更新者';
COMMENT ON COLUMN db_version_b.update_date IS '更新日時';
