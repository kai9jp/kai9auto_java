package kai9.auto.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.encryption.StringEncryptor;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kai9.libs.JsonResponse;
import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;
import kai9.auto.keyword.TableExportExcelKw;
import kai9.auto.model.syori3;

/**
 * お便利API :コントローラ
 */
@RestController
public class UtilsAPI {

    @Autowired
    private StringEncryptor encryptor;

    @Autowired
    private ApplicationContext context;

    /**
     * 暗号化
     */
    @PostMapping(value = "/api/encrypt", produces = "application/json;charset=utf-8")
    public void Encrypt(@RequestBody
    Map<String, String> requestData, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        try {

            String inputText = requestData.get("inputText");
            String outputText = encryptor.encrypt(inputText);

            JsonResponse json = new JsonResponse();
            json.setReturn_code(HttpStatus.OK.value());
            json.setMsg("暗号化しました");
            json.Add("outputText", outputText);
            json.SetJsonResponse(res);
            return;

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
        }
    }

    /**
     * 複合化
     */
    public String decrypt(String text) {
        return encryptor.decrypt(text);
    }

    /**
     * テスト用エクセル生成
     */
    @PostMapping(value = "/api/TableExcelGet", produces = "application/json;charset=utf-8")
    public ResponseEntity<Resource> TableExcelGet(@RequestBody
    Map<String, String> requestData, HttpServletResponse res, HttpServletRequest request) throws CloneNotSupportedException, IOException, JSONException {
        try {
            String tableName = requestData.get("tableName");
            String dbIP = requestData.get("dbIP");
            String dbPort = requestData.get("dbPort");
            String dbName = requestData.get("dbName");
            String dbID = requestData.get("dbID");
            String dbPassword = requestData.get("dbPassword");
            String schema = requestData.get("schema");
            Boolean IsNeedData = Boolean.valueOf(requestData.get("IsNeedData"));

            Syori3Param Syori3Param = new Syori3Param();
            if (!dbIP.isEmpty()) {
                // 各パラメータをセット
                dbPassword = decrypt(dbPassword);
                Syori3Param = new Syori3Param();
                Syori3Param.schema = schema;
                Syori3Param.ChangeDB("jdbc:postgresql://" + dbIP + ":" + dbPort + "/" + dbName, dbID, dbPassword);
            } else {
                // IPが空の場合、自身(kai9auto)の各パラメータをセットする
                String origin_dbUrl = context.getEnvironment().getProperty("spring.datasource.primary.url");
                String origin_dbUsername = context.getEnvironment().getProperty("spring.datasource.primary.username");
                String origin_dbPassword = context.getEnvironment().getProperty("spring.datasource.primary.password");
                DriverManagerDataSource dataSource = new DriverManagerDataSource(origin_dbUrl, origin_dbUsername, origin_dbPassword);
                Syori3Param.db_jdbcTemplate = new JdbcTemplate(dataSource);

                Syori3Param.setSchema("kai9auto");
            }

            // テーブル出力(エクセル)
            TableExportExcelKw TableExportExcel = context.getBean(TableExportExcelKw.class);
            syori3 syori3 = new syori3();
            syori3.setValue1(tableName);
            Syori3Param.s3 = syori3;

            // アプリケーションが使用する一時フォルダを指定
            File tempDir = new File(System.getProperty("java.io.tmpdir"));// OSが勝手に消してくれる場所
            File myTempDir = new File(tempDir, "kai9auto_temp");
            myTempDir.mkdirs();
            String TmpFilename = myTempDir + "\\" + tableName + ".xlsx";
            syori3.setValue2(TmpFilename);
            // エクセル作成(Syori3ParamのDB情報を用いる)
            TableExportExcel.exec(Syori3Param, IsNeedData);

            // 生成したファイルをAPIの戻りにバイナリ変換して返す
            File file = new File(TmpFilename);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + syori3.getValue2());
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            Kai9Utils.handleException(e, res);
            return null;
        }
    }

}
