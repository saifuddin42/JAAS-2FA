import javax.security.auth.spi.LoginModule;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import java.util.HashMap;
import javax.security.auth.login.FailedLoginException;

public class UsernamePasswordLoginModule implements LoginModule {
    Subject subject = null;
    CallbackHandler callbackHandler = null;
    MyPrincipal myPrincipal = null;

    // test users
    private Map<String, String> testUsers = new HashMap<>() {
        {
            put("saif", "pass");
            put("saif1", "pass1");
            put("saif2", "pass2");
        }
    };

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
                myPrincipal = new MyPrincipal(username);

            } else {
                isValid = false;
                throw new FailedLoginException("Authentication failed");
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

        if (subject != null
                && /* !subject.getPrincipals().isEmpty() */ !subject.getPrincipals().contains(myPrincipal)) {
            subject.getPrincipals().add(myPrincipal);
            isValid = true;
        }

        return isValid;
    }

    @Override
    public boolean abort() throws LoginException {
        // System.out.println("MyLoginModule.abort()");
        System.out.println();

        if (subject != null && subject.getPrincipals().contains(myPrincipal))
            subject.getPrincipals().remove(myPrincipal);

        subject = null;
        myPrincipal = null;

        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        // System.out.println("MyLoginModule.logout()");
        System.out.println();

        subject.getPrincipals().remove(myPrincipal);
        subject = null;

        return true;
    }
}