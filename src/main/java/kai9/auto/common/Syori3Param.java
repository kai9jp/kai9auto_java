package kai9.auto.common;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import kai9.auto.model.AppEnv;
import kai9.auto.model.syori3;
import kai9.auto.model.syori_rireki3;
import kai9.auto.service.syori_rireki3_Service;
import lombok.Getter;
import lombok.Setter;

/**
 * 処理3のパラーメタ用コンポーネント
 * 
 */

@EnableEncryptableProperties
@Getter
@Setter
@Component
//@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)/* スレッドスコープでスレッド単位のインスタンスを生成させる */
@Scope("prototype")
public class Syori3Param {

    @Autowired
    private ApplicationContext context;// メモリ解放不要

    @Autowired
    private Environment environment;// メモリ解放不要

    public syori_rireki3_Service sr3s;

    public syori_rireki3 sr3;
    public syori3 s3;

    private String db_Url;

    private String db_Username;

    private String db_Password;

    public String schema = "kai9auto";

    public JdbcTemplate db_jdbcTemplate;

    public Map<String, String> Variable1Map = new HashMap<>();

    public AppEnv AppEnv;

    // 任意のパラメータ(自由に利用可能)
    public String optionalParam;

    // -------------------------------------
    // WEB系
    // -------------------------------------
    public WebDriver driver = null;
    public WebDriverWait wait = null;
    public Boolean IsScreenShot = false;

    @Value("${spring.datasource.primary.schema}")
    private String schema_kai9_test;

    public Syori3Param() {
    }

    // プロトタイプスコープなので、インスタンスを自前生成する
    public void CreateBeans() {
        this.sr3s = context.getBean(syori_rireki3_Service.class);
        this.sr3s.CreateBeans();

        // 環境マスタをロード
        String sql = "select * from app_env_a";
        RowMapper<AppEnv> rowMapper = new BeanPropertyRowMapper<AppEnv>(AppEnv.class);
        List<AppEnv> AppEnvList = this.sr3s.getJdbcTemplate().query(sql, rowMapper);
        if (AppEnvList.isEmpty()) {
            throw new RuntimeException("環境マスタのロードに失敗しました");
        }
        this.AppEnv = AppEnvList.get(0);
    }

    // プロトタイプスコープなので、インスタンスを自前破棄する
    public void destroy() throws SQLException {
        // WebDriverを使用している場合、ドライバーを終了する
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        if (wait != null) {
            wait = null;
        }

        if (AppEnv != null) {
            AppEnv = null;
        }

        // syori_rireki3_Serviceを使用している場合、close()メソッドを呼び出してリソースを解放する
        if (sr3s != null) {
            sr3s.destroy();
        }
        // JdbcTemplateを使用している場合、close()メソッドを呼び出してリソースを解放する
        if (db_jdbcTemplate != null) {
            db_jdbcTemplate.getDataSource().getConnection().close();
            db_jdbcTemplate = null;
        }

        // Variable1Mapの要素をクリア
        Variable1Map.clear();
    }

    // DB接続切替
    public String ChangeDB(String dbUrl, String dbUsername, String dbPassword) {
        this.db_Url = dbUrl;
        this.db_Username = dbUsername;
        this.db_Password = dbPassword;

        DriverManagerDataSource dataSource = new DriverManagerDataSource(this.db_Url, this.db_Username, this.db_Password);
        dataSource.setSchema(this.schema);// スキーマ
        this.db_jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            // データベースに接続成功
        } catch (SQLException e) {
            throw new RuntimeException("データベースに接続失敗", e);
        }
        // DBに接続してSQLを実行し、結果を取得する
        this.db_jdbcTemplate.execute("SELECT 1");
        // 例外発生時は上位クラスで受けるので、ここでは例外処理を行わない

