import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;

public class Main {
    static enum Actions {
        action1, action2, logout
    };

    public static void main(String[] args) {
        System.setProperty("java.security.auth.login.config", "jaas-config.cnf");

        try {        
            while (true) {
                LoginContext lc = new LoginContext("MyLoginContext", new MyCallbackHandler());
                lc.login();

                boolean actionFlag = true;
                
                while (actionFlag) {
                    actionFlag = performAction(lc);
                }
            }
        } catch (FailedLoginException fe) {
            System.err.println(fe.getMessage());
        } catch (LoginException le) {
            System.err.println("Login failed");
        }
    }

    static boolean performAction(LoginContext lc) {
        System.out.println("Enter action: action1, action2, logout");
        String action = System.console().readLine();
        boolean flag = true;

        try {
            switch (Actions.valueOf(action)) {
                case action1:
                    System.out.println("Performing action 1");
                    
                    PrivilegedAction<Object> privilegedAction1 = () -> {
                        System.out.print("action 1 was performed ");
                        return null;
                    };

                    Subject.doAs(lc.getSubject(), privilegedAction1);
                    System.out.println("by " + lc.getSubject().getPrincipals().iterator().next().getName());

                    break;
                
                case action2:
                    System.out.println("Performing action 2");

                    PrivilegedAction<Object> privilegedAction2 = () -> {
                        System.out.print("action 2 was performed ");
                        return null;
                    };

                    Subject.doAs(lc.getSubject(), privilegedAction2);
                    System.out.println("by " + lc.getSubject().getPrincipals().iterator().next().getName());

                    break;
                
                case logout:
                    System.out.println("Logging out");
                    
                    try {
                        lc.logout();
                    } catch (LoginException le) {
                        System.err.println("Logout failed");
                    }

                    flag = false;
            }
            
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid action");
        }

        return flag;
    }
}