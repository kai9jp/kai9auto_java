package kai9.auto.keyword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class FileMonitoring {

    /**
     * ファイル監視
     * 第1引数：ファイル名
     * 第2引数：タイムアウト値(秒)
     * 第2引数の「秒」は書いても無視される仕様。判りやすい様に記載する形としている。
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        String crlf = System.lineSeparator(); // 改行コード

        try {
            // 第1引数が空かどうかをチェック
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            // 第2引数が空かどうかをチェック
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }

            // 第1引数のファイル名を取得
            String fileName = s3p.s3.getValue1().trim();

            // 第2引数のタイムアウト値(秒)を取得し、"秒"を無視して数値部分を抽出
            String timeoutValue = s3p.s3.getValue2().trim();
            int timeoutSeconds = Integer.parseInt(timeoutValue.replaceAll("[^0-9]", ""));

            // ファイルの存在確認を指定秒数だけ待つ
            List<Path> matchingFiles = findMatchingFiles(fileName, timeoutSeconds);

            // 一致するファイルがない場合の処理
            if (matchingFiles.isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "指定されたファイルが存在しません。: " + fileName);
            } else {
                // 一致するファイルがある場合の処理
                String foundFiles = matchingFiles.stream()
                        .map(path -> path.toString() + crlf)
                        .collect(Collectors.joining());
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ファイルが見つかりました。" + crlf + foundFiles);
            }
        } catch (Exception e) {
            // エラーが発生した場合の処理
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
        }
    }

    /**
     * ファイル存在確認
     * 
     * @param fileName 検索するファイル名
     * @param timeoutSeconds タイムアウト値(秒)
     * @return 一致するファイルのリスト（単一の結果）
     */
    private List<Path> findMatchingFiles(String fileName, int timeoutSeconds) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        Path path = Paths.get(fileName);
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) / 1000 < timeoutSeconds) {
            // ファイルの存在を確認
            if (Files.exists(path) && Files.isRegularFile(path)) {
                matchingFiles.add(path);
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("スリープ中に割り込みが発生しました。", e);
            }
        }
        return matchingFiles;
    }
}
