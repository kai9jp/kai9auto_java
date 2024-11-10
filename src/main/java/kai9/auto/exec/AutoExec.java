package kai9.auto.exec;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import kai9.libs.Kai9Utils;
import kai9.auto.model.scheduler;
import kai9.auto.model.syori_queue;
import kai9.auto.service.syori_queue_Service;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

//アプリ起動時に動く(ApplicationRunner)
//時間トリガで自動実行させる機能
@Component
public class AutoExec implements ApplicationRunner {

    private static WebClient webClient;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    private syori_queue_Service syori_queue_service;

    @Autowired
    @Qualifier("commonjdbc")
    JdbcTemplate jdbcTemplate_com;

    private static String kai9secretKey;

    // 実行中の処理を管理
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    // スケジューラ解析用
    private static final String[] JAPANESE_DAYS = { "月", "火", "水", "木", "金", "土", "日" };

    // 処理排他の強制ロック解除時間
    private static final int LOCK_TIMEOUT_MINUTES = 1;

    // コンストラクタでAPI実行用の各部品を生成(Export_s_excel.javaにも同じ実装有り。変更時は揃える事)
    public AutoExec(WebClient.Builder webClientBuilder, @Value("${server.port}")
    int serverPort, @Value("${jwt.secretKey}")
    String secretKey) throws SSLException {
        // 初回のみ初期化
        if (webClient == null) {
            synchronized (AutoExec.class) {
                if (webClient == null) {
                    webClient = createWebClient(webClientBuilder, serverPort);
                }
            }
        }
        kai9secretKey = secretKey;
    }

    private static WebClient createWebClient(WebClient.Builder webClientBuilder, int serverPort) throws SSLException {
        // SslContextオブジェクトを作成する
        SslContext sslContext = SslContextBuilder.forClient()
                // サーバー証明書の検証を行わないために信頼できないTrustManagerFactoryを指定する
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        // HttpClientオブジェクトを作成する
        HttpClient httpClient = HttpClient.create()
                // HTTPSを使用するためにsecureメソッドを呼び出し、sslContextを指定する
                .secure(ssl -> ssl.sslContext(sslContext));

        // 環境
        boolean isDevelop = Boolean.valueOf(Kai9Utils.getPropertyFromYaml("kai9.is_develop"));
        boolean isPortChange = Boolean.valueOf(Kai9Utils.getPropertyFromYaml("kai9.is_port_change"));

        // ドメイン
        String domain = Kai9Utils.getPropertyFromYaml("jwt.domain");

        // tomcatの実行環境と、Eclipse内包のtomcat環境とで、ベースURLを変更する
        // 又は、ポートがデフォルトの443から変えられてる場合は、application.ymlのportを適用させる
        if (isDevelop || isPortChange) {
            domain = "https://" + domain + ":" + serverPort;
        } else {
            domain = "https://" + domain;
        }

        // WebClientオブジェクトを作成する
        return webClientBuilder
                // HttpClientオブジェクトを使用するようにClientHttpConnectorを設定する
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // ベースURLを設定する
                .baseUrl(domain)
                .build();
    }

    // 起動イベント
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // アプリケーション起動後に実行したい処理をここに記述
        Kai9Utils.makeLog("info", "自動実行モード:起動に成功しました", this.getClass());

