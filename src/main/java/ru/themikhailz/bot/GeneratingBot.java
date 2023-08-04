package ru.themikhailz.bot;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.themikhailz.qr.FileQrCodeGenerator;

import java.io.File;
import java.io.IOException;

@Component
public class GeneratingBot extends TelegramLongPollingBot {

    private final String botUsername;

    private final String botToken;

    private final int size = 1000;

    private Message requestMessage = new Message();

    private final SendMessage response = new SendMessage();

    public GeneratingBot(@Value("${telegram.bot.username}") String botUsername, @Value("${telegram.bot.token}") String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    public void onUpdateReceived(Update update) {
        requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();
        response.setChatId(chatId.toString());

        if (requestMessage.getText().equals("/start")) {
            startMsg(response);
        } else {
            generateQrCode(response);
        }

    }

    private void generateQrCode(SendMessage response) throws TelegramApiException {
        String text = requestMessage.getText();
        try {
            File qr = FileQrCodeGenerator.generate(text, size);

            InputFile file = new InputFile(qr);
            SendPhoto photo = SendPhoto.builder()
                                       .chatId(response.getChatId())
                                       .photo(file)
                                       .build();
            execute(photo);
        } catch (IOException e) {
            defaultMsg(response, "Не получилось сгенерировать QR код. \n" + e.getMessage());
        }
    }

    private void startMsg(SendMessage response) throws TelegramApiException {
        defaultMsg(response, "Присылайте мне текст, я вам верну QR-code!\n" +
                             "Если объем информации небольшой, то может быть такое, " +
                             "сканеру qr-кода понадобится больше времени для сканирования, " +
                             "из-за отсутствия выравнивающего узора - маленький квадратик снизу справа");
    }

    private void defaultMsg(SendMessage response, String message) throws TelegramApiException {
        response.setText(message);
        execute(response);
    }
}