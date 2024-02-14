import javax.security.auth.spi.LoginModule;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import java.util.HashMap;
import javax.security.auth.login.FailedLoginException;

import java.net.Socket;
import java.io.PrintWriter;

public class MyLoginModule implements LoginModule {
    Subject subject = null;
    CallbackHandler callbackHandler = null;
    MyPrincipal usernamePrincipal = null;
    MyPrincipal smsPrincipal = null;

    // test users
    private Map<String, String> testUsers = new HashMap<>() {{
        put("saif", "pass");
        put("saif1", "pass1");
        put("saif2", "pass2");
    }};

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        // System.out.println("MyLoginModule.initialize()");

        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        // System.out.println("MyLoginModule.login()");
        System.out.println();

        if (!usernamePasswordVerification()) {
            throw new FailedLoginException("Username/Password verification failed");
        }

        if (!smsVerification()) {
            throw new FailedLoginException("SMS verification failed");
        }

        return true;
    }

    private boolean smsVerification() {
        try {
            // Send a random 6 digit verification code to a tcp port for SMS verification
            int randomCode = (int) (Math.random() * 1000000);
            System.out.println("Sending verification code to port 8080...");
            Socket socket = new Socket("localhost", 8080);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Your verification code is: " + randomCode);
            out.close();
            socket.close();

            // get the verification code from the user
            Callback[] callbacks = new Callback[1];
            callbacks[0] = new TextInputCallback("Validate verification code: ");
            callbackHandler.handle(callbacks);
            String verificationCode = ((TextInputCallback) callbacks[0]).getText();

            // verify the verification code
            if (verificationCode.equals(String.valueOf(randomCode))) {
                System.out.println("Verification successful!");
                smsPrincipal = new MyPrincipal("sms");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean usernamePasswordVerification() {
        boolean isValid = false;

        // define callback array to pass to callback handler
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Enter username: ");
        callbacks[1] = new PasswordCallback("Enter password: ", false);

        try {
            // call the callback handler to get the username and password
            callbackHandler.handle(callbacks);

            // get the username and password from the callback handler
            String username = ((NameCallback) callbacks[0]).getName();
            String password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());
            ((PasswordCallback) callbacks[1]).clearPassword(); // clear password for security

            // validate the username and password
            if (testUsers.containsKey(username) && testUsers.get(username).equals(password)) {
                System.out.println("Authentication successful");
                isValid = true;

                // create principal for the authenticated user to be added to the subject later
                // in the commit() method
                usernamePrincipal = new MyPrincipal(username);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return isValid;
    }

    @Override
    public boolean commit() throws LoginException {
        boolean isValid = false;
        // System.out.println("MyLoginModule.commit()");
        System.out.println();

        if (subject != null && /*!subject.getPrincipals().isEmpty()*/ !subject.getPrincipals().contains(usernamePrincipal) && !subject.getPrincipals().contains(smsPrincipal)) {
            subject.getPrincipals().add(usernamePrincipal);
            subject.getPrincipals().add(smsPrincipal);
            isValid = true;
        }

        return isValid;
    }

    @Override
    public boolean abort() throws LoginException {
        // System.out.println("MyLoginModule.abort()");
        System.out.println();

        if(subject != null && subject.getPrincipals().contains(usernamePrincipal))
            subject.getPrincipals().remove(usernamePrincipal);

        if(subject != null && subject.getPrincipals().contains(smsPrincipal))
            subject.getPrincipals().remove(smsPrincipal);
        
        subject = null;
        usernamePrincipal = null;
        smsPrincipal = null;

        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        // System.out.println("MyLoginModule.logout()");
        System.out.println();

        subject.getPrincipals().remove(usernamePrincipal);
        subject.getPrincipals().remove(smsPrincipal);
        subject = null;
        
        return true;
    }
}