package br.com.unip.erp.service;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Abre arquivos HTML no Google Chrome quando disponível e usa o navegador padrão como alternativa. */
public final class NavegadorUtil {
    private NavegadorUtil() {}

    public static void abrir(Path arquivo) throws IOException {
        Path absoluto = arquivo.toAbsolutePath().normalize();
        if (!Files.exists(absoluto)) {
            throw new IOException("O arquivo que será aberto no navegador não foi encontrado: " + absoluto);
        }
        String uri = absoluto.toUri().toString();

        String os = System.getProperty("os.name", "").toLowerCase();
        for (Path chrome : candidatosChrome(os)) {
            if (Files.isRegularFile(chrome)) {
                try {
                    new ProcessBuilder(chrome.toString(), "--new-window", uri).start();
                    return;
                } catch (IOException ignored) {
                    // Se o executável existir, mas não puder abrir, tenta o navegador padrão.
                }
            }
        }

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(absoluto.toUri());
            return;
        }

        if (os.contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", "", uri).start();
        } else if (os.contains("mac")) {
            new ProcessBuilder("open", uri).start();
        } else {
            new ProcessBuilder("xdg-open", uri).start();
        }
    }

    private static List<Path> candidatosChrome(String os) {
        List<Path> caminhos = new ArrayList<>();
        if (os.contains("win")) {
            adicionar(caminhos, System.getenv("PROGRAMFILES"), "Google/Chrome/Application/chrome.exe");
            adicionar(caminhos, System.getenv("PROGRAMFILES(X86)"), "Google/Chrome/Application/chrome.exe");
            adicionar(caminhos, System.getenv("LOCALAPPDATA"), "Google/Chrome/Application/chrome.exe");
            adicionar(caminhos, System.getenv("PROGRAMW6432"), "Google/Chrome/Application/chrome.exe");
        } else if (os.contains("mac")) {
            caminhos.add(Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"));
            String home = System.getProperty("user.home");
            if (home != null) caminhos.add(Path.of(home, "Applications/Google Chrome.app/Contents/MacOS/Google Chrome"));
        } else {
            caminhos.add(Path.of("/usr/bin/google-chrome"));
            caminhos.add(Path.of("/usr/bin/google-chrome-stable"));
            caminhos.add(Path.of("/usr/bin/chromium"));
            caminhos.add(Path.of("/usr/bin/chromium-browser"));
        }
        return caminhos;
    }

    private static void adicionar(List<Path> caminhos, String base, String relativo) {
        if (base != null && !base.isBlank()) caminhos.add(Path.of(base, relativo));
    }
}