        String sql = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema = ?";
        List<String> tableNames = this.db_jdbcTemplate.queryForList(sql, String.class, schema);
        return String.join(", ", tableNames);
    }

    // DB接続切替戻し
    public String ChangeDBBack() {
        this.db_Url = environment.getProperty("spring.datasource.primary.url");
        this.db_Username = environment.getProperty("spring.datasource.primary.username");
        this.db_Password = environment.getProperty("spring.datasource.primary.password");
        this.schema = schema_kai9_test;

        // データソースとJdbcTemplateを作成する
        DriverManagerDataSource dataSource = new DriverManagerDataSource(db_Url, db_Username, db_Password);
        this.db_jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            // データベースに接続成功
        } catch (SQLException e) {
            throw new RuntimeException("データベースに接続失敗", e);
        }
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema = ?";
        List<String> tableNames = this.db_jdbcTemplate.queryForList(sql, String.class, schema);
        return String.join(", ", tableNames);
    }

    /**
     * エラーを登録しつつ、スクリーンショットを保存する
     */
    public void WebUpdateError(syori_rireki3 sr3, String msg) throws IOException {
        syori_rireki3 result = null;
        String crlf = System.lineSeparator();
        try {
            if (IsScreenShot) {
                String filename = takeScreenshot(sr3);
                sr3.setScreen_shot_filepath(filename);
                sr3s.updateError(sr3, msg + crlf + "[スナップショット保存場所]" + crlf + filename + crlf + crlf + crlf);
                result = sr3;
            } else {
                sr3s.updateError(sr3, msg);
                result = sr3;
            }
            return;
        } catch (Exception e) {
            sr3s.updateError(sr3, msg + crlf + e.getMessage());
        } finally {
            // ファイル操作に失敗した時に備え更新だけは最後に行う
            if (result == null) sr3s.updateError(sr3, msg);
        }
    }

    /**
     * スクリーンショットを保存する
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public String takeScreenshot(syori_rireki3 sr3) throws IOException, InterruptedException {
        // スクリーンショットを取得する
        File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        // ファイル名に日時秒を付ける
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        File folder = new File(this.AppEnv.getDir_web_screenshot());
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("スクリーンショットの格納先フォルダが存在しません:" + this.AppEnv.getDir_web_screenshot());
        }

        // 処理番号１ + 処理回数 毎にフォルダを作成
        String folderPath = this.AppEnv.getDir_web_screenshot() + "\\" + sr3.getS1_id() + "_" + sr3.getS_count();
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // ファイル名を生成
        String filename = folderPath + "\\" + sr3.getS2_id() + "_" + sr3.getS3_id() + "_" + timestamp + ".png";

        // スクリーンショットをファイルに保存する
        int retries = 3;// 共有フォルダへの保存を考慮し1秒待機で3回リトライを試みる
        while (retries > 0) {
            try {
                FileUtils.copyFile(screenshotFile, new File(filename));
                break; // 成功した場合はループを抜ける
            } catch (IOException e) {
                retries--;
                if (retries == 0) {
                    e.printStackTrace();
                    throw e;
                }
                // 少し待機してからリトライする
                Thread.sleep(1000);
            }
        }
        return filename;
    }

    /**
     * ページの読み込みが完了するまで待機する
     */
    public void WebWaitFor() {
        // ページの読み込みが完了するまで待機する
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));
    }

    /**
     * 変数が定義されている場合、変数名を返す
     */
    public String GetVariableName() {
        // [@変数名@]の形式にマッチする正規表現パターンを定義する
        String regex = "^\\[@([\\w\\d]+)@\\]$";
        // syori3からVariable1を取得し、空白をトリムする
        String Variable1 = this.s3.getVariable1().trim();
        // Variable1が[@変数名@]の形式にマッチする場合
        if (Variable1.matches(regex)) {
            // 正規表現パターンをコンパイルして、Matcherオブジェクトを作成する
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(Variable1);
            // マッチングが成功した場合
            if (matcher.matches()) {
                // 変数名箇所の文字列を抜き取る
                String variableName = matcher.group(1);
                return variableName;
            }
        }
        return "";
    }

}
