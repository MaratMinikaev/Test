import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Name_Key {

    String name() throws FileNotFoundException {
        PrintWriter writer=new PrintWriter("Доступ Имя");
        Scanner scanner=new Scanner(System.in);
        System.out.println("Введите имя бота");
        String name=scanner.nextLine();
        writer.write(name);
        writer.close();
        return name;
    }
     String key() throws FileNotFoundException {
         PrintWriter writer=new PrintWriter("Доступ Ключ");
        Scanner scanner=new Scanner(System.in);
         System.out.println("Введите ключ бота");
        String key=scanner.nextLine();
        writer.write(key);
        writer.close();
        return key;
    }

}
