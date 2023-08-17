import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws TelegramApiException, FileNotFoundException {
        Name_Key name_key=new Name_Key();
        name_key.name();
        name_key.key();

        TelegramBotsApi api=new TelegramBotsApi(DefaultBotSession.class);
        BotSession botSession=api.registerBot(new Bot());


    }
}
