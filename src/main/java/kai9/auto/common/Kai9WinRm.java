package kai9.auto.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Kai9WinRm クラス
//-------------------------------
public class Kai9WinRm {

    // WinRmResponse クラス
    public static class WinRmResponse {
        private int statusCode;
        private String stdout;
        private String stderr;
        private long duration;

        public WinRmResponse(int statusCode, String stdout, String stderr, long duration) {
            this.statusCode = statusCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.duration = duration;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public long getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "Status Code: " + statusCode + "\\n" +
                    "Standard Output: " + stdout + "\\n" +
                    "Error Output: " + stderr + "\\n" +
                    "Duration: " + duration + " ms";
        }
    }

    // このスクリプトを実行すると利用可能になる（セキュアではない）
    // "\\192.168.0.14\disk1\03.tools\13.ansible\ConfigureRemotingForAnsible.ps1"
    // もし5985(http)で暗号化しない通信を行う場合、上のスクリプト実行後、下記コマンドの実行が必要
    // ※暗号化通信を許可するという設定なので、絶対に行わない方が良い↓
    // winrm set winrm/config/service @{AllowUnencrypted="true"}

    // 認証方式を定義する列挙型
    // 利用可否は以下の通り、上から順にセキュア度が高い
    // ①ドメイン参加
    // ②ワークグループ
    // Basic認証(①=利用可、②＝利用可)
    // NTLM(①=利用可、②＝利用可)
    // Kerberos認証(①=利用可、②＝利用できない) ※未実装
    public enum AuthType {
        BASIC, NTLM
    }

    /**
     * ホスト名検証の方式を定義する列挙型
     *
     * ALLOW_ALL: すべてのホスト名を許可する（セキュアではない）
     * STRICT: ホスト名を厳密に検証する（セキュア）
     */
    public enum HostnameVerification {
        ALLOW_ALL, // すべてのホスト名を許可する（セキュアではない）
        STRICT // ホスト名を厳密に検証する（セキュア）
    }

    // プロトコルを定義
    public enum Protocol {
        HTTP, HTTPS
    }

    static String crlf = System.lineSeparator(); // 改行コード

    // WinRM接続先URLと認証情報を格納する変数
    private final String url;
    private final String auth;
    private final AuthType authType;
    private CloseableHttpClient httpClient;
    private String shellId;

    // コンストラクタ：接続先ホスト、ポート、ユーザー名、パスワード、認証方式を受け取って初期化
    public Kai9WinRm(String host, int port, String username, String password, AuthType authType, HostnameVerification hostnameVerification, Protocol protocol) {
        this.url = protocol.name().toLowerCase() + "://" + host + ":" + port + "/wsman";
        this.authType = authType;

        if (authType == AuthType.BASIC) {
            // Basic認証のためのエンコード
            this.auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        } else {
            // NTLM認証の場合はauthは使わない
            this.auth = null;
        }

        this.httpClient = createHttpClient(username, password, host, port, authType, hostnameVerification, protocol);
    }

    // SSLContextを使用してHttpClientを作成
    private CloseableHttpClient createHttpClient(String username, String password, String host, int port, AuthType authType, HostnameVerification hostnameVerification, Protocol protocol) {
        try {
            if (protocol == Protocol.HTTP) {
                // HTTPを使用する場合、SSLを使用しないHttpClientを作成
                if (authType == AuthType.NTLM) {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                            new AuthScope(host, port),
                            new NTCredentials(username, password, null, null) // ドメインはnull
                    );

                    return HttpClients.custom()
                            .setDefaultCredentialsProvider(credsProvider)
                            .build();
                } else {
                    return HttpClients.custom()
                            .disableRedirectHandling() // リダイレクトを無視
                            .disableCookieManagement() // クッキー管理を無効化
                            .build();
                }
            } else {
                // HTTPSを使用する場合、SSLを使用するHttpClientを作成
                SSLContext sslContext = SSLContextBuilder.create()
                        .loadTrustMaterial((chain, trustAuthType) -> true)
                        .build();

                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                if (authType == AuthType.NTLM) {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                            new AuthScope(host, port),
                            new NTCredentials(username, password, null, null) // ドメインはnull
                    );

                    return HttpClients.custom()
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier(hostnameVerification == HostnameVerification.ALLOW_ALL ? (hostname, session) -> true : SSLConnectionSocketFactory.getDefaultHostnameVerifier())
                            .setDefaultCredentialsProvider(credsProvider)
                            .build();
                } else {
                    return HttpClients.custom()
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier(hostnameVerification == HostnameVerification.ALLOW_ALL ? (hostname, session) -> true : SSLConnectionSocketFactory.getDefaultHostnameVerifier())
                            .build();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SSLコンテキストの作成に失敗しました", e);
        }
    }

