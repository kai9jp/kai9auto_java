Create Table m_keyword2_a(
modify_count integer NOT NULL,
no integer ,
keyword varchar(50) NOT NULL,
class_name varchar(100) NOT NULL,
func_name varchar(100) NOT NULL,
ok_result varchar(100) NOT NULL,
ng_result varchar(100) NOT NULL,
param1 varchar(100) ,
param2 varchar(100) ,
param3 varchar(100) ,
variable1 varchar(100) ,
bikou text ,
primary key (modify_count,keyword));

Create Table m_keyword2_b(
modify_count integer NOT NULL,
no integer ,
keyword varchar(50) NOT NULL,
class_name varchar(100) NOT NULL,
func_name varchar(100) NOT NULL,
ok_result varchar(100) NOT NULL,
ng_result varchar(100) NOT NULL,
param1 varchar(100) ,
param2 varchar(100) ,
param3 varchar(100) ,
variable1 varchar(100) ,
bikou text ,
primary key (modify_count,keyword));

COMMENT ON COLUMN m_keyword2_a.modify_count IS '更新回数';
COMMENT ON COLUMN m_keyword2_a.no IS 'NO';
COMMENT ON COLUMN m_keyword2_a.keyword IS 'キーワード';
COMMENT ON COLUMN m_keyword2_a.class_name IS 'クラス名';
COMMENT ON COLUMN m_keyword2_a.func_name IS '関数名';
COMMENT ON COLUMN m_keyword2_a.ok_result IS 'OK文言';
COMMENT ON COLUMN m_keyword2_a.ng_result IS 'NG文言';
COMMENT ON COLUMN m_keyword2_a.param1 IS '第1引数';
COMMENT ON COLUMN m_keyword2_a.param2 IS '第2引数';
COMMENT ON COLUMN m_keyword2_a.param3 IS '第3引数';
COMMENT ON COLUMN m_keyword2_a.variable1 IS '変数';
COMMENT ON COLUMN m_keyword2_a.bikou IS '備考';

COMMENT ON COLUMN m_keyword2_b.modify_count IS '更新回数';
COMMENT ON COLUMN m_keyword2_b.no IS 'NO';
COMMENT ON COLUMN m_keyword2_b.keyword IS 'キーワード';
COMMENT ON COLUMN m_keyword2_b.class_name IS 'クラス名';
COMMENT ON COLUMN m_keyword2_b.func_name IS '関数名';
COMMENT ON COLUMN m_keyword2_b.ok_result IS 'OK文言';
COMMENT ON COLUMN m_keyword2_b.ng_result IS 'NG文言';
COMMENT ON COLUMN m_keyword2_b.param1 IS '第1引数';
COMMENT ON COLUMN m_keyword2_b.param2 IS '第2引数';
COMMENT ON COLUMN m_keyword2_b.param3 IS '第3引数';
COMMENT ON COLUMN m_keyword2_b.variable1 IS '変数';
COMMENT ON COLUMN m_keyword2_b.bikou IS '備考';