        try {
            // 自ホスト名を取得
            String localHost = InetAddress.getLocalHost().getHostName().toUpperCase();

            // ------------------------------------------------
            // 起動時に実行指定されている処理番号をキック
            // ------------------------------------------------
            // スレッドプールを作成
            String startUp_S1id = String.valueOf(Kai9Utils.getPropertyFromYaml("kai9.startUp_S1id"));
            List<String> startUp_S1ids = Arrays.asList(startUp_S1id.split(","));
            for (String item : startUp_S1ids) {
                String finalItem = item;
                executor.submit(() -> {
                    // 非同期処理として実行
                    callAPI(Integer.valueOf(finalItem), "", "", 0, "", jdbcTemplate,jdbcTemplate_com, false, "");
                });
            }

            // ------------------------------------------------
            // (毎分)定期的に処理を検索し、実行ホストが空、又は、自分の場合、実行する
            // ------------------------------------------------
            Runnable taskCheckAndExec = () -> {
                try {

                    // syori1_aのrun_timingおよびs1_idを取得
                    String sql = "SELECT run_timing, s1_id FROM syori1_a "
                            // 削除レコードを除く
                            + "WHERE delflg = false "
                    // 処理設定_親のrun_hostが空、又は、パラメータで渡されたrun_hostと一致すること
                            + "AND (run_host = '' OR UPPER(run_host) = UPPER(:run_host))";

                    MapSqlParameterSource param = new MapSqlParameterSource().addValue("run_host", localHost);
                    List<Map<String, Object>> runTimings = namedJdbcTemplate.queryForList(sql, param);

                    // 各レコードを処理
                    for (Map<String, Object> row : runTimings) {
                        String runTiming = (String) row.get("run_timing");
                        Integer s1_id = (Integer) row.get("s1_id");
                        String[] scheduleLabels = runTiming.split("\n");

                        for (String label : scheduleLabels) {
                            if (label.isEmpty()) continue;

                            // スケジューラと現在時刻が一致するか判定
                            LocalDate currentDate = LocalDate.now();
                            LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                            boolean isMatch = isScheduledMatch(label, currentDate, currentTime,null);

                            // スケジュールが一致する場合、処理を実行
                            if (isMatch) {
                                executor.submit(() -> {
                                    try {
                                        callAPI(s1_id, "", "", 0, "", jdbcTemplate,jdbcTemplate_com, false, "");
                                    } catch (Exception e) {
                                        Kai9Utils.makeLog("error", "自動実行モード:callAPIの実行に失敗しました," + Kai9Utils.processExceptionMessages(e), AutoExec.class);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Kai9Utils.makeLog("error", "taskCheckAndExec処理中にエラーが発生しました" + Kai9Utils.processExceptionMessages(e), this.getClass());
                }
            };

            // cronでタスクチェックを毎分実行
            taskScheduler.schedule(taskCheckAndExec, new CronTrigger("0 * * * * *")); // 毎分実行
        } catch (Exception e) {
            Kai9Utils.makeLog("error", "起動中にエラーが発生しました(runメソッド)。" + Kai9Utils.processExceptionMessages(e), this.getClass());
        }
    }

    // 毎秒実行されるスケジューラタスク
    @Scheduled(fixedRate = 1000)
    public void performTask() throws UnknownHostException {
        // 処理キューを検索し、自ホストの処理が有れば実行する
        String localHost = InetAddress.getLocalHost().getHostName().toString().toUpperCase();
        try {
            String sql = "SELECT * FROM syori_queue WHERE UPPER(run_host) = UPPER(:run_host) ORDER BY update_date ASC LIMIT 1";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("run_host", localHost);
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            syori_queue syori_queue = namedTemplate.queryForObject(
                    sql,
                    paramMap,
                    new BeanPropertyRowMapper<syori_queue>(syori_queue.class));
            // 処理キューを削除
            syori_queue_service.delete(syori_queue);

            // APIコール
            callAPI(syori_queue.getS1_id(), syori_queue.getS2_ids(), syori_queue.getS3_ids(), syori_queue.getUpdate_u_id(), "", jdbcTemplate,jdbcTemplate_com, false, "");
        } catch (EmptyResultDataAccessException e) {
            // レコードがヒットしなかった場合、何もしない
            return;
        }
    }

    // 終了イベント
    @PreDestroy
    public void onDestroy() {
        Kai9Utils.makeLog("info", "自動実行モード:停止:アプリケーションの終了イベントが発生しました", this.getClass());

        // 自動スタートアップ処理用(スレッドプールのシャットダウン処理)
        Kai9Utils.makeLog("info", "自動実行モード:停止:スレッドプールをシャットダウンします", this.getClass());
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                Kai9Utils.makeLog("warn", "自動実行モード:停止:「処理」のスレッドが時間内に終了しなかったため強制終了します", this.getClass());
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Kai9Utils.makeLog("error", "自動実行モード:停止:「処理」のスレッドプールをシャットダウン中にエラーが発生しました"+Kai9Utils.processExceptionMessages(e), this.getClass());
            executor.shutdownNow();
        }
    }

    // APIコール
    @SuppressWarnings("deprecation")
    public static void callAPI(int s1_id, String s2Ids, String s3Ids, int run_u_id, String execute_uuid, JdbcTemplate jdbcTemplate, JdbcTemplate jdbcTemplate_com, Boolean s_linking_ng, String optionalParam) {
        try {
            String runHost;
            try {
                // ホスト名を取得
                runHost = InetAddress.getLocalHost().getHostName().toUpperCase();
            } catch (UnknownHostException e) {
                Kai9Utils.makeLog("error", "ホスト名の取得に失敗しました: " + Kai9Utils.processExceptionMessages(e), AutoExec.class);
                return;
            }

            // 排他チェックとロック取得
            boolean isLocked = acquireExclusiveLock(s1_id, runHost, run_u_id, jdbcTemplate);
            if (!isLocked) {
                // 排他されている場合は処理せず抜ける
                Kai9Utils.makeLog("info", "処理がすでに他ノードで実行中のためスキップされました:処理1No= " + s1_id, AutoExec.class);
                return;
            }
            
            try {
                String login_id = "";
                if (run_u_id == 0) {
                    // Cron実行の場合

                    // パスワード複合化
                    String auto_runPw = Kai9Utils.getPropertyFromYaml("kai9.auto_run_pw");
                    // 自動実行ユーザが存在しない場合は作成する
                    String sql = "INSERT INTO m_user_a (modify_count, login_id, sei, mei, sei_kana, mei_kana, password, mail, need_password_change, ip, default_g_id, authority_lv, note, update_u_id, update_date, delflg)" +
                            " SELECT 1, 'auto_run', '自動', '実行', 'ジドウ', 'ジッコウ', '" + auto_runPw + "', 'auto_run@kai9.com', FALSE, '', 0, 3, '', 0, ?, FALSE" +
                            " WHERE NOT EXISTS (" +
                            "   SELECT 1 FROM m_user_a WHERE login_id = 'auto_run'" +
                            " ) RETURNING user_id;";
                    Integer newUserId = null;
                    try {
                        newUserId = jdbcTemplate_com.queryForObject(sql, new Object[] { Timestamp.valueOf(LocalDateTime.now()) }, Integer.class);
                    } catch (EmptyResultDataAccessException e) {
                        // INSERT文が実行されず、キーが返されなかった場合、何もしない
                    } catch (DataAccessException e) {
                        // その他のデータベースアクセスエラー
                        Kai9Utils.makeLog("error", "自動実行モード:auto_runユーザの自動作成に失敗しました,", AutoExec.class);
                    }
                    if (newUserId != null) {
                        // 自動生成した場合だけ、履歴テーブルへコピー
                        String copySql = "INSERT INTO m_user_b SELECT * FROM m_user_a WHERE user_id = ?;";
                        jdbcTemplate_com.update(copySql, newUserId);
                    }
                    login_id = "auto_run";
                } else {
                    // 処理キューの場合
                    // 実行ユーザを検索しセット
                    String sql = "select * from m_user_a where user_id = ?";
                    Map<String, Object> map = jdbcTemplate_com.queryForMap(sql, run_u_id);
                    login_id = (String) map.get("login_id");
                }

                // 自己認証用にトークン生成
                String secretKey = kai9secretKey;
                Algorithm algorithm = Algorithm.HMAC256(secretKey);
                String jwtToken = JWT.create()
                        .withSubject(login_id)
                        .withIssuer("com.kai9")
                        .withIssuedAt(Date.from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant()))
                        .sign(algorithm);
                // APIコール
                callExecSyoriApi(s1_id, s2Ids, s3Ids, jwtToken, execute_uuid, s_linking_ng, optionalParam);
                
            } finally {
                //ロック解除
                releaseExclusiveLock(s1_id, runHost,jdbcTemplate);            
            }
        }catch (Exception e) {
            Kai9Utils.makeLog("error", "callAPI:予期せぬエラー,"+Kai9Utils.processExceptionMessages(e), AutoExec.class);
        }
    }

    // APIコール
    public static void callExecSyoriApi(Integer s1Id, String s2Ids, String s3Ids, String jwtToken, String execute_uuid, Boolean s_linking_ng, String optionalParam) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/api/exec_syori")
                .queryParam("s1_id", s1Id)
                .queryParam("s2_ids", s2Ids)
                .queryParam("s3_ids", s3Ids)
                .queryParam("execute_uuid", execute_uuid)
                .queryParam("s_linking_ng", s_linking_ng)
                .queryParam("optionalParam", optionalParam);

        String response = webClient.post()
                .uri(uriBuilder.build().toUriString())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.COOKIE, "token=" + jwtToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();  // APIリクエスト完了を待機
        
        //Kai9Utils.makeLog("info", "APIコール成功: " + response, AutoExec.class);


        // 時間がかかる処理の途中で、高負荷等による通信断が発生した場合、実際はAPIが動いてるのに、エラーログが出力されてしまいノイズになるため、ここでの成否は問わず、DBに登録した状態を管理するだけの方針とする
//    	responseMono
//            .doOnSuccess(result -> {
//            String logMessage = String.format("APIコール 成功: URL=%s, パラメータ=%s", requestUrl, uriBuilder.build().getQueryParams());
//            Kai9Utils.makeLog("info", logMessage, Export_s_excel.class);
//            })
//            .doOnError(error -> {
//                Kai9Utils.makeLog("error", "APIコール 失敗" + System.lineSeparator() + error.getMessage(), AutoExec.class);
//            })
//          .subscribe();
    }

    // 指定日時が、スケジュールラベルにマッチするか判定
    public static boolean isScheduledMatch(String label, LocalDate currentDate, LocalTime currentTime,scheduler scheduler, boolean isNearestDay) {
        if (scheduler == null) {
            scheduler = new scheduler();
        }
        boolean isMatch = false;

        // スケジュールラベル解析
        try {
            // 繰り返し制御の解析
            Matcher recurringMatch = Pattern.compile("\\[(\\d+)分間隔\\s?(\\d{2}:\\d{2})?(まで)?\\]").matcher(label);
            if (recurringMatch.find()) {
                scheduler.setRecurring_interval(Integer.parseInt(recurringMatch.group(1)));
                // 時刻が存在しない場合は "00:00" を設定
                scheduler.setRecurring_end_time(recurringMatch.group(2) != null ? recurringMatch.group(2) : "00:00");
                label = label.replace(recurringMatch.group(0), "").trim(); // 繰り返し部分を除去
            }

            // スケジュールパターンの解析
            if (label.startsWith("毎日")) {
                scheduler.setSchedule_pattern("毎日");
                Matcher timeMatcher = Pattern.compile("\\d{2}:\\d{2}").matcher(label);
                if (timeMatcher.find()) {
                    scheduler.setExecution_time(timeMatcher.group(0));
                } else {
                    throw new IllegalArgumentException("毎日のスケジュールに有効な実行時刻が設定されていません: " + label);
                }

            } else if (label.startsWith("毎週")) {
                scheduler.setSchedule_pattern("毎週");
                String[] parts = label.replace("毎週 ", "").split(" ");
                scheduler.setWeekdays(parts[0]);
                if (parts.length > 1 && parts[1].matches("\\d{2}:\\d{2}")) {
                    scheduler.setExecution_time(parts[1]);
                } else {
                    throw new IllegalArgumentException("毎週スケジュールに有効な実行時刻が設定されていません: " + label);
                }

            } else if (label.startsWith("毎月 ") && label.contains("日")) {
                scheduler.setSchedule_pattern("毎月");
                Matcher dayMatcher = Pattern.compile("毎月 (\\d+)日").matcher(label);
                if (dayMatcher.find()) {
                    scheduler.setExecution_day(Integer.parseInt(dayMatcher.group(1)));
                } else {
                    throw new IllegalArgumentException("毎月スケジュールに有効な実行日が設定されていません: " + label);
                }
                Matcher timeMatcher = Pattern.compile("\\d{2}:\\d{2}").matcher(label);
                if (timeMatcher.find()) {
                    scheduler.setExecution_time(timeMatcher.group(0));
                } else {
                    throw new IllegalArgumentException("毎月スケジュールに有効な実行時刻が設定されていません: " + label);
                }

            } else if (label.startsWith("毎月(月末)")) {
                scheduler.setSchedule_pattern("毎月(月末)");

                // "毎月(月末)から XX日前" の部分から XX を抽出
                Matcher daysAgoMatcher = Pattern.compile("毎月\\(月末\\)から (\\d+)日前").matcher(label);
                if (daysAgoMatcher.find()) {
                    scheduler.setMonth_end_n_days_ago(Integer.parseInt(daysAgoMatcher.group(1)));
                } else {
                    throw new IllegalArgumentException("毎月(月末)スケジュールに有効なN日前が設定されていません: " + label);
                }

                Matcher timeMatcher = Pattern.compile("\\d{2}:\\d{2}").matcher(label);
                if (timeMatcher.find()) {
                    scheduler.setExecution_time(timeMatcher.group(0));
                } else {
                    throw new IllegalArgumentException("毎月(月末)スケジュールに有効な実行時刻が設定されていません: " + label);
                }

            } else if (label.startsWith("第")) {
                scheduler.setSchedule_pattern("第N曜日");

                // 週番号部分を取得し、「第」を取り除いてカンマで分割
                String weekPart = label.substring(0, label.indexOf(" ")).replace("第", "");
                scheduler.setWeeks_number(String.join(",", weekPart.split(","))); // 例: "1,2,3,4,5"

                // 曜日部分を取得し、カンマで分割
                String dayPart = label.substring(label.indexOf(" ") + 1, label.lastIndexOf(" ")).replace("曜日", "");
                scheduler.setWeekdays(String.join(",", dayPart.split(","))); // 例: "月,水,木,金,日"

                // 実行時刻を抽出
                Matcher timeMatcher = Pattern.compile("\\d{2}:\\d{2}").matcher(label);
                if (timeMatcher.find()) {
                    scheduler.setExecution_time(timeMatcher.group(0));
                } else {
                    throw new IllegalArgumentException("第N曜日スケジュールに有効な実行時刻が設定されていません: " + label);
                }
            }

        } catch (Exception e) {
            System.err.println("スケジュールラベル解析エラー: " + e.getMessage());
            return false;
        }

        //24時間を超えるケースを想定し時間を変換(48:00等）
        String[] timeParts = scheduler.getExecution_time().split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        // 24時間を超える場合、24で割った余りで時間を設定し、前日フラグをtrueにする
        if (hours >= 24) {
            hours = hours % 24;
            //前日として扱う
            currentDate = currentDate.minusDays(1);
        }
        LocalTime executionTime = LocalTime.of(hours, minutes);
        

     // スケジュールパターンに基づく実行条件を確認（曜日・日付のみ）
        boolean baseMatch = false;
        LocalDateTime nearestDateTime = null; // 直近のスケジュール日時を保持
        LocalDateTime now = LocalDateTime.of(currentDate, currentTime); // 現在日時

        switch (scheduler.getSchedule_pattern()) {
            case "毎日":
                baseMatch = true;
                // 直近フラグが有効な場合のみ、過去2日間をチェック
                if (isNearestDay) {
                    for (int i = 0; i < 2; i++) {
                        LocalDateTime candidateDateTime = LocalDateTime.of(currentDate.minusDays(i), executionTime);
                        if (candidateDateTime.isBefore(now)) {
                            nearestDateTime = candidateDateTime;
                            break;
                        }
                    }
                }
                break;

            case "毎週":
                DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
                String currentDayInJapanese = JAPANESE_DAYS[currentDayOfWeek.getValue() - 1];
                baseMatch = Arrays.asList(scheduler.getWeekdays().split(",")).contains(currentDayInJapanese);
                
                if (isNearestDay && baseMatch) {
                    // 直近フラグが有効な場合のみ、過去7日間をチェック
                    for (int i = 0; i < 7; i++) {
                        LocalDate candidateDate = currentDate.minusDays(i);
                        DayOfWeek candidateDayOfWeek = candidateDate.getDayOfWeek();
                        String candidateDayInJapanese = JAPANESE_DAYS[candidateDayOfWeek.getValue() - 1];

                        if (Arrays.asList(scheduler.getWeekdays().split(",")).contains(candidateDayInJapanese)) {
                            LocalDateTime candidateDateTime = LocalDateTime.of(candidateDate, executionTime);
                            if (candidateDateTime.isBefore(now)) {
                                nearestDateTime = candidateDateTime;
                                break;
                            }
                        }
                    }
                }
                break;

            case "毎月":
                baseMatch = currentDate.getDayOfMonth() == scheduler.getExecution_day();
                if (isNearestDay && baseMatch) {
                    // 直近フラグが有効な場合のみ、過去1ヶ月分をチェック
                    for (int i = 0; i < 2; i++) {
                        LocalDate candidateDate = currentDate.minusMonths(i).withDayOfMonth(scheduler.getExecution_day());
                        LocalDateTime candidateDateTime = LocalDateTime.of(candidateDate, executionTime);
                        if (candidateDateTime.isBefore(now)) {
                            nearestDateTime = candidateDateTime;
                            break;
                        }
                    }
                }
                break;

            case "毎月(月末)":
                int lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).getDayOfMonth();
                int scheduledDay = lastDayOfMonth - scheduler.getMonth_end_n_days_ago();
                baseMatch = currentDate.getDayOfMonth() == scheduledDay;
                if (isNearestDay && baseMatch) {
                    // 直近フラグが有効な場合のみ、過去1ヶ月分をチェック
                    for (int i = 0; i < 2; i++) {
                        LocalDate lastDay = currentDate.minusMonths(i).withDayOfMonth(currentDate.minusMonths(i).lengthOfMonth());
                        LocalDate candidateDate = lastDay.minusDays(scheduler.getMonth_end_n_days_ago());
                        LocalDateTime candidateDateTime = LocalDateTime.of(candidateDate, executionTime);
                        if (candidateDateTime.isBefore(now)) {
                            nearestDateTime = candidateDateTime;
                            break;
                        }
                    }
                }
                break;

            case "第N曜日":
                String[] weeks = scheduler.getWeeks_number().split(",");
                String[] weekdays = scheduler.getWeekdays().split(",");
                
                for (String week : weeks) {
                    int weekNumber = Integer.parseInt(week.trim());
                    for (String weekdayInJapanese : weekdays) {
                        int dayIndex = Arrays.asList(JAPANESE_DAYS).indexOf(weekdayInJapanese.trim());

                        if (dayIndex != -1) {
                            DayOfWeek dayOfWeek = DayOfWeek.of(dayIndex + 1);
                            LocalDate nthDay = currentDate.with(TemporalAdjusters.dayOfWeekInMonth(weekNumber, dayOfWeek));
                            baseMatch = currentDate.equals(nthDay);

                            if (isNearestDay && baseMatch) {
                                // 直近フラグが有効な場合のみ、過去6週間をチェック
                                for (int i = 0; i < 6; i++) {
                                    LocalDate nthWeekdayDate = currentDate.minusWeeks(i)
                                            .with(TemporalAdjusters.dayOfWeekInMonth(weekNumber, dayOfWeek));
                                    LocalDateTime candidateDateTime = LocalDateTime.of(nthWeekdayDate, executionTime);
                                    if (candidateDateTime.isBefore(now)) {
                                        nearestDateTime = candidateDateTime;
                                        break;
                                    }
                                }
                            }
                            if (nearestDateTime != null) break;
                        }
                    }
                    if (nearestDateTime != null) break;
                }
                break;

            default:
                break;
        }
        
        // 直近日フラグが有効な場合、直近の日付との比較だけ行う
        if (isNearestDay) {
            if (nearestDateTime == null || nearestDateTime.toLocalDate().isAfter(now.toLocalDate())) {
                return false; 
            }else {
                return true; 
            }
        }
        
        // 曜日・日付が一致する場合のみ、時間と繰り返し間隔をチェック
        if (baseMatch) {
            LocalTime recurringEndTime = LocalTime.parse(scheduler.getRecurring_end_time());
            long minutesBetween = ChronoUnit.MINUTES.between(executionTime, currentTime);

            // 繰り返し間隔が有効な場合
            if (scheduler.getRecurring_interval() > 0) {
                // 繰り返し間隔と終了時刻をチェック
                if ((recurringEndTime.equals(LocalTime.of(0, 0)) || !currentTime.isAfter(recurringEndTime))
                        && minutesBetween >= 0 && (minutesBetween % scheduler.getRecurring_interval() == 0)) {
                    isMatch = true;
                }
            } else {
                // 繰り返し間隔が無効な場合、実行時刻が一致するか確認
                isMatch = currentTime.equals(executionTime);
            }
        }

            
        return isMatch;
    }
    //ラッパー関数
    public static boolean isScheduledMatch(String label, LocalDate currentDate, LocalTime currentTime,scheduler scheduler) {
        return isScheduledMatch(label, currentDate, currentTime, scheduler, false);
    }

    // [処理排他] 排他チェックとロック取得
    private static boolean acquireExclusiveLock(int s1_id, String runHost, int update_u_id, JdbcTemplate jdbcTemplate) {
        boolean onDebugLog = Boolean.valueOf(Kai9Utils.getPropertyFromYaml("kai9.on_debug_log"));
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        // 現在時刻とタイムアウト基準時刻
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        Timestamp timeoutTimestamp = Timestamp.valueOf(LocalDateTime.now().minusMinutes(LOCK_TIMEOUT_MINUTES));

        // 古いロックのクリーンアップ（60分以上前のロックを解除）
        String cleanupSql = "DELETE FROM syori_exclusive WHERE s1_id = :s1_id AND run_host = :run_host AND update_date <= :timeout";
        MapSqlParameterSource cleanupParams = new MapSqlParameterSource()
                .addValue("s1_id", s1_id)
                .addValue("run_host", runHost)
                .addValue("timeout", timeoutTimestamp);
        namedJdbcTemplate.update(cleanupSql, cleanupParams);

        // 排他ロックとしてレコードを挿入
        String insertSql = "INSERT INTO syori_exclusive (s1_id, run_host, update_u_id, update_date) " +
                           "VALUES (:s1_id, :run_host, :update_u_id, :update_date)";

        MapSqlParameterSource insertParams = new MapSqlParameterSource()
                .addValue("s1_id", s1_id)
                .addValue("run_host", runHost)
                .addValue("update_u_id", update_u_id)
                .addValue("update_date", currentTimestamp);

        try {
            // ロックを示すレコードを挿入
            namedJdbcTemplate.update(insertSql, insertParams);
            if (onDebugLog) {
                Kai9Utils.makeLog("info", "ロック取得成功: s1_id=" + s1_id + ", runHost=" + runHost, AutoExec.class);
            }
            return true;
        } catch (DuplicateKeyException e) {
            // ロックがすでに存在する場合
            if (onDebugLog) {
                Kai9Utils.makeLog("info", "ロック取得失敗（既に存在）: s1_id=" + s1_id + ", runHost=" + runHost, AutoExec.class);
            }
            return false;
        } catch (DataAccessException e) {
            // その他のデータベースエラーが発生した場合
            Kai9Utils.makeLog("error", "排他ロックの取得に失敗しました: " + Kai9Utils.processExceptionMessages(e), AutoExec.class);
            return false;
        }
    }

    // [処理排他] 処理終了後にロック解除
    public static void releaseExclusiveLock(int s1_id, String runHost, JdbcTemplate jdbcTemplate) {
        boolean onDebugLog = Boolean.valueOf(Kai9Utils.getPropertyFromYaml("kai9.on_debug_log"));
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        String releaseSql = "DELETE FROM syori_exclusive WHERE s1_id = :s1_id AND run_host = :run_host";
        MapSqlParameterSource releaseParams = new MapSqlParameterSource()
                .addValue("s1_id", s1_id)
                .addValue("run_host", runHost);

        namedJdbcTemplate.update(releaseSql, releaseParams);
        if (onDebugLog) {
            Kai9Utils.makeLog("info", "ロック解除完了: s1_id=" + s1_id + ", runHost=" + runHost, AutoExec.class);
        }
    }
    
    
    
}
