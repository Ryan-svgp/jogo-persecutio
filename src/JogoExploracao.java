import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

//teste

public class JogoExploracao extends JPanel implements KeyListener {
    // Posição do personagem no mapa gigante
    int mundoX = 1000;
    int mundoY = 1000;

    Image imagemPersonagem;
    Image imagemMapa; //mamou

    public JogoExploracao() {
        // Carrega o mapa e o personagem
        imagemMapa = new ImageIcon("img/repouso.jpg").getImage();
        imagemPersonagem = new ImageIcon("img/nave.png").getImage();

        // Diz para o painel "escutar" o teclado
        addKeyListener(this);
        setFocusable(true); // Foca a tela para receber os comandos
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Calcula o centro da tela
        int centroX = 400;
        int centroY = 300;

        // Calcula onde a ponta esquerda do mapa deve estar baseada no personagem
        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;

        // 1º Desenha o mapa (deslocado pela câmera)
        g.drawImage(imagemMapa, cameraX, cameraY, 2000, 1500, this);

        // 2º Desenha o personagem (sempre fixo no centro da tela)
        g.drawImage(imagemPersonagem, centroX - 25, centroY - 25, 50, 50, this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_RIGHT) { mundoX = mundoX + 10; }
        if (tecla == KeyEvent.VK_LEFT) { mundoX = mundoX - 10; }
        if (tecla == KeyEvent.VK_UP) { mundoY = mundoY - 10; }
        if (tecla == KeyEvent.VK_DOWN) { mundoY = mundoY + 10; }

        repaint(); // Chama o paintComponent de novo!
    }

    // O Java obriga a colocar esses dois, mesmo vazios
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

