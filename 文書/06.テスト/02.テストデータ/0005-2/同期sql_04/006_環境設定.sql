Create Table app_env_a(
modify_count integer NOT NULL,
dir_tmp varchar(300) ,
del_days_tmp integer ,
update_u_id integer ,
update_date timestamp ,
primary key (modify_count));

Create Table app_env_b(
modify_count integer NOT NULL,
dir_tmp varchar(300) ,
del_days_tmp integer ,
update_u_id integer ,
update_date timestamp ,
primary key (modify_count));

COMMENT ON COLUMN app_env_a.modify_count IS '更新回数';
COMMENT ON COLUMN app_env_a.dir_tmp IS 'tmpフォルダ';
COMMENT ON COLUMN app_env_a.del_days_tmp IS '[経過日数]tmpフォルダ削除';
COMMENT ON COLUMN app_env_a.update_u_id IS '更新者';
COMMENT ON COLUMN app_env_a.update_date IS '更新日時';

COMMENT ON COLUMN app_env_b.modify_count IS '更新回数';
COMMENT ON COLUMN app_env_b.dir_tmp IS 'tmpフォルダ';
COMMENT ON COLUMN app_env_b.del_days_tmp IS '[経過日数]tmpフォルダ削除';
COMMENT ON COLUMN app_env_b.update_u_id IS '更新者';
COMMENT ON COLUMN app_env_b.update_date IS '更新日時';


--LOGINユーザの権限
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE app_env_a TO kai9tmplpg;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE app_env_b TO kai9tmplpg;