    // WinRMセッションを開く
    public void openShell() throws Exception {
        String shellCreationBody = createShellCreationMessage();
        String[] results = sendWinRMRequest(shellCreationBody);
        this.shellId = results[1]; // シェルIDを取得して保存
    }

    // スクリプトとして保存し、実行させる版(10秒待機できなかった問題も解決済)
    public WinRmResponse executePowershellWithResponse(String command) throws Exception {
        // 1. リモートの一時ディレクトリのパス
        String remoteTempDir = "$env:LOCALAPPDATA\\Temp"; // リモート環境の一時ディレクトリを指定
        String scriptPath = remoteTempDir + "\\temp_script_" + UUID.randomUUID().toString() + ".ps1";
        String resultPath = remoteTempDir + "\\temp_result_" + UUID.randomUUID().toString() + ".txt";

        // 2. スクリプトをUTF-16LEでエンコードしてBase64に変換
        // パワーシェルコマンドをBase64で暗号化しEncodedCommandオプションで実行させる(文字コードの問題や、エスケープ文字の問題を回避させる)
        String encodedScript = Base64.getEncoder().encodeToString(command.getBytes(StandardCharsets.UTF_16LE));
        String createScriptCommand = String.format(
                "Set-Content -LiteralPath \"%s\" -Value ([System.Text.Encoding]::Unicode.GetString([Convert]::FromBase64String(\"%s\")))",
                scriptPath, encodedScript);
        executePowershellWithResponse2(createScriptCommand);

        // 3. スクリプトをリモートで実行して結果を出力
        String executeCommand = String.format("powershell.exe -ExecutionPolicy Bypass -File \"%s\" | Out-File -FilePath \"%s\"", scriptPath, resultPath);
        WinRmResponse response = executePowershellWithResponse2(executeCommand);

        // 4. 結果のファイルの存在確認
        String checkFileExistence = String.format("Test-Path \"%s\"", resultPath);
        WinRmResponse existenceResponse = executePowershellWithResponse2(checkFileExistence);

        if (!existenceResponse.getStdout().trim().equalsIgnoreCase("True")) {
            throw new Exception("Result file not found: " + resultPath);
        }

        // 5. 結果をリモート側から回収
        String readResultCommand = String.format("Get-Content -Path %s", resultPath);
        WinRmResponse resultResponse = executePowershellWithResponse2(readResultCommand);
        response = new WinRmResponse(response.getStatusCode(), response.getStdout()+resultResponse.getStdout(), response.getStderr()+resultResponse.getStderr(), response.getDuration());

        // 6. リモートのクリーンアップ(作成した一時ファイルを削除)
        String cleanupScript = "Remove-Item -Path '" + resultPath + "' -Force";
        executePowershellWithResponse2(cleanupScript);

        // WinRmResponseを返す
        return response;
    }

    private WinRmResponse executePowershellWithResponse2(String psCommand) throws Exception {
        String command = "powershell.exe -ExecutionPolicy Bypass -Command \"" + psCommand.replace("\"", "\\\"") + "\"";
        return executeCommandWithResponse(command);
    }

    // Stringで返すラッパーメソッド
    public String executePowershell(String psCommand) throws Exception {
        return executePowershellWithResponse(psCommand).getStdout();
    }

    // コマンドを実行し、その結果を取得する
    public String executeCommand(String command) throws Exception {
        return executeCommandWithResponse(command).getStdout();
    }

