import javax.security.auth.callback.CallbackHandler;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MyCallbackHandler implements CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        // System.out.println("MyCallbackHandler.handle()");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callback;
                System.out.print(nameCallback.getPrompt());
                nameCallback.setName(reader.readLine());
            } else if (callback instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                System.out.print(passwordCallback.getPrompt());
                passwordCallback.setPassword(reader.readLine().toCharArray());
            } else if (callback instanceof TextInputCallback) {
                TextInputCallback textInputCallback = (TextInputCallback) callback;
                System.out.print(textInputCallback.getPrompt());
                textInputCallback.setText(reader.readLine());
            } else {
                throw new UnsupportedCallbackException(callback, "The submitted Callback is unsupported");
            }
        }
    }

}