Create Table m_env_a(
modify_count integer NOT NULL,
dir_parametersheet varchar(300) ,
dir_processingscenario varchar(300) ,
dir_processedscenario varchar(300) ,
dir_testdata varchar(300) ,
dir_retentionperiod varchar(300) ,
dir_generationmanagement varchar(300) ,
dir_webdriver varchar(300) ,
dir_tmp varchar(300) ,
dir_testdataabbreviation varchar(10) ,
dir_retentionperiodabbreviation varchar(10) ,
dir_tmpabbreviation varchar(10) ,
del_days_tmp integer ,
del_days_retentionperiod integer ,
del_days_generationmanagement integer ,
del_days_processedscenario integer ,
del_days_log integer ,
del_days_processhistory integer ,
del_days_historyrecord integer ,
num_gm integer ,
timeout_m integer ,
log_cut integer ,
update_u_id integer ,
update_date timestamp ,
tc_svn_update boolean ,
primary key (MODIFY_COUNT));

Create Table m_env_b(
modify_count integer NOT NULL,
dir_parametersheet varchar(300) ,
dir_processingscenario varchar(300) ,
dir_processedscenario varchar(300) ,
dir_testdata varchar(300) ,
dir_retentionperiod varchar(300) ,
dir_generationmanagement varchar(300) ,
dir_webdriver varchar(300) ,
dir_tmp varchar(300) ,
dir_testdataabbreviation varchar(10) ,
dir_retentionperiodabbreviation varchar(10) ,
dir_tmpabbreviation varchar(10) ,
del_days_tmp integer ,
del_days_retentionperiod integer ,
del_days_generationmanagement integer ,
del_days_processedscenario integer ,
del_days_log integer ,
del_days_processhistory integer ,
del_days_historyrecord integer ,
num_gm integer ,
timeout_m integer ,
log_cut integer ,
update_u_id integer ,
update_date timestamp ,
tc_svn_update boolean ,
primary key (MODIFY_COUNT));

COMMENT ON COLUMN m_env_a.modify_count IS '更新回数';
COMMENT ON COLUMN m_env_a.dir_parametersheet IS 'DIR_パラメータシート';
COMMENT ON COLUMN m_env_a.dir_processingscenario IS 'DIR_処理シナリオ';
COMMENT ON COLUMN m_env_a.dir_processedscenario IS 'DIR_処理済シナリオ';
COMMENT ON COLUMN m_env_a.dir_testdata IS 'DIR_テストデータ';
COMMENT ON COLUMN m_env_a.dir_retentionperiod IS 'DIR_一定期間保存';
COMMENT ON COLUMN m_env_a.dir_generationmanagement IS 'DIR_世代管理';
COMMENT ON COLUMN m_env_a.dir_webdriver IS 'DIR_WebDriver';
COMMENT ON COLUMN m_env_a.dir_tmp IS 'DIR_TMP';
COMMENT ON COLUMN m_env_a.dir_testdataabbreviation IS 'DIR_テストデータ略称';
COMMENT ON COLUMN m_env_a.dir_retentionperiodabbreviation IS 'DIR_一定期間保存略称';
COMMENT ON COLUMN m_env_a.dir_tmpabbreviation IS 'DIR_TMP略称';
COMMENT ON COLUMN m_env_a.del_days_tmp IS '[経過日数]tmpフォルダ削除';
COMMENT ON COLUMN m_env_a.del_days_retentionperiod IS '[経過日数]一定期間保存';
COMMENT ON COLUMN m_env_a.del_days_generationmanagement IS '[経過日数]世代管理';
COMMENT ON COLUMN m_env_a.del_days_processedscenario IS '[経過日数]処理済シナリオ';
COMMENT ON COLUMN m_env_a.del_days_log IS '[経過日数]ログ';
COMMENT ON COLUMN m_env_a.del_days_processhistory IS '[経過日数]処理履歴';
COMMENT ON COLUMN m_env_a.del_days_historyrecord IS '[経過日数]履歴レコード';
COMMENT ON COLUMN m_env_a.num_gm IS '世代管理数';
COMMENT ON COLUMN m_env_a.timeout_m IS 'タイムアウト分数';
COMMENT ON COLUMN m_env_a.log_cut IS 'ログ打ち切り行数';
COMMENT ON COLUMN m_env_a.update_u_id IS '更新者';
COMMENT ON COLUMN m_env_a.update_date IS '更新日時';
COMMENT ON COLUMN m_env_a.tc_svn_update IS '自動SVN更新';

COMMENT ON COLUMN m_env_b.modify_count IS '更新回数';
COMMENT ON COLUMN m_env_b.dir_parametersheet IS 'DIR_パラメータシート';
COMMENT ON COLUMN m_env_b.dir_processingscenario IS 'DIR_処理シナリオ';
COMMENT ON COLUMN m_env_b.dir_processedscenario IS 'DIR_処理済シナリオ';
COMMENT ON COLUMN m_env_b.dir_testdata IS 'DIR_テストデータ';
COMMENT ON COLUMN m_env_b.dir_retentionperiod IS 'DIR_一定期間保存';
COMMENT ON COLUMN m_env_b.dir_generationmanagement IS 'DIR_世代管理';
COMMENT ON COLUMN m_env_b.dir_webdriver IS 'DIR_WebDriver';
COMMENT ON COLUMN m_env_b.dir_tmp IS 'DIR_TMP';
COMMENT ON COLUMN m_env_b.dir_testdataabbreviation IS 'DIR_テストデータ略称';
COMMENT ON COLUMN m_env_b.dir_retentionperiodabbreviation IS 'DIR_一定期間保存略称';
COMMENT ON COLUMN m_env_b.dir_tmpabbreviation IS 'DIR_TMP略称';
COMMENT ON COLUMN m_env_b.del_days_tmp IS '[経過日数]tmpフォルダ削除';
COMMENT ON COLUMN m_env_b.del_days_retentionperiod IS '[経過日数]一定期間保存';
COMMENT ON COLUMN m_env_b.del_days_generationmanagement IS '[経過日数]世代管理';
COMMENT ON COLUMN m_env_b.del_days_processedscenario IS '[経過日数]処理済シナリオ';
COMMENT ON COLUMN m_env_b.del_days_log IS '[経過日数]ログ';
COMMENT ON COLUMN m_env_b.del_days_processhistory IS '[経過日数]処理履歴';
COMMENT ON COLUMN m_env_b.del_days_historyrecord IS '[経過日数]履歴レコード';
COMMENT ON COLUMN m_env_b.num_gm IS '世代管理数';
COMMENT ON COLUMN m_env_b.timeout_m IS 'タイムアウト分数';
COMMENT ON COLUMN m_env_b.log_cut IS 'ログ打ち切り行数';
COMMENT ON COLUMN m_env_b.update_u_id IS '更新者';
COMMENT ON COLUMN m_env_b.update_date IS '更新日時';
COMMENT ON COLUMN m_env_b.tc_svn_update IS '自動SVN更新';
