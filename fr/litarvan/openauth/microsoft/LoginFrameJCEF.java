package fr.litarvan.openauth.microsoft;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CompletableFuture;

public class LoginFrameJCEF extends JFrame {

    private CompletableFuture<String> future;
    private boolean completed;

    private CefApp cefApp;
    private CefClient client;
    private CefBrowser browser;

    public LoginFrameJCEF() {
        this.setTitle("Microsoft Authentication");
        this.setSize(750, 750);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Initialisation de JCEF
        CefApp.startup(new String[0]); // Démarrage de JCEF
        cefApp = CefApp.getInstance();
        client = cefApp.createClient();

        this.setLayout(new BorderLayout());
    }

    public CompletableFuture<String> start(String url) {
        if (future != null) return future;

        future = new CompletableFuture<>();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!completed)
                    future.complete(null);
                disposeBrowser();
            }
        });

        // Crée et ajoute le navigateur
        browser = client.createBrowser(url, false, false);
        this.add(browser.getUIComponent(), BorderLayout.CENTER);

        // Gestion des changements d'URL
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser cefBrowser, boolean isLoading,
                                             boolean canGoBack, boolean canGoForward) {
                SwingUtilities.invokeLater(() -> {
                    String currentUrl = cefBrowser.getURL();
                    if (currentUrl.contains("access_token")) {
                        future.complete(currentUrl);
                        completed = true;
                        disposeBrowser();
                        dispose();
                    }
                });
            }
        });

        // Affichage
        SwingUtilities.invokeLater(() -> this.setVisible(true));

        return future;
    }

    private void disposeBrowser() {
        if (browser != null) {
            browser.close(true);
            browser = null;
        }
        if (client != null) {
            client.dispose();
            client = null;
        }
        if (cefApp != null) {
            cefApp.dispose();
            cefApp = null;
        }
    }
}