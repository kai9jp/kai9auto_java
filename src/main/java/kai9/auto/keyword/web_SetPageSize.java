package kai9.auto.keyword;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.springframework.stereotype.Component;

import kai9.libs.Kai9Utils;
import kai9.auto.common.Syori3Param;

@Component
public class web_SetPageSize {

    /**
     * [web]画面サイズ調整(横,縦)
     * 第１引数：横幅(ピクセル)
     * 第２引数：縦幅(ピクセル)
     */
    public void exec(Syori3Param s3p) throws CloneNotSupportedException, IOException, JSONException, SQLException {
        try {
            if (s3p.s3.getValue1().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第１引数は省略できません。");
                return;
            }
            if (s3p.s3.getValue2().trim().isEmpty()) {
                s3p.sr3s.updateError(s3p.sr3, "第２引数は省略できません。");
                return;
            }
            Integer width = Integer.valueOf(s3p.s3.getValue1());
            Integer height = Integer.valueOf(s3p.s3.getValue2());
            // サイトアクセス
            s3p.driver.manage().window().setSize(new Dimension(width, height));

            Dimension actualSize = s3p.driver.manage().window().getSize();
            if (actualSize.getWidth() == width && actualSize.getHeight() == height) {
                s3p.sr3s.updateSuccess(this.getClass().getSimpleName(), 100, s3p, "サイズ変更に成功しました。横幅(ピクセル)=" + width + "縦幅(ピクセル)=" + height);
                return;
            } else {
                s3p.sr3s.updateError(s3p.sr3, "ウィンドウサイズが変更されませんでした");
                return;
            }

        } catch (Exception e) {
            s3p.WebUpdateError(s3p.sr3, Kai9Utils.GetException(e));
            return;
        }
    }

}
