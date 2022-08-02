import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            S.s("Please enter a sql");
            String sql = scanner.nextLine();
            if (sql == null || sql.trim().equals("")) {
                continue;
            }
            if (sql.equals("q") || sql.equals("exit") || sql.equals("quit")) {
                break;
            }
            String command = "adb shell am start-foreground-service -n im.thebot.messenger.beta/com.sdk.chat.test.ChatDebugService --es sql \"" + sql + "\"";
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
            }
        }
        S.s("Quit.");
    }
}