    // コマンドを実行し、WinRmResponse を返す
    public WinRmResponse executeCommandWithResponse(String command) throws Exception {
        long startTime = System.currentTimeMillis(); // 実行時間計測開始

        if (shellId == null) {
            throw new IllegalStateException("シェルが開かれていません。先にopenShell()を呼び出してください。");
        }

        // コマンド実行メッセージを作成し、送信
        String commandBody = createCommandMessage(shellId, command);
        String[] results = sendWinRMRequest(commandBody);
        String commandId = results[2]; // コマンドIDを取得

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();
        stdoutBuilder.append(results[3]);
        stderrBuilder.append(results[4]);
        
        // すべての出力を受け取るまで繰り返し
        String outputRequestBody = createOutputRequestMessage(shellId, commandId);
        boolean isEndOfStream = false;

        while (!isEndOfStream) {
            results = sendWinRMRequest(outputRequestBody);
            stdoutBuilder.append(results[3]);
            stderrBuilder.append(results[4]);

            // すべての出力が読み込まれたかどうかを確認
            isEndOfStream = results[3].isEmpty() && results[4].isEmpty();
        }

        long duration = System.currentTimeMillis() - startTime; // 実行時間計測終了
        int statusCode = Integer.parseInt(results[0]); // ステータスコードを取得
        String stdout = stdoutBuilder.toString();
        String stderr = stderrBuilder.toString();

        // WinRmResponseオブジェクトを返す
        return new WinRmResponse(statusCode, stdout, stderr, duration);
    }

    // WinRMリクエストを送信し、レスポンスを取得
    private String[] sendWinRMRequest(String soapMessage) throws Exception {
        HttpPost httpPost = new HttpPost(url);

        // HTTPヘッダーの設定
        if (authType == AuthType.BASIC) {
            httpPost.setHeader("Authorization", "Basic " + auth);
        }
        httpPost.setHeader("Content-Type", "application/soap+xml; charset=UTF-8");

        // SOAPメッセージをHTTPエンティティとして設定
        httpPost.setEntity(new StringEntity(soapMessage));

        // リクエストを実行してレスポンスを取得
        HttpResponse response = httpClient.execute(httpPost);
        // ステータスコードを取得
        int statusCode = response.getStatusLine().getStatusCode();

        // ステータスコードによるエラーチェック
        if (statusCode == 401) {
            // 401 Unauthorized: 認証エラー
            throw new RuntimeException("認証エラーが発生しました。ステータスコード:" + statusCode);
        } else if (statusCode >= 400) {
            throw new RuntimeException("接続エラーが発生しました。ステータスコード: " + statusCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder responseText = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseText.append(line);
        }

        if (responseText.length() == 0) {
            throw new RuntimeException("winrmの結果を受信できませんでした");
        }

        // レスポンスからシェルIDとコマンドIDを抽出
        String shellId = extractShellId(responseText.toString());
        String commandId = extractCommandId(responseText.toString());

        // レスポンスからstdoutをデコードして取得
        String stdout = extractAndDecode(responseText.toString(), "stdout");
        String stderr = extractAndDecode(responseText.toString(), "stderr");
        // XMLを解析してエラーを取り出す
        stderr = extractErrorMessages(stderr);

        // ステータスコード、シェルID、コマンドID、stdout、stderrを配列として返す
        return new String[] { String.valueOf(statusCode), shellId, commandId, stdout, stderr };
    }

    public static String extractErrorMessages(String xmlResponse) {
        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            return ""; // XMLが空の場合は空を返す
        }
        if (!xmlResponse.contains("#< CLIXML")) {
            // CLIXML形式でない場合は、そのまま返す
            return xmlResponse;
        }

        // 不正なヘッダーを削除
        String cleanedResponse = removeInvalidHeader(xmlResponse);
        // 戻りが空の場合、空を返す
        if (cleanedResponse.isBlank()) return "";

        try {
            // XMLパーサーを作成
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(cleanedResponse)));

            // エラータグを抽出
            NodeList errorNodes = document.getElementsByTagName("S");

            StringBuilder errorMessages = new StringBuilder();

