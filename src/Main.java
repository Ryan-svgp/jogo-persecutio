import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame janela = new JFrame("Casa de Repouso");
        
        // Em vez de carregar o jogo, carrega o menu primeiro!
        TelaMenu menu = new TelaMenu(janela);
        janela.add(menu);
        
        // 1. Tira a barra de cima (o X, o minimizar e as bordas)
        janela.setUndecorated(true);
        // 2. Trava o tamanho para não deixar o usuário arrastar
        janela.setResizable(false);
        // 3. Força a tela cheia (ocupa o monitor todo)
        janela.setExtendedState(JFrame.MAXIMIZED_BOTH);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setVisible(true);
    }
}
