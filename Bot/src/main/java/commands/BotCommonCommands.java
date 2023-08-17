package commands;

public class BotCommonCommands {

    @AppBotCommand(name = "/hello", description = "when request hello",showInHelp = true)
     String hello(){
        return "hello user";
    }

    @AppBotCommand(name = "/buy", description = "when request buy",showInHelp = true)
     String buy(){
        return "buy user";


    }
    @AppBotCommand(name = "/help", description = "when request help",showInHelp = true,showInKeyboard = true)
     String help(){
        return "helper to run";
    }

}
