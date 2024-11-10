package kai9.auto.common;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import kai9.auto.exec.AutoExec;
import kai9.auto.model.AppEnv;
import kai9.auto.model.syori1;
import kai9.auto.model.syori2;
import kai9.auto.model.syori_rireki1;
import kai9.auto.model.syori_rireki2;
import kai9.auto.model.syori_rireki3;
import kai9.libs.Kai9Utils;
import kai9.libs.PoiUtil;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

//処理シナリオの出力
@Component
public class Export_s_excel {

    private static WebClient webClient;

    @Autowired
    public JdbcTemplate db_jdbcTemplate;

    @Value("${jwt.secretKey}")
    private String kai9secretKey;

    // コンストラクタでAPI実行用の各部品を生成(Autoexec.javaにも同じ実装有り。変更時は揃える事)
    public Export_s_excel(WebClient.Builder webClientBuilder, @Value("${server.port}")
    int serverPort) throws SSLException {
        // 初回のみ初期化
        if (webClient == null) {
            synchronized (AutoExec.class) {
                if (webClient == null) {
                    webClient = createWebClient(webClientBuilder, serverPort);
                }
            }
        }
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

    public void Export(int s1_id, int s_count) {

        // 自己認証用にトークン生成
        String secretKey = kai9secretKey;
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withSubject("auto_run")
                .withIssuer("com.kai9")
                .withIssuedAt(Date.from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant()))
                .sign(algorithm);

        // 処理履歴の検索(URLとパラメータ)
        UriComponentsBuilder uriBuilder1 = UriComponentsBuilder.fromPath("/api/syori_rireki1_find_one")
                .queryParam("limit", 100)
                .queryParam("offset", 0)
                .queryParam("findstr", "")
                .queryParam("s1_id", s1_id)
                .queryParam("s2_id", -1)
                .queryParam("s_count", s_count);
        String requestUrl = uriBuilder1.build().toUriString();

        // 処理履歴の検索(APIコール)
        Mono<String> responseMono1 = webClient.post()
                .uri(uriBuilder1.build().toUriString())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.COOKIE, "token=" + jwtToken)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    // 5xxエラーのハンドリング
                    return clientResponse.createException().flatMap(Mono::error);
                })
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    // 4xxエラーのハンドリング（必要に応じて追加）
                    return clientResponse.createException().flatMap(Mono::error);
                })
                .bodyToMono(String.class)
                .doOnSuccess(result -> {
                    // String logMessage = String.format("APIコール 成功: URL=%s, パラメータ=%s", requestUrl, uriBuilder1.build().getQueryParams());
                    // Kai9Utils.makeLog("info", logMessage, Export_s_excel.class);
                })
                .doOnError(error -> {
                    Kai9Utils.makeLog("error", "APIコール 失敗" + System.lineSeparator() + error.getMessage(), Export_s_excel.class);
                });

        // 処理履歴の検索(コールバック)
        responseMono1.subscribe(responseBody1 -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                // 処理履歴をlist形式に変換
                JsonNode rootNode = mapper.readTree(responseBody1);
                String data = rootNode.get("data").asText();
                List<syori_rireki1> syoriRireki1List = mapper.readValue(data, new TypeReference<List<syori_rireki1>>() {
                });

                if (syoriRireki1List.size() != 0) {
                    syori_rireki1 sr1 = syoriRireki1List.get(0);

                    // 処理マスタ検索:シナリオのエクセルを入手(URLとパラメータ)
                    UriComponentsBuilder uriBuilder2 = UriComponentsBuilder.fromPath("/api/syori1_s_excel_download")
                            .queryParam("s1_id", s1_id)
                            .queryParam("modify_count", sr1.getSyori_modify_count());
                    Mono<byte[]> responseMono2 = webClient.post()
                            .uri(uriBuilder2.build().toUriString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.COOKIE, "token=" + jwtToken)
                            .retrieve()
                            .bodyToMono(byte[].class);

                    // APIの結果を回収
                    responseMono2.flatMap(body -> {
                        try {
                            // poi形式に変換
                            Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(body));

                            // 処理マスタの検索(URLとパラメータ)
                            UriComponentsBuilder uriBuilder3 = UriComponentsBuilder.fromPath("/api/syori1_find")
                                    .queryParam("limit", 100)
                                    .queryParam("offset", 0)
                                    .queryParam("findstr", "")
                                    .queryParam("isDelDataShow", true)
                                    .queryParam("s1_id", sr1.getS1_id());
                            // 処理履歴の検索(APIコール)
                            Mono<String> responseMono3 = webClient.post()
                                    .uri(uriBuilder3.build().toUriString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(HttpHeaders.COOKIE, "token=" + jwtToken)
                                    .retrieve()
                                    .bodyToMono(String.class);
                            responseMono3.subscribe(responseBody3 -> {
                                // 処理マスタをlist形式に変換
                                List<String> errors = new ArrayList<String>();
                                try {
                                    JsonNode rootNode3 = null;
                                    rootNode3 = mapper.readTree(responseBody3);
                                    String data3 = rootNode3.get("data").asText();
                                    List<syori1> syori1List = null;
                                    syori1List = mapper.readValue(data3, new TypeReference<List<syori1>>() {
                                    });
                                    if (syori1List.size() != 0) {
                                        syori1 s1 = syori1List.get(0);

                                        // エクセルへデータを格納
                                        String sheetname = "実行順";
                                        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetname);
                                        if (sheet == null) {
                                            String errorMessage = "出力に失敗しました。「" + sheetname + "」シートが存在しません。";
                                            errors.add(errorMessage);
                                            return;
                                        }

                                        // 制御文字「#R3#」の検索
                                        int row3 = PoiUtil.findRow(sheet, "#R3#");
                                        if (row3 == -1) {
                                            errors.add("制御文字「#R3#」がエクセルに発見できませんでした:シート名[" + sheetname + "]");
                                            return;
                                        }

                                        // 制御文字「#R4#」の検索
                                        int row4 = PoiUtil.findRow(sheet, "#R4#");
                                        if (row4 == -1) {
                                            errors.add("制御文字「#R4#」がエクセルに発見できませんでした:シート名[" + sheetname + "]");
                                            return;
                                        }

                                        // 結果を反映
                                        for (syori_rireki2 sr2 : sr1.syori_rireki2s) {

                                            // //実行順シートへの反映
                                            for (int row = row4; row <= sheet.getLastRowNum(); row++) {
                                                String sn = PoiUtil.GetStringValue(sheet, row, s1.getCol_sheetname());
                                                if (sn.isEmpty()) continue;
                                                // シート名が異なる場合は抜ける
                                                if (!sn.equals(sr2.getSheetname())) continue;

                                                Row Row = sheet.getRow(row);
                                                Cell cell = null;
                                                CellStyle oldStyle = null;
                                                CellStyle newStyle = null;

                                                // 開始時刻
                                                cell = Row.getCell(s1.getCol_r_start_time());
                                                if (cell == null) cell = Row.createCell(s1.getCol_r_start_time());
                                                cell.setCellValue(sr2.getStart_time());
                                                // 日時フォーマット設定
                                                oldStyle = cell.getCellStyle();
                                                newStyle = workbook.createCellStyle();
                                                newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                newStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
                                                cell.setCellStyle(newStyle);// スタイル適用

                                                // 終了時刻
                                                cell = Row.getCell(s1.getCol_r_end_time());
                                                if (cell == null) cell = Row.createCell(s1.getCol_r_end_time());
                                                cell.setCellValue(sr2.getEnd_time());
                                                // 日時フォーマット設定
                                                oldStyle = cell.getCellStyle();
                                                newStyle = workbook.createCellStyle();
                                                newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                newStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
                                                cell.setCellStyle(newStyle);// スタイル適用

                                                // 結果
                                                cell = Row.getCell(s1.getCol_result());
                                                if (cell == null) cell = Row.createCell(s1.getCol_result());
                                                // フォーマット設定
                                                oldStyle = cell.getCellStyle();
                                                newStyle = workbook.createCellStyle();
                                                newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                if (sr2.getIs_suspension()) {
                                                    // 中止の場合、黄色で中止と表示
                                                    cell.setCellValue("中止");
                                                    newStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                                                    newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                } else {
                                                    if (sr2.getResult_type() == 0) {
                                                        cell.setCellValue("OK");
                                                    } else {
                                                        cell.setCellValue("NG");
                                                    }
                                                    if (sr2.getResult_type() == 1) {
                                                        // NGを赤にする
                                                        newStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                                                        newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                    }
                                                    if (sr2.getResult_type() == 2) {
                                                        // 想定相違を緑にする
                                                        newStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
                                                        newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                    }
                                                }
                                                cell.setCellStyle(newStyle);// スタイル適用

                                                // 各シートへの反映
                                                sheetname = sr2.getSheetname();
                                                org.apache.poi.ss.usermodel.Sheet sheet3 = workbook.getSheet(sheetname);
                                                if (sheet3 == null) {
                                                    errors.add("出力に失敗しました。「" + sheetname + "」シートが存在しません。");
                                                    continue;
                                                }

                                                // 制御文字「#R3#」の検索
                                                row3 = PoiUtil.findRow(sheet3, "#R3#");
                                                if (row3 == -1) {
                                                    errors.add("制御文字「#R3#」がエクセルに発見できませんでした:シート名[" + sheetname + "]");
                                                    continue;
                                                }

                                                // 処理2を確保
                                                syori2 syori2 = null;
                                                for (syori2 s2 : s1.syori2s) {
                                                    if (sr2.getSheetname() != sheetname) continue;
                                                    syori2 = s2;
                                                    break;
                                                }
                                                if (syori2 == null) continue;

                                                // 各処理3シートへの値反映
                                                int rowCount = sheet3.getLastRowNum() + 1;
                                                for (int rowIndex = row3; rowIndex < rowCount; rowIndex++) {
                                                    String step = PoiUtil.GetStringValue(sheet3, rowIndex, syori2.getCol_step());
                                                    for (syori_rireki3 sr3 : sr2.syori_rireki3s) {
                                                        if (!step.equals(sr3.getStep().toString())) continue;
                                                        if ("セパレータ".equals(sr3.getKeyword())) continue;

                                                        // 実施結果
                                                        Row = sheet3.getRow(rowIndex);
                                                        cell = Row.getCell(syori2.getCol_run_result());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_run_result());
                                                        cell.setCellValue(sr3.getIs_ok() ? "OK" : "NG");

                                                        // 想定相違
                                                        Row = sheet3.getRow(rowIndex);
                                                        cell = Row.getCell(syori2.getCol_ass_diff());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_ass_diff());
                                                        // フォーマット設定
                                                        oldStyle = cell.getCellStyle();
                                                        newStyle = workbook.createCellStyle();
                                                        newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                        if (sr3.getIs_suspension()) {
                                                            // 中止の場合、黄色で中止と表示
                                                            cell.setCellValue("中止");
                                                            newStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                                                            newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                        } else {
                                                            if (sr3.getResult_type() == 0) {
                                                                cell.setCellValue("一致");
                                                            } else {
                                                                cell.setCellValue("相違");
                                                            }
                                                            if (sr3.getResult_type() == 1) {
                                                                // NGを赤にする
                                                                newStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                                                                newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                            }
                                                            if (sr3.getResult_type() == 2) {
                                                                // 想定相違を緑にする
                                                                newStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
                                                                newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                            }
                                                            if (sr3.getResult_type() == 4) {
                                                                // SKIPの場合、緑色でSKIPと表示
                                                                cell.setCellValue("SKIP");
                                                                newStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                                                                newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                                            }
                                                        }
                                                        cell.setCellStyle(newStyle);// スタイル適用

                                                        // 開始時刻
                                                        cell = Row.getCell(syori2.getCol_start_time());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_start_time());
                                                        cell.setCellValue(sr3.getStart_time());
                                                        // 日時フォーマット設定
                                                        oldStyle = cell.getCellStyle();
                                                        newStyle = workbook.createCellStyle();
                                                        newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                        newStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
                                                        cell.setCellStyle(newStyle);// スタイル適用

                                                        // 終了時刻
                                                        cell = Row.getCell(syori2.getCol_end_time());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_end_time());
                                                        cell.setCellValue(sr3.getEnd_time());
                                                        // 日時フォーマット設定
                                                        oldStyle = cell.getCellStyle();
                                                        newStyle = workbook.createCellStyle();
                                                        newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                        newStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
                                                        cell.setCellStyle(newStyle);// スタイル適用

                                                        // 所要時間
                                                        cell = Row.getCell(syori2.getCol_sum_time());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_sum_time());
                                                        Duration diff = Duration.between(
                                                                sr3.getStart_time().toInstant(),
                                                                sr3.getEnd_time().toInstant());
                                                        String diffFormatted = String.format("%02d:%02d:%02d",
                                                                diff.toHoursPart(),
                                                                diff.toMinutesPart(),
                                                                diff.toSecondsPart());
                                                        if (cell != null) cell.setCellValue(diffFormatted);
                                                        // 日時フォーマット設定
                                                        oldStyle = cell.getCellStyle();
                                                        newStyle = workbook.createCellStyle();
                                                        newStyle.cloneStyleFrom(oldStyle);// 元スタイル継承
                                                        newStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
                                                        cell.setCellStyle(newStyle);// スタイル適用

                                                        // ログ
                                                        cell = Row.getCell(syori2.getCol_log());
                                                        if (cell == null) cell = Row.createCell(syori2.getCol_log());
                                                        cell.setCellValue(sr3.getLog());
                                                    }
                                                }
                                            }

                                        }

                                        // 環境マスタから処理シナリオの保存先をロード
                                        String sql = "select * from app_env_a";
                                        RowMapper<AppEnv> rowMapper = new BeanPropertyRowMapper<AppEnv>(AppEnv.class);
                                        List<AppEnv> AppEnvList = db_jdbcTemplate.query(sql, rowMapper);
                                        if (AppEnvList.isEmpty()) {
                                            errors.add("環境マスタのロードに失敗しました");
                                            return;
                                        }
                                        AppEnv AppEnv = AppEnvList.get(0);
                                        String dir = AppEnv.getDir_processedscenario();
                                        String[] fileNameParts = s1.getS_excel_filename().split("\\.");
                                        String fileName = fileNameParts[0];
                                        String extension = fileNameParts[1];
                                        String exportFileName = dir + "\\" + String.format("%s_%s回.%s", fileName, sr1.getS_count(), extension);

                                        // ファイルとして保存する
                                        FileOutputStream fileOut = new FileOutputStream(exportFileName);
                                        workbook.write(fileOut);
                                        fileOut.close();
                                        workbook.close();
                                    }
                                } catch (Exception e) {
                                    errors.add(e.getMessage());
                                    return;
                                } finally {
                                    if (errors.size() != 0) {
                                        String msg = "【s1_id:" + String.valueOf(s1_id) + "/s_count:" + String.valueOf(s_count) + "】";
                                        Kai9Utils.makeLog("error", msg + errors.toString(), this.getClass());
                                    }

                                }
                            });

                        } catch (Exception e) {
                            String msg = "【s1_id:" + String.valueOf(s1_id) + "/s_count:" + String.valueOf(s_count) + "】";
                            Kai9Utils.makeLog("error", msg + e.getMessage(), this.getClass());
                        }
                        return Mono.empty();
                    }).subscribe(result -> {
//			            System.out.println(result);
                    }, error -> {
                        String msg = "【s1_id:" + String.valueOf(s1_id) + "/s_count:" + String.valueOf(s_count) + "】";
                        Kai9Utils.makeLog("error", msg + error.getMessage(), this.getClass());
                    });
                }
            } catch (JsonProcessingException e) {
                String msg = "【s1_id:" + String.valueOf(s1_id) + "/s_count:" + String.valueOf(s_count) + "】";
                Kai9Utils.makeLog("error", msg + e.getMessage(), this.getClass());
            }
        });
    }
}
