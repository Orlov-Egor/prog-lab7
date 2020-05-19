package client.utility;

import common.interaction.Request;
import common.interaction.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AuthHandler {
    private final String loginCommand = "login";
    private final String registerCommand = "register";

    private Scanner userScanner;

    public AuthHandler(Scanner userScanner) {
        this.userScanner = userScanner;
    }

    public Request handle() {
        AuthAsker authAsker = new AuthAsker(userScanner);
        String command = authAsker.askQuestion("У вас уже есть учетная запись?") ? loginCommand : registerCommand;
        User user = new User(authAsker.askLogin(), encodePassword(authAsker.askPassword()));
        return new Request(command, "", user);
    }

    public String encodePassword (String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(password.getBytes());
            BigInteger integers = new BigInteger(1, bytes);
            String newPassword = integers.toString(16);
            while (newPassword.length() < 32) {
                newPassword = "0" + newPassword;
            }
            return newPassword;
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
