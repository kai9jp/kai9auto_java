Create Table TABLE1_A(
id integer NOT NULL,
modify_count integer NOT NULL,
str1 varchar(100) ,
str2 text ,
int1 integer ,
int2 integer ,
date1 date ,
flg1 boolean ,
delflg boolean ,
update_u_id integer ,
update_date timestamp ,
primary key (id,MODIFY_COUNT));

Create Table TABLE1_B(
id integer NOT NULL,
modify_count integer NOT NULL,
str1 varchar(100) ,
str2 text ,
int1 integer ,
int2 integer ,
date1 date ,
flg1 boolean ,
delflg boolean ,
update_u_id integer ,
update_date timestamp ,
primary key (id,MODIFY_COUNT));


COMMENT ON COLUMN TABLE1_A.id IS 'ID';
COMMENT ON COLUMN TABLE1_A.modify_count IS '更新回数';
COMMENT ON COLUMN TABLE1_A.str1 IS '文字列1';
COMMENT ON COLUMN TABLE1_A.str2 IS '文字列2';
COMMENT ON COLUMN TABLE1_A.int1 IS '数値1';
COMMENT ON COLUMN TABLE1_A.int2 IS '数値2';
COMMENT ON COLUMN TABLE1_A.date1 IS '日付1';
COMMENT ON COLUMN TABLE1_A.flg1 IS 'フラグ1';
COMMENT ON COLUMN TABLE1_A.delflg IS '削除フラグ';
COMMENT ON COLUMN TABLE1_A.update_u_id IS '更新者';
COMMENT ON COLUMN TABLE1_A.update_date IS '更新日時';

COMMENT ON COLUMN TABLE1_B.id IS 'ID';
COMMENT ON COLUMN TABLE1_B.modify_count IS '更新回数';
COMMENT ON COLUMN TABLE1_B.str1 IS '文字列1';
COMMENT ON COLUMN TABLE1_B.str2 IS '文字列2';
COMMENT ON COLUMN TABLE1_B.int1 IS '数値1';
COMMENT ON COLUMN TABLE1_B.int2 IS '数値2';
COMMENT ON COLUMN TABLE1_B.date1 IS '日付1';
COMMENT ON COLUMN TABLE1_B.flg1 IS 'フラグ1';
COMMENT ON COLUMN TABLE1_B.delflg IS '削除フラグ';
COMMENT ON COLUMN TABLE1_B.update_u_id IS '更新者';
COMMENT ON COLUMN TABLE1_B.update_date IS '更新日時';
