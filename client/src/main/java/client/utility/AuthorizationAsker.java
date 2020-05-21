package client.utility;

import client.App;
import common.exceptions.IncorrectInputInScriptException;
import common.exceptions.MustBeNotEmptyException;
import common.exceptions.NotInDeclaredLimitsException;
import common.utility.Outputer;

import java.util.Scanner;
import java.util.NoSuchElementException;

public class AuthorizationAsker{
	private Scanner userScanner;
	private boolean fileMode;

	public AuthorizationAsker (Scanner userScanner){
		this.userScanner = userScanner;
	}

	public String askLogin(){
		String login;
        while (true) { 
            try {
                Outputer.println("Введите логин:");
                Outputer.print(App.PS2);
                login = userScanner.nextLine().trim();
                if (login.equals("")) throw new MustBeNotEmptyException();
                break;
            } catch (NoSuchElementException exception) {
                Outputer.printerror("Имя не распознано!");
            } catch (MustBeNotEmptyException exception) {
                Outputer.printerror("Имя не может быть пустым!");
            } catch (IllegalStateException exception) {
                Outputer.printerror("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return login;
	}
// Добавить проверку парля на верность
	public String askPassword(){
		String password;
        while (true) {
            try {
                Outputer.println("Введите пароль:");
                Outputer.print(App.PS2);
                password = userScanner.nextLine().trim();
                break;
            } catch (NoSuchElementException exception) {
                Outputer.printerror("Имя не распознано!");
            } catch (IllegalStateException exception) {
                Outputer.printerror("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return password;
	}
}