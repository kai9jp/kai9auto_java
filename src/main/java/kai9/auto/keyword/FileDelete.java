package kai9.auto.keyword;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class FileDelete {

    /**
     * ファイル削除
     * 第1引数:ファイル名(カンマ区切可)
     * ファイル名にワイルドカードを指定可能
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "引数は省略できません。");
                return;
            }

            // カンマ区切りのファイル名を分割
            List<String> fileNames = Arrays.asList(s3p.s3.getValue1().trim().split(","));

            for (String fileName : fileNames) {
                if (!fileName.contains("*")) {
                    File file = new File(fileName.trim());
                    if (!file.exists()) {
                        s3p.sr3s.updateError(s3p.sr3, "指定されたファイルが存在しません。" + file.getAbsolutePath());
                        return;
                    }
                    // ファイル削除
                    try {
                        Files.delete(Paths.get(fileName.trim()));
                        s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ファイル削除しました: " + fileName.trim());
                    } catch (IOException e) {
                        s3p.sr3s.updateError(s3p.sr3, "ファイル削除に失敗しました: " + fileName.trim());
                        return;
                    }
                } else {
                    // ワイルドカード指定の場合
                    Path dir = Paths.get(StringUtils.substringBeforeLast(fileName.trim(), File.separator));
                    String pattern = StringUtils.substringAfterLast(fileName.trim(), File.separator).replace(".", "\\.").replace("*", ".*");

                    try (Stream<Path> stream = Files.list(dir)) {
                        List<Path> files = stream.filter(path -> path.getFileName().toString().matches(pattern)).collect(Collectors.toList());
                        for (Path path : files) {
                            try {
                                Files.delete(path);
                                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "ファイル削除しました: " + path.toString());
                            } catch (IOException e) {
                                s3p.sr3s.updateError(s3p.sr3, "ファイル削除に失敗しました: " + path.toString());
                                return;
                            }
                        }
                    } catch (IOException e) {
                        s3p.sr3s.updateError(s3p.sr3, "ワイルドカード指定によるファイル検索に失敗しました");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            s3p.sr3s.updateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        } finally {
        }
    }
}