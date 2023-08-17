import Utils.ImageUtils;
import Utils.PhotoMessageUtils;
import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.ImageFilter;
import functions.ImageOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

//import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Bot extends TelegramLongPollingBot {

    HashMap<String, Message> messages = new HashMap<>();

    @Override
    public String getBotUsername() {
        InputStream fileName= null;
        try {
            fileName = new FileInputStream("Доступ Имя");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner name=new Scanner(fileName);
        String nameBot=name.nextLine();
        return ""+nameBot+"";
    }
    @Override
    public String getBotToken() {
        InputStream fileKey= null;
        try {
            fileKey = new FileInputStream("Доступ Ключ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner name=new Scanner(fileKey);
        String keyBot=name.nextLine();
        return ""+keyBot+"";
    }

    @Override
    public void onUpdateReceived(Update update) {
        //final String localFileName="received_image.jpeg";
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }
            responseTextMessage = runPhotoMessage(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }

            Object responseMediaMessage = runPhotoFilter(message);
            if (responseMediaMessage != null) {
                if (responseMediaMessage instanceof SendMediaGroup){
                    execute((SendMediaGroup) responseMediaMessage);
                }else
                    if (responseMediaMessage instanceof SendMessage){
                        execute((SendMessage) responseMediaMessage);

                }
                return;
            }
        } catch (InvocationTargetException | IllegalAccessException | TelegramApiException e) {
            e.printStackTrace();
        }

//        try {
//            ArrayList<String> photoPaths = new ArrayList<>(PhotoMessageUtils.savePhotos(getFileByMessage(message), getBotToken()));
//            for (String path : photoPaths) {
//
//                execute(preparePhotoMessage(path, chatId));
//            }
//            String response = runCommand(message.getText());
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(chatId);
//            sendMessage.setText(response);
//            execute(sendMessage);
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        ///
//
//    }
    }

    private SendMessage runCommonCommand(Message message) throws
            InvocationTargetException, IllegalAccessException {
//        ArrayList<String> photoPaths = new ArrayList<>(PhotoMessageUtils.savePhotos(getFileByMessage(message), getBotToken()));
//        for (String path : photoPaths) {
//            PhotoMessageUtils.processingImage(path);
//            execute(preparePhotoMessage(path, message.getChatId().toString()));
//        }
        String text = message.getText();
        BotCommonCommands commands = new BotCommonCommands();
        Method[] classMethods = commands.getClass().getDeclaredMethods();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(text)) {
                    method.setAccessible(true);
                    String responseText = (String) method.invoke(commands);
                    if (responseText != null) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(message.getChatId().toString());
                        sendMessage.setText(responseText);
                        return sendMessage;
                    }
                }
            }
        }
        return null;
    }

    private SendMessage runPhotoMessage(Message message) {
        List<File> files = getFileByMessage(message);
        if (files.isEmpty()) {
            return null;
        }
        String chatId = message.getChatId().toString();
        messages.put(chatId, message);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>(getKeyboardRows(ImageFilter.class));

        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите фильтр: ");
        return sendMessage;
    }

    private Object runPhotoFilter(Message newMessage) throws InvocationTargetException, IllegalAccessException {
        final String text = newMessage.getText();
        ImageOperation operation = ImageUtils.getOperation(text);
        if (operation == null) return null;
        String chatId = newMessage.getChatId().toString();
        Message photoMessage=messages.get(chatId);
        if (photoMessage!=null){
        List<File> files = getFileByMessage(photoMessage);
        try {
            List<String> paths = PhotoMessageUtils.savePhotos(files, getBotToken());

            return preparePhotoMessage(paths, operation, chatId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        }else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Отправьте фото, чтобы воспользоваться фильтром");
            return sendMessage;
        }
        return null;
    }

    private List<File> getFileByMessage(Message message) {
        List<PhotoSize> photoSizes = message.getPhoto();
        if (photoSizes==null)return new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();
        for (PhotoSize photoSize : photoSizes) {
            final String fileId = photoSize.getFileId();
            try {
                files.add(sendApiMethod(new GetFile(fileId)));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    private SendMediaGroup preparePhotoMessage(List<String> localPaths, ImageOperation operation, String chatId) throws Exception {
        SendMediaGroup mediaGroup = new SendMediaGroup();
        ArrayList<InputMedia> medias = new ArrayList<>();
        for (String path : localPaths) {
            InputMedia inputMedia = new InputMediaPhoto();
            PhotoMessageUtils.processingImage(path, operation);
            inputMedia.setMedia(new java.io.File(path), "" + path + "");
            // inputMedia.setNewMediaFile();
            medias.add(inputMedia);
        }
        mediaGroup.setMedias(medias);
        mediaGroup.setChatId(chatId);
//        SendPhoto sendPhoto = new SendPhoto();
//
//        sendPhoto.setReplyMarkup(getKeyboard());
//        sendPhoto.setChatId(chatId);
//        InputFile newFile = new InputFile();
//        newFile.setMedia(new java.io.File(localPath));
//        sendPhoto.setPhoto(newFile);
//        sendPhoto.setCaption("Абалдеть");
//        SendMessage sendMessage = new SendMessage();
//        //sendMessage.setChatId(chatId.getChatId().toString());
//        //sendMessage.setText("you message: "+chatId.getText());
//        return sendPhoto;
        return mediaGroup;
    }

    private ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>();

        allKeyboardRows.addAll(getKeyboardRows(BotCommonCommands.class));
        allKeyboardRows.addAll(getKeyboardRows(ImageFilter.class));

        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;

    }

    private ArrayList<KeyboardRow> getKeyboardRows(Class someClass) {
        Method[] classMethods = someClass.getDeclaredMethods();
        ArrayList<AppBotCommand> commands = new ArrayList<>();
        for (Method method : classMethods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                commands.add(method.getAnnotation(AppBotCommand.class));

            }
        }
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        int columnCount = 3;
        int rowsCount = commands.size() / columnCount + (commands.size() % columnCount == 0 ? 0 : 1);
        for (int rouIndex = 0; rouIndex < rowsCount; rouIndex++) {
            KeyboardRow row = new KeyboardRow();

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int index = rouIndex * columnCount + columnIndex;
                if (index >= commands.size()) continue;
                AppBotCommand command = commands.get(rouIndex * columnCount + columnIndex);
                KeyboardButton keyboardButton = new KeyboardButton(command.name());
                row.add(keyboardButton);

            }
            keyboardRows.add(row);
        }
        return keyboardRows;
    }

}
