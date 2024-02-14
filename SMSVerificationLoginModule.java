import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.TextInputCallback;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class SMSVerificationLoginModule implements LoginModule {
    Subject subject = null;
    CallbackHandler callbackHandler = null;
    MyPrincipal myPrincipal = null;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        boolean isValid = false;
        
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
            if (Integer.toString(randomCode).equals(verificationCode)) {
                System.out.println("SMS Verification successful!");
                isValid = true;
                
                // create principal for the authenticated user to be added to the subject later
                // in the commit() method
                myPrincipal = new MyPrincipal(verificationCode);
            } else {
                isValid = false;
                throw new FailedLoginException("SMS Verification failed.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isValid;
    }

    @Override
    public boolean commit() throws LoginException {
        boolean isValid = false;
        if (subject != null && !subject.getPrincipals().contains(myPrincipal)) {
            subject.getPrincipals().add(myPrincipal);
            isValid = true;
        }
        return isValid;
    }

    @Override
    public boolean abort() throws LoginException {
        if (subject != null && subject.getPrincipals().contains(myPrincipal))
            subject.getPrincipals().remove(myPrincipal);
        subject = null;
        myPrincipal = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(myPrincipal);
        subject = null;
        return true;
    }
}