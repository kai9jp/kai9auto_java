Create Table sql_a(
sql_pk serial NOT NULL,
modify_count integer NOT NULL,
sql_name varchar(50) ,
sql text ,
memo text ,
update_u_id integer ,
update_date timestamp ,
delflg boolean ,
primary key (sql_pk,modify_count));

Create Table sql_b(
sql_pk serial NOT NULL,
modify_count integer NOT NULL,
sql_name varchar(50) ,
sql text ,
memo text ,
update_u_id integer ,
update_date timestamp ,
delflg boolean ,
primary key (sql_pk,modify_count));

COMMENT ON COLUMN sql_a.sql_pk IS 'ID';
COMMENT ON COLUMN sql_a.modify_count IS '更新回数';
COMMENT ON COLUMN sql_a.sql_name IS 'SQL名';
COMMENT ON COLUMN sql_a.sql IS 'SQL';
COMMENT ON COLUMN sql_a.memo IS '備考';
COMMENT ON COLUMN sql_a.update_u_id IS '更新者';
COMMENT ON COLUMN sql_a.update_date IS '更新日時';
COMMENT ON COLUMN sql_a.delflg IS '削除フラグ';

COMMENT ON COLUMN sql_b.sql_pk IS 'ID';
COMMENT ON COLUMN sql_b.modify_count IS '更新回数';
COMMENT ON COLUMN sql_b.sql_name IS 'SQL名';
COMMENT ON COLUMN sql_b.sql IS 'SQL';
COMMENT ON COLUMN sql_b.memo IS '備考';
COMMENT ON COLUMN sql_b.update_u_id IS '更新者';
COMMENT ON COLUMN sql_b.update_date IS '更新日時';
COMMENT ON COLUMN sql_b.delflg IS '削除フラグ';


--LOGINユーザの権限
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE sql_a TO kai9tmplpg;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE sql_b TO kai9tmplpg;

--シリアル型がある場合、シーケンスにも権限付与が必要
GRANT USAGE, SELECT, UPDATE ON SEQUENCE sql_a_sql_pk_seq TO kai9tmplpg;

--ユニークインデックス
CREATE UNIQUE INDEX sql_a_unqidx ON sql_a (sql_name);
