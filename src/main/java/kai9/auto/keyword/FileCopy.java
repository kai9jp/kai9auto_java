package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class FileCopy {

    /**
     * ファイルコピー
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第1引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第2引数は省略できません。");
                return;
            }
            if (!s3p.s3.getValue1().trim().contains("*")) {
                File file = new File(s3p.s3.getValue1().trim());
                if (!file.exists()) {
                    s3p.sr3s.updateError(s3p.sr3, "第1引数で指定されたファイルが存在しません。" + file.getAbsolutePath());
                    return;
                }
            }

            String crlf = System.lineSeparator();
            if (s3p.s3.getValue1().indexOf('*') == -1) {
                // ファイルコピー
                Path srcPath = Paths.get(s3p.s3.getValue1().trim());
                Path dstPath = Paths.get(s3p.s3.getValue2().trim());
                try {
                    Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
                    s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ファイルコピーしました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
                } catch (IOException e) {
                    s3p.sr3s.updateError(s3p.sr3, "ファイルコピーに失敗しました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
                    return;
                }
            } else {
                // ワイルドカード指定の場合
                Path dir = Paths.get(StringUtils.substringBeforeLast(s3p.s3.getValue1().trim(), File.separator)); // コピー元フォルダパス
                String fileName = StringUtils.substringAfterLast(s3p.s3.getValue1().trim(), File.separator); // ファイル名（ワイルドカード指定可能）
                try (Stream<Path> stream = Files.list(dir)) {
                    // ワイルドカードにマッチするファイルの一覧を取得
                    List<Path> files = stream.filter(path -> {
                        return path.getFileName().toString().matches(fileName.replace(".", "\\.").replace("*", ".*"));
                    }).collect(Collectors.toList());

                    // ファイルをコピー
                    int count = 0; // カウンター変数を初期化
                    for (Path srcPath : files) {
                        Path dstPath = Paths.get(s3p.s3.getValue2().trim(), srcPath.getFileName().toString()); // コピー先ファイルパス
                        try {
                            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING); // ファイルコピー
                            int progress = (int) ((float) (++count) / files.size() * 100); // 進捗率を計算
                            s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), progress, s3p, "ファイルコピーしました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
                        } catch (IOException e) {
                            s3p.sr3s.updateError(s3p.sr3, "ファイルコピーに失敗しました" + crlf + "コピー元:" + srcPath.toString() + crlf + "コピー先:" + dstPath.toString());
                            return;
                        }
                    }
                } catch (IOException e) {
                    // ファイル一覧取得に失敗した場合の処理
                    s3p.sr3s.updateError(s3p.sr3, "ワイルドカード指定によるファイル検索に失敗しました");
                    return;
                }
            }
            return;
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }

}