            for (int i = 0; i < errorNodes.getLength(); i++) {
                // "S" タグに "Error" 属性があるかを確認
                if ("Error".equals(errorNodes.item(i).getAttributes().getNamedItem("S").getTextContent())) {
                    // x000D__x000A_ を改行に置き換える
                    String message = errorNodes.item(i).getTextContent();
                    message = message.replace("_x000D__x000A_", "\n");
                    errorMessages.append(message).append(System.lineSeparator());
                }
            }

            return errorMessages.toString().trim();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return "XML解析中にエラーが発生しました: " + e.getMessage() + crlf + xmlResponse;
        }
    }

    // 不正なヘッダーを削除するためのメソッド
    private static String removeInvalidHeader(String content) {
        // "#< CLIXML" を探して、それを削除
        String invalidHeader = "#< CLIXML";
        int headerIndex = content.indexOf(invalidHeader);
        if (headerIndex != -1) {
            return content.substring(headerIndex + invalidHeader.length()).trim();
        }
        return content.trim(); // 不正なヘッダーが見つからない場合はそのまま返す
    }

    // シェルを閉じる
    public void closeShell() throws Exception {
        if (shellId != null) {
            String shellDeletionBody = createShellDeletionMessage(shellId);
            sendWinRMRequest(shellDeletionBody);
            shellId = null; // シェルIDをリセット
        }
    }

    // シェル作成のためのSOAPメッセージを作成
    private String createShellCreationMessage() {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" " +
                "xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                "xmlns:w=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                "xmlns:p=\"http://schemas.microsoft.com/wbem/wsman/1/windows/shell\">" +
                "<s:Header>" +
                "<a:Action s:mustUnderstand=\"1\">" +
                "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create</a:Action>" +
                "<a:To s:mustUnderstand=\"1\">" + url + "</a:To>" +
                "<w:ResourceURI s:mustUnderstand=\"1\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>" +
                "<a:ReplyTo>" +
                "<a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address>" +
                "</a:ReplyTo>" +
                "<w:MaxEnvelopeSize s:mustUnderstand=\"1\">153600</w:MaxEnvelopeSize>" +
                "<a:MessageID>uuid:" + UUID.randomUUID().toString() + "</a:MessageID>" +
                "<w:Locale xml:lang=\"en-US\" s:mustUnderstand=\"0\"/>" +
                "<w:OperationTimeout>PT60S</w:OperationTimeout>" +
                "</s:Header>" +
                "<s:Body>" +
                "<p:Shell>" +
                "<p:InputStreams>stdin</p:InputStreams>" +
                "<p:OutputStreams>stdout stderr</p:OutputStreams>" +
                "</p:Shell>" +
                "</s:Body>" +
                "</s:Envelope>";
    }

    // コマンド実行のためのSOAPメッセージを作成
    private String createCommandMessage(String shellId, String command) {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" " +
                "xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                "xmlns:w=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                "xmlns:rsp=\"http://schemas.microsoft.com/wbem/wsman/1/windows/shell\">" +
                "<s:Header>" +
                "<a:Action s:mustUnderstand=\"true\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command</a:Action>" +
                "<w:ResourceURI s:mustUnderstand=\"true\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>" +
                "<w:SelectorSet><w:Selector Name=\"ShellId\">" + shellId + "</w:Selector></w:SelectorSet>" +
                "<a:To s:mustUnderstand=\"true\">" + url + "</a:To>" +
                "<a:ReplyTo><a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address></a:ReplyTo>" +
                "<a:MessageID>uuid:" + UUID.randomUUID().toString() + "</a:MessageID>" +
                "<w:MaxEnvelopeSize s:mustUnderstand=\"true\">153600</w:MaxEnvelopeSize>" +
                "<w:OperationTimeout>PT60S</w:OperationTimeout>" +
                "</s:Header>" +
                "<s:Body>" +
                "<rsp:CommandLine><rsp:Command>" + command + "</rsp:Command></rsp:CommandLine>" +
                "</s:Body></s:Envelope>";
    }

    // コマンド結果取得のためのSOAPメッセージを作成
    private String createOutputRequestMessage(String shellId, String commandId) {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" " +
                "xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                "xmlns:w=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\" " +
                "xmlns:rsp=\"http://schemas.microsoft.com/wbem/wsman/1/windows/shell\">" +
                "<s:Header>" +
                "<a:Action s:mustUnderstand=\"true\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive</a:Action>" +
                "<w:ResourceURI s:mustUnderstand=\"true\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>" +
                "<w:SelectorSet><w:Selector Name=\"ShellId\">" + shellId + "</w:Selector></w:SelectorSet>" +
                "<a:To s:mustUnderstand=\"true\">" + url + "</a:To>" +
                "<a:ReplyTo><a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address></a:ReplyTo>" +
                "<a:MessageID>uuid:" + UUID.randomUUID().toString() + "</a:MessageID>" +
                "<w:MaxEnvelopeSize s:mustUnderstand=\"true\">153600</w:MaxEnvelopeSize>" +
                "<w:OperationTimeout>PT60S</w:OperationTimeout>" +
                "</s:Header>" +
                "<s:Body>" +
                "<rsp:Receive>" +
                "<rsp:DesiredStream CommandId=\"" + commandId + "\">stdout stderr</rsp:DesiredStream>" +
                "</rsp:Receive>" +
                "</s:Body></s:Envelope>";
    }

    // シェルを閉じるためのSOAPメッセージを作成
    private String createShellDeletionMessage(String shellId) {
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" " +
                "xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                "xmlns:w=\"http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd\">" +
                "<s:Header>" +
                "<a:Action s:mustUnderstand=\"true\">" +
                "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete</a:Action>" +
                "<w:ResourceURI s:mustUnderstand=\"true\">" +
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>" +
                "<w:SelectorSet><w:Selector Name=\"ShellId\">" + shellId + "</w:Selector></w:SelectorSet>" +
                "<a:To s:mustUnderstand=\"true\">" + url + "</a:To>" +
                "<a:ReplyTo><a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address></a:ReplyTo>" +
                "<a:MessageID>uuid:" + UUID.randomUUID().toString() + "</a:MessageID>" +
                "<w:MaxEnvelopeSize s:mustUnderstand=\"true\">153600</w:MaxEnvelopeSize>" +
                "<w:OperationTimeout>PT60S</w:OperationTimeout>" +
                "</s:Header>" +
                "<s:Body/>" +
                "</s:Envelope>";
    }

    // シェルIDを抽出するためのメソッド
    private String extractShellId(String xmlResponse) throws ParserConfigurationException, SAXException, IOException {
        return extractValueFromXml(xmlResponse, "rsp:ShellId");
    }

    // コマンドIDを抽出するためのメソッド
    private String extractCommandId(String xmlResponse) throws ParserConfigurationException, SAXException, IOException {
        return extractValueFromXml(xmlResponse, "rsp:CommandId");
    }

    // XMLドキュメントから指定したタグ名の値を抽出する汎用関数
    private String extractValueFromXml(String xmlResponse, String tagName) throws ParserConfigurationException, SAXException, IOException {
        // XMLパーサーを作成
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));

        // 指定したタグ名の値を取得
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        } else {
            return "";
        }
    }

    // stdoutなどのストリームをBase64デコードして取得するためのメソッド
    public static String extractAndDecode(String xmlResponse, String streamName) throws Exception {
        // XMLパーサーを作成
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));

        // 指定されたストリーム名からBase64文字列を抽出
        NodeList nodes = document.getElementsByTagName("rsp:Stream");
        StringBuilder decodedOutput = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            String name = nodes.item(i).getAttributes().getNamedItem("Name").getTextContent();
            if (streamName.equals(name)) {
                // Base64文字列をデコード
                String base64EncodedString = nodes.item(i).getTextContent();
                byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
                // デコードしたバイト配列をShift_JIS文字列に変換
                String Output = new String(decodedBytes, Charset.forName("Shift_JIS"));
//                String Output = new String(decodedBytes, StandardCharsets.UTF_8);

                decodedOutput.append(Output);
            }
        }
        
        return decodedOutput.toString();
        // return decodedOutput.toString().replaceAll("[\\x00-\\x1F\\x7F]", "");//ASCII制御文字(制御コード)を削除
    }

}